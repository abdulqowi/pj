package com.fp.orchestrator.service.Listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pja.common.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class KafkaListenerList {
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTemplate<String, ProductDto> productDtoKafkaTemplate;


    public KafkaListenerList(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate, KafkaTemplate<String, ProductDto> productDtoKafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.productDtoKafkaTemplate = productDtoKafkaTemplate;
    }
    @KafkaListener(topics = "Product-listen-event", groupId = "group_id")
    public void listenProduct(String jsonMessage) throws JsonProcessingException {
        log.info("Received message from Product-listen-event Orchestrator : {}", jsonMessage);
        Map<String, String> messageMap = objectMapper.readValue(jsonMessage, Map.class);
        Map<String, Integer> intergermap = objectMapper.readValue(jsonMessage, Map.class);
        Map<String, Integer> longMap = objectMapper.readValue(jsonMessage, Map.class);
        Map<String, Double> duoMap = objectMapper.readValue(jsonMessage, Map.class);
        ProductDto dto = new ProductDto();
        dto.setOrderId(messageMap.get("orderId"));
        dto.setPrice(Float.valueOf(String.valueOf(duoMap.get("price"))));
        dto.setItemId(Long.valueOf(longMap.get("itemId")));
        dto.setUuid(messageMap.get("uuid"));
        dto.setQuantity(intergermap.get("quantity"));
        dto.setStatus(messageMap.get("status"));
        try {
            if (jsonMessage!=null&&!jsonMessage.isEmpty()) {
                log.info("Forwarding message to Order-listen-event Kafka topic: {}", jsonMessage);

                productDtoKafkaTemplate.send("Order-listen-event", dto);
            } else {
                log.error("Invalid Kafka message: Missing fields in {}", jsonMessage);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in Kafka message: {}", jsonMessage, e);
        }
    }

//    @KafkaListener(topics = "Transaction-event", groupId = "group_id")
//    public void listenTransaction(String jsonMessage) {
//        log.info("Received message from Transaction-event Kafka topic: {}", jsonMessage);
//
//        try {
//            Map<String, String> messageMap = objectMapper.readValue(jsonMessage, Map.class);
//
//            String uuidString = messageMap.get("uuid");
//            String quantity = messageMap.get("quantity");
//            String status = messageMap.get("status");
//
//            if (uuidString != null && quantity != null && status != null) {
//                log.info("Forwarding message to Product-deduct-event Kafka topic: {}", jsonMessage);
//                kafkaTemplate.send("Product-deduct-event", jsonMessage);
//            } else {
//                log.error("Invalid Kafka message: Missing fields in {}", jsonMessage);
//            }
//        } catch (IOException e) {
//            log.error("Error parsing Kafka message: {}", jsonMessage, e);
//        } catch (IllegalArgumentException e) {
//            log.error("Invalid UUID format in Kafka message: {}", jsonMessage, e);
//        }
//    }

    @KafkaListener(topics = "Order-Send-event", groupId = "group_id")
    public void listenOrder(String jsonMessage) {
        log.info("Received message from Order-event from Order: {}", jsonMessage);
        try {
            if (jsonMessage!=null) {
                log.info("Forwarding message to Send-Product-event to Product: {}", jsonMessage);
                kafkaTemplate.send("Send-Product-Event", jsonMessage);
            } else {
                log.error("Invalid Kafka message: Missing fields in {}", jsonMessage);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in Kafka message: {}", jsonMessage, e);
        }
    }
}
