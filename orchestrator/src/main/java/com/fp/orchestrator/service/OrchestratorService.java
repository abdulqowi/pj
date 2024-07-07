package com.fp.orchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fp.orchestrator.service.dto.OrchestatorDto;
import com.pja.common.dto.OrderSendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class OrchestratorService {
    @Autowired
    private WebClient genericWebClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    KafkaTemplate<String,OrderSendResponse>kafkaTemplate;
    public Mono<OrchestatorDto> sendOrder(OrderSendResponse response) {
        return this.genericWebClient.post()
                .uri("/transactions")
                .body(Mono.just(response), OrderSendResponse.class)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(rawResponse -> {
                    log.info("Raw response: {}", rawResponse);
                    OrchestatorDto dto = new OrchestatorDto();
                    dto.setCode("200");
                    dto.setStatus("Success");
                    dto.setData(rawResponse);
                    return Mono.just(dto);
                })
                .doOnSuccess(result -> log.info("Successfully sent order: {}", result))
                .doOnError(error -> log.error("Failed to send order: {}", error.getMessage()));
    }
    public Mono<Void> sendToOrder(OrderSendResponse response) {
        return Mono.fromFuture(kafkaTemplate.send("Process-order-event", response))
                .doOnSuccess(result -> log.info("Successfully sent order: {}", response))
                .doOnError(error -> log.error("Failed to send order: {}", error.getMessage()))
                .then();
    }
}
