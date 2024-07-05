package com.fp.OrderService.service;

import com.fp.OrderService.config.OrderKafkaListener;
import com.fp.OrderService.dto.OrderItem;
import com.fp.OrderService.repository.OrderItemRepository;
import com.pja.common.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.internals.Topic;
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
    private final KafkaTemplate<String, ProductDto> kafkaTemplate;

    public OrderItemService(KafkaTemplate<String, ProductDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<OrderItem> createOrderItem(OrderItem orderItem) {
        orderItem.setStatus("Unconfirmed");
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

    public Mono<OrderItem> confirmedItem(OrderItem orderItem) {
        return createOrderItem(orderItem)
                .flatMap(savedItem -> getOrderItemById(orderItem.getId())
                        .flatMap(item -> {
                            String status = item.getStatus();
                            if ("Not Found".equalsIgnoreCase(status)) {
                                return Mono.error(new NoSuchElementException("Items not found"));
                            }
                            if ("Not Available".equalsIgnoreCase(status)) {
                                return Mono.error(new RuntimeException("Product not available"));
                            }
                            log.info("saved items status: " + savedItem.getId());
                            return Mono.just(item);
                        })
                );
    }

//    public Mono<OrderResponse>getResponse(String jsonMessage,OrderItem item) {
//        var response = new OrderResponse();
//        response.setCode("200");
//        response.setMessage("Retrieved Product");
//        response.setOrder();
//    }

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
        return orderItemRepository.findById(id).delayElement(Duration.ofMillis(200));
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
        return orderItemRepository.deleteById(id);
    }
}