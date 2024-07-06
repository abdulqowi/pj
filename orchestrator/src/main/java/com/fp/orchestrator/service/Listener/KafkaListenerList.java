package com.fp.orchestrator.service.Listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fp.orchestrator.service.OrchestratorService;
import com.pja.common.dto.OrderSendResponse;
import com.pja.common.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class KafkaListenerList {
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTemplate<String, ProductDto> productDtoKafkaTemplate;
    private final OrchestratorService orchestratorService;


    public KafkaListenerList(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate, KafkaTemplate<String, ProductDto> productDtoKafkaTemplate, OrchestratorService orchestratorService) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.productDtoKafkaTemplate = productDtoKafkaTemplate;
        this.orchestratorService = orchestratorService;
    }
    @KafkaListener(topics = "Order-Summary-Event", groupId = "group_id")
    public void listenOrderSummaryEvent(String jsonMessage) {
        log.info("Received OrderSummary event from Kafka: {}", jsonMessage);

        Mono.just(jsonMessage)
                .map(message -> {
                    try {
                        return objectMapper.readValue(message, OrderSendResponse.class);
                    } catch (Exception e) {
                        log.error("Error processing OrderSummary event: {}", jsonMessage, e);
                        throw new RuntimeException("Failed to parse JSON message");
                    }
                })
                .flatMap(orchestratorService::sendOrder)
                .doOnSuccess(eventDto -> log.info("Sent to Transaction: {}", eventDto))
                .doOnError(e -> log.error("Error processing OrderSummary event: {}", jsonMessage, e))
                .subscribe();
    }


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
