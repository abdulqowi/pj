package com.fp.OrderService.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pja.common.dto.ItemDto;
import com.fp.OrderService.dto.OrderItem;
import com.fp.OrderService.repository.OrderItemRepository;
import com.fp.OrderService.service.OrderItemService;
import com.fp.OrderService.service.OrderService;
import com.pja.common.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderKafkaListener {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    OrderService orderService;
    @Autowired
    KafkaTemplate<Object, List<ItemDto>> kafkaTemplate;
    @KafkaListener(topics = "Order-listen-event", groupId = "group_id")
    public void listenProduct(String jsonMessage) {
        try {
            ProductDto dto = objectMapper.readValue(jsonMessage, ProductDto.class);
            log.info("Received Order-listen-event: {}", jsonMessage);
            if (dto.getStatus().equalsIgnoreCase("success")) {
                var item = new OrderItem();
                item.setProductId(UUID.fromString(dto.getUuid()));
                item.setPrice(dto.getPrice());
                item.setStatus(dto.getStatus());
                item.setQuantity(dto.getQuantity());
                item.setOrderId(Long.valueOf(dto.getOrderId()));
                orderItemService.updateOrderItem(dto.getItemId(), item).subscribe();
            } else {
                log.warn("Attempting to delete order item due to non-success status: {}", dto.getStatus());
                orderItemService.deleteOrderItem(dto.getItemId()).subscribe();
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in Kafka message: {}", jsonMessage, e);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON message: {}", jsonMessage, e);
        }
    }
    @KafkaListener(topics = "Process-order-event", groupId = "group_id")
    public void listenOrderProductEvent(String jsonMessage) {
        try {
            log.info(jsonMessage);
            Map<String,Integer>map=objectMapper.readValue(jsonMessage,Map.class);
            long orderId = map.get("orderId");
            log.info("Received order product event for order ID: {}", orderId);

            updateOrderAndSendToKafka(orderId)
                    .subscribe();
        } catch (NumberFormatException e) {
            log.error("Invalid order ID format: {}", jsonMessage, e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Mono<Void> updateOrderAndSendToKafka(long orderId) {
        return orderService.updateStatus(orderId)
                .flatMap(order -> getAllOrderItems(orderId)
                        .flatMap(items -> {
                            List<ItemDto> productInfos = items.stream()
                                    .map(orderItem -> {
                                        ItemDto itemDto = new ItemDto();
                                        itemDto.setProductId(orderItem.getProductId().toString());
                                        itemDto.setQuantity(orderItem.getQuantity());
                                        log.info("DTO : {}",itemDto);
                                        return itemDto;
                                    })
                                    .collect(Collectors.toList());
                            log.info("Get ItemDTO :{}",productInfos);
                            return sendKafkaMessage(productInfos);
                        })
                )
                .doOnError(error -> log.error("Failed to process order event: {}", error.getMessage()))
                .then();
    }

    private Mono<List<OrderItem>> getAllOrderItems(long orderId) {
        return orderItemRepository.findAllByOrderId(orderId)
                .collectList()
                .doOnNext(items -> {
                    if (items.isEmpty()) {
                        log.warn("No order items found for order ID: {}", orderId);
                    }
                    log.info("Getting Items : {}",items);
                });
    }

    private Mono<Void> sendKafkaMessage(List<ItemDto> productInfos) {
        return Mono.fromRunnable(() -> {
            try {
                kafkaTemplate.send("Product-deduct-event", productInfos);
                log.info("Successfully sent Product-deduct-event message :{}",productInfos);
            } catch (Exception e) {
                log.error("Failed to send Product-deduct-event message", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
