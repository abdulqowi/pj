package com.fp.OrderService.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fp.OrderService.dto.OrderItem;
import com.fp.OrderService.service.OrderItemService;
import com.pja.common.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class OrderKafkaListener {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    OrderItemService orderItemService;
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
}
