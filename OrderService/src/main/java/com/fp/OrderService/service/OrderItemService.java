package com.fp.OrderService.service;

import com.fp.OrderService.dto.Order;
import com.fp.OrderService.dto.OrderItem;
import com.fp.OrderService.repository.OrderItemRepository;
import com.fp.OrderService.repository.OrderRepository;
import com.pja.common.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderRepository orderRepository;

    private final KafkaTemplate<String, ProductDto> kafkaTemplate;

    public OrderItemService(KafkaTemplate<String, ProductDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<OrderItem> createOrderItem(OrderItem orderItem, Long orderId) {
        orderItem.setStatus("Unconfirmed");
        orderItem.setOrderId(orderId);
        return orderItemRepository.save(orderItem).doOnNext(item -> {
            var product = new ProductDto();
            product.setOrderId(orderItem.getOrderId().toString());
            product.setItemId(item.getId());
            product.setPrice(0f);
            product.setUuid(orderItem.getProductId().toString());
            product.setQuantity(orderItem.getQuantity());
            product.setStatus(orderItem.getStatus());
            kafkaTemplate.send("Order-Send-event", product);
            log.info("Send Message to Kafka :  "+ product);
        });
    }

    public Mono<OrderItem> confirmedItem(OrderItem orderItem, Long orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Order ID not found")))
                .flatMap(order -> createOrderItem(orderItem, orderId))
                .flatMap(savedItem -> getOrderItemById(savedItem.getId())
                        .flatMap(item -> {
                            String status = item.getStatus();
                            if ("Not Found".equalsIgnoreCase(status)) {
                                return Mono.error(new NoSuchElementException("Items not found"));
                            }
                            if ("Not Available".equalsIgnoreCase(status)) {
                                return Mono.error(new RuntimeException("Product not available"));
                            }
                            log.info("Saved item status: " + savedItem.getId());
                            return Mono.just(item);
                        })
                );
    }

    public Flux<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

//    public Flux<OrderItem> getItems(Long id){
//        return getAllItems(id).flatMap(
//                orderItem -> {
//                    String productId = orderItem.getProductId().toString();
//                    ProductDto dto = new ProductDto();
//                    dto.setUuid(productId);
//                    return getAllItems(id)
//                            .doOnNext(ids->kafkaTemplate.send("Product-event", dto))
//                            .doOnNext(ids->log.info("Success get items : "+ dto));
//                }
//        );
//    }
    public Mono<OrderItem> getOrderItemById(Long id) {
        return orderItemRepository.findById(id)
            .delayElement(Duration.ofMillis(500))
            .switchIfEmpty(Mono.defer(() -> {
                log.error("Order item not found for id: {}", id);
                return Mono.error(new NoSuchElementException("Order item not found for id: " + id));
            }));
    }


    public Mono<OrderItem> updateOrderItem(Long id, OrderItem orderItemDetails) {
        return orderItemRepository.findById(id)
                .flatMap(existingItem -> {
                    existingItem.setPrice(orderItemDetails.getPrice());
                    existingItem.setQuantity(orderItemDetails.getQuantity());
                    existingItem.setOrderId(orderItemDetails.getOrderId());
                    existingItem.setProductId(orderItemDetails.getProductId());
                    existingItem.setOrderId(orderItemDetails.getOrderId());
                    existingItem.setStatus(orderItemDetails.getStatus());
                    return orderItemRepository.save(existingItem);
                });
    }
    public Mono<Void> deleteOrderItem(Long id) {
        return orderItemRepository.findById(id)
                .flatMap(item -> {
                    log.info("Deleting order item: {}", item);
                    return orderItemRepository.deleteById(id);
                })
                .then()
                .doOnSuccess(v -> log.info("Order item with id {} has been deleted", id))
                .doOnError(e -> log.error("Error deleting order item with id {}", id, e));
    }
}