package com.fp.ProductService.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fp.ProductService.dto.Product;
import com.fp.ProductService.service.ProductService;
import com.pja.common.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class ProductKafkaListener {

    private final ProductService productService;
    private final KafkaTemplate<String, ProductDto> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ProductKafkaListener(ProductService productService, KafkaTemplate<String, ProductDto> kafkaTemplate, ObjectMapper objectMapper) {
        this.productService = productService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }


    @KafkaListener(topics = "Send-Product-Event", groupId = "group_id")
    public void listenProduct(String jsonMessage) {
        try {
            Map<String, String> messageMap = objectMapper.readValue(jsonMessage, Map.class);
            String uuidString = messageMap.get("uuid");

            if (uuidString != null && !uuidString.isEmpty()) {
                log.info("Receiving message Product-listen-event From Product Kafka Listener: {}", jsonMessage);

                productService.findById(UUID.fromString(uuidString))
                        .subscribe(
                                product -> {
                                    ProductDto dto = new ProductDto();
                                    dto.setUuid(uuidString);
                                    if (product.getUuid() == null) {
                                        dto.setStatus("Not Found");
                                    } else {
                                        if (product.getStockQuantity() <= 0) {
                                            dto.setStatus("Not available");
                                        }
                                        dto.setStatus("Success");
                                    }
                                    kafkaTemplate.send("Product-listen-event", dto);
                                    log.info("Send event from product Kafka : {}", dto);
                                },
                                error -> log.error("Error finding product: {}", uuidString, error)
                        );
            } else {
                log.error("Invalid Kafka message: Missing fields in {}", jsonMessage);
            }
        } catch (IllegalArgumentException | JsonProcessingException e) {
            log.error("Invalid UUID format in Kafka message: {}", jsonMessage, e);
        }
    }
    @KafkaListener(topics = "Product-deduct-event", groupId = "group_id")
    public void listen(String jsonMessage) {
        log.info("Received message from Orchestrator: {}", jsonMessage);

        try {
            Map<String,String> messageMap = objectMapper.readValue(jsonMessage, Map.class);

            // Get the UUID string from the message map
            String uuidString = messageMap.get("uuid");

            if (uuidString != null) {
                UUID uuid = UUID.fromString(uuidString);

                // Deduct stock and subscribe to the result
                productService.deduct(uuid)
                        .subscribe(
                                updatedProduct -> log.info("Stock deducted successfully for product: {}", updatedProduct.getName()),
                                error -> log.error("Error deducting stock for product with UUID: {}", uuid, error)
                        );
            } else {
                log.error("UUID field is missing in Kafka message: {}", jsonMessage);
            }
        } catch (IOException e) {
            log.error("Error parsing Kafka message: {}", jsonMessage, e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in Kafka message: {}", jsonMessage, e);
        }
    }
}