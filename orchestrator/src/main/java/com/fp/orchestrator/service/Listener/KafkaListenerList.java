package com.fp.orchestrator.service.Listener;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public KafkaListenerList(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }
    @KafkaListener(topics = "Product-listen-event", groupId = "group_id")
    public void listenProduct(String jsonMessage) {
        log.info("Received message from Product-listen-event Orchestrator : {}", jsonMessage);
        try {
            if (jsonMessage!=null&&!jsonMessage.isEmpty()) {
                log.info("Forwarding message to Product-listen-event Kafka topic: {}", jsonMessage);
                kafkaTemplate.send("Product-listen-event", jsonMessage);
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
