package com.fp.OrderService.service;

import com.fp.OrderService.config.GlobalModelMapper;
import com.fp.OrderService.dto.*;
import com.fp.OrderService.repository.OrderItemRepository;
import com.fp.OrderService.repository.OrderRepository;
import com.pja.common.dto.OrderSendResponse;
import com.pja.common.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final WebClient genericWebClient;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ModelMapper mapper = GlobalModelMapper.getModelMapper();
    private final KafkaTemplate<String, OrderSendResponse> transTemplate;

    //Mengisi Form Order
    public Mono<Order> save(OrderRequest orderRequest) {
        Order order = mapper.map(orderRequest, Order.class);
        order.setOrderStatus("unconfirmed");
        ZoneId zoneId = ZoneId.of("Asia/Jakarta");
        order.setOrderDate(LocalDateTime.now(zoneId));
        order.setTotalAmount(0f);
        String endpoint = "/balances/{id}";

        return setCustomerIdForOrder(order, endpoint)
                .flatMap(orderRepository::save)
                .doOnSuccess(savedOrder -> log.info("Order saved successfully: {}", savedOrder))
                .doOnError(e -> log.error("Failed to save order: {}", e.getMessage(), e));
    }

    public Mono<Order> setCustomerIdForOrder(Order order, String endpoint) {
        return this.genericWebClient.get()
                .uri(endpoint, order.getCustomerId())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        log.info("Success :{}",order);
                        return Mono.just(order);
                    } else {
                        log.error("Error occurred while retrieving customer ID, status code: {}", response.statusCode());
                        return response.createException().flatMap(Mono::error);
                    }
                })
                .doOnError(e -> log.error("Error occurred while retrieving customer ID: {}", e.getMessage(), e));
    }

    //Menyelesaikan Order
    public Mono<OrderResponse> getAllItems(Long id) {
        Mono<Order> orderMono = findById(id);
        Flux<OrderItem> itemsFlux = orderItemRepository.findAllByOrderId(id);
        Mono<Float> totalPriceMono = orderItemRepository.sumPriceByOrderId(id);

        return orderMono
                .zipWith(totalPriceMono)
                .flatMap(tuple -> {
                    Order order = tuple.getT1();
                    Float totalPrice = tuple.getT2();
                    order.setTotalAmount(totalPrice);
                    return itemsFlux.collectList()
                            .map(items -> {
                                OrderResponse response = new OrderResponse()
                                        .setOrder(order)
                                        .setItems(items);
                                log.info("OrderResponse constructed: {}", response);
                                return response;
                            });
                });
    }

    //Konfirmasi Order
    public Mono<OrderResponse> confirmedOrder(Long id) {
        Mono<Order> orderMono = findById(id);
        Flux<OrderItem> itemsFlux = orderItemRepository.findAllByOrderId(id);
        Mono<Float> totalPriceMono = orderItemRepository.sumPriceByOrderId(id);

        return orderMono
                .zipWith(totalPriceMono)
                .flatMap(tuple -> {
                    Order order = tuple.getT1();
                    Float totalPrice = tuple.getT2();
                    order.setTotalAmount(totalPrice);

                    return itemsFlux.collectList().flatMap(items -> {
                        // Create OrderResponse
                        OrderResponse orderResponse = new OrderResponse()
                                .setOrder(order)
                                .setItems(items);

                        // Map Order to OrderSendResponse
                        OrderSendResponse orderSendResponse = new OrderSendResponse();
                        orderSendResponse.setOrderId(order.getId());
                        orderSendResponse.setCustomerId(order.getCustomerId());
                        orderSendResponse.setAmount(totalPrice);
                        orderSendResponse.setStatus(String.valueOf(OrderStatus.CREATED));

                        // Send the OrderSendResponse message to Kafka
                        return Mono.fromFuture(transTemplate.send("Order-Summary-Event", orderSendResponse))
                                .doOnSuccess(result -> log.info("Sent to Kafka: {}", orderSendResponse))
                                .doOnError(error -> log.error("Failed to send to Kafka: {}", error.getMessage()))
                                .thenReturn(orderResponse);
                    });
                });
    }


    public Mono<Order> update(Long id, OrderRequest orderRequest) {
        return orderRepository.findById(id)
                .flatMap(existingOrder -> {
                    mapper.map(orderRequest, existingOrder);
                    return orderRepository.save(existingOrder);
                })
                .switchIfEmpty(Mono.empty());
    }

    public Mono<Order> updateStatus(Long id) {
        return orderRepository.findById(id)
                .flatMap(existingOrder -> {
                    existingOrder.setOrderStatus(OrderStatus.COMPLETED.toString());
                    return orderRepository.save(existingOrder);
                })
                .doOnSuccess(updatedOrder -> log.info("Order status updated successfully for id: {}", id))
                .doOnError(error -> log.error("Error updating order status for id: {}", id, error))
                .switchIfEmpty(Mono.error(new NoSuchElementException("Order not found with id: " + id)));
    }
    public Mono<Void> delete(Long id) {
        return orderRepository.deleteById(id);
    }

    public Mono<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Flux<Order> findAll() {
        return orderRepository.findAll();
    }

}
