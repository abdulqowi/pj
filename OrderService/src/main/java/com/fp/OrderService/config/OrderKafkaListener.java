package com.fp.OrderService.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fp.OrderService.dto.OrderItem;
import com.fp.OrderService.service.OrderItemService;
import com.pja.common.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class OrderKafkaListener {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    OrderItemService orderItemService;
    private final ModelMapper mapper = GlobalModelMapper.getModelMapper();
    @KafkaListener(topics = "Order-listen-event", groupId = "group_id")
    public void listenProduct(String jsonMessage) {
        try {
            Map<String, String> messageMap = objectMapper.readValue(jsonMessage, Map.class);
            Map<String, Integer> intergermap = objectMapper.readValue(jsonMessage, Map.class);
            Map<String, Long> longMap = objectMapper.readValue(jsonMessage, Map.class);
            Map<String, Float> floatMap = objectMapper.readValue(jsonMessage, Map.class);
            Map<String, Double> duoMap = objectMapper.readValue(jsonMessage, Map.class);
            log.info(jsonMessage);
            ProductDto dto = new ProductDto();
            dto.setOrderId(messageMap.get("orderId"));
            dto.setItemId(Long.valueOf(intergermap.get("itemId")));
            dto.setUuid(messageMap.get("uuid"));
            dto.setPrice(Float.valueOf(String.valueOf(duoMap.get("price"))));
            dto.setQuantity(intergermap.get("quantity"));
            dto.setStatus(messageMap.get("status"));

            if (dto.getOrderId() != null && dto.getUuid() != null && dto.getStatus() != null) {
                log.info("Received Order-listen-event from Order Kafka Listener: {}", jsonMessage);
                var item = new OrderItem();
                item = mapper.map(dto, OrderItem.class);
                item.setProductId(UUID.fromString(dto.getUuid()));
                orderItemService.updateOrderItem(dto.getItemId(),item).subscribe();
            } else {
                log.error("Invalid Kafka message: Missing fields in {}", jsonMessage);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in Kafka message: {}", jsonMessage, e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
