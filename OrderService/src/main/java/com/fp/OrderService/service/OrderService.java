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
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    @Autowired
    private WebClient genericWebClient;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ModelMapper mapper = GlobalModelMapper.getModelMapper();
    private final KafkaTemplate<String, OrderSendResponse> transTemplate;

    //Mengisi Form Order
    public Mono<Order> save(OrderRequest orderRequest) {
        Order order = mapper.map(orderRequest, Order.class);
        String endpoint = "/balances/{id}";

        return setCustomerIdForOrder(order, endpoint)
                .flatMap(orderRepository::save);
    }

    public Mono<Order> setCustomerIdForOrder(Order order, String endpoint) {
        return this.genericWebClient.get()
                .uri(endpoint,order.getCustomerId())
                .header(HttpHeaders.ACCEPT,MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(GetCustomerId.class)
                .flatMap(customerId -> {
                    if (customerId != null && customerId.getCustomerId() != null) {
                        order.setCustomerId(customerId.getCustomerId());
                        return Mono.just(order);
                    } else {
                        return Mono.empty();
                    }
                })
                .doOnError(e -> log.error("Error occurred while retrieving customer ID: {}", e.getMessage(), e));
    }

    //Menyelesaikan Order
    public Mono<OrderResponse>getAllItems(Long id){
        Mono<Order> order = findById(id);
        Flux<OrderItem>items = orderItemRepository.findAllByOrderId(id);
        Mono<Float>totalPrice = orderItemRepository.sumPriceByOrderId(id);
        return order.flatMap(orders ->
                totalPrice.flatMap(totalPriceOrder->{
                    orders.setTotalAmount(totalPriceOrder);
                    return Mono.just(new OrderResponse()
                            .setOrder(orders)
                            .setItems(items));
                })
        );
    }
    //Konfirmasi Order
    public Mono<OrderResponse> confirmedOrder(Long id) {
        Mono<Order> orderMono = findById(id);
        Flux<OrderItem> items = orderItemRepository.findAllByOrderId(id);
        Mono<Float> totalPriceMono = orderItemRepository.sumPriceByOrderId(id);

        return orderMono.flatMap(order ->
                totalPriceMono.flatMap(totalPrice -> {
                    // Set total price in Order
                    order.setTotalAmount(totalPrice);

                    // Create OrderResponse
                    OrderResponse orderResponse = new OrderResponse()
                            .setOrder(order)
                            .setItems(items);

                    // Map Order to OrderSendResponse
                    OrderSendResponse orderSendResponse = new OrderSendResponse();
                    orderSendResponse.setOrderId(order.getId());
                    orderSendResponse.setAmount(totalPrice);
                    orderSendResponse.setStatus(String.valueOf(OrderStatus.CREATED));

                    // Send the OrderSendResponse message to Kafka
                    transTemplate.send("Order-Summary-Event", orderSendResponse);

                    return Mono.just(orderResponse);
                })
        );
    }
    public Mono<Order> update(Long id, OrderRequest orderRequest) {
        return orderRepository.findById(id)
                .flatMap(existingOrder -> {
                    mapper.map(orderRequest, existingOrder);
                    return orderRepository.save(existingOrder);
                })
                .switchIfEmpty(Mono.empty());
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
