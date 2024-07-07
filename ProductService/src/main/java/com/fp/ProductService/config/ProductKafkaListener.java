package com.fp.ProductService.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fp.ProductService.service.ProductService;
import com.pja.common.dto.ItemDto;
import com.pja.common.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
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
            ProductDto dto = objectMapper.readValue(jsonMessage, ProductDto.class);
            log.info("Receiving message Send-Product-event From Product Kafka Listener: {}", jsonMessage);

            productService.findById(UUID.fromString(dto.getUuid()))
                    .doOnNext(product -> log.info("Product found: {}", product))
                    .flatMap(product -> {
                        if (product.getStockQuantity()>= dto.getQuantity()){
                            dto.setPrice(product.getPrice());
                            dto.setStatus("Success");
                            log.info("DTO after processing: {}", dto);
                        }else {
                            dto.setStatus("Unavailable");
                        }
                        kafkaTemplate.send("Order-listen-event", dto);
                        return Mono.just(product);
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        dto.setStatus("Failed");
                        log.info("Product not found, setting DTO status to Failed: {}", dto);
                        kafkaTemplate.send("Order-listen-event", dto);
                        return Mono.empty();
                    }))
                    .doOnError(error -> log.error("Error processing product: {}", dto, error))
                    .subscribe(
                            result -> log.info("Successfully sent event: {}", dto),
                            error -> log.error("Failed to send event: {}", error.getMessage())
                    );
        } catch (Exception e) {
            log.error("Error processing Send-Product-event: {}", jsonMessage, e);
        }
    }

    @KafkaListener(topics = "Product-deduct-event", groupId = "group_id",containerFactory = "fooListener")
    public void listen(List<ItemDto> jsonMessage) {
        log.info("Received message from Orchestrator: {}", jsonMessage);
        try {
            Flux.fromIterable(jsonMessage)
                    .flatMap(item -> {
                        UUID uuid = UUID.fromString(item.getProductId());

                        return productService.deduct(uuid, item.getQuantity())
                                .doOnSuccess(updatedProduct -> log.info("Stock deducted successfully for product: {}, quantity: {}", updatedProduct.getName(),item.getProductId()))
                                .doOnError(error -> log.error("Error deducting stock for product with UUID: {}, quantity: {}", uuid, item.getQuantity(), error))
                                .onErrorResume(error -> Mono.empty());
                    })
                    .subscribe();
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in Kafka message: {}", jsonMessage, e);
        }
    }
}