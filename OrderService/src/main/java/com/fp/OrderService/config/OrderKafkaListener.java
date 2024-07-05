package com.fp.OrderService.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fp.OrderService.service.OrderItemService;
import com.pja.common.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class OrderKafkaListener {
    @Autowired
    ObjectMapper objectMapper;
    @KafkaListener(topics = "Product-listen-event", groupId = "group_id")
    public void listenProduct(String jsonMessage) {
        try {
//            Map<String,String> messageMap = objectMapper.readValue(jsonMessage, Map.class);
            if (jsonMessage!=null) {
                log.info("Received Product-listen-event from Order Kafka Listenre: {}", jsonMessage);
            } else {
                log.error("Invalid Kafka message: Missing fields in {}", jsonMessage);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in Kafka message: {}", jsonMessage, e);
        }
    }
}
