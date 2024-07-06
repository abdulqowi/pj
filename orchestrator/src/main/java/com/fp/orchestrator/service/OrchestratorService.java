package com.fp.orchestrator.service;

import com.fp.orchestrator.service.dto.OrchestatorDto;
import com.pja.common.dto.OrderSendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class OrchestratorService {
    @Autowired
    private WebClient genericWebClient;
    public Mono<OrchestatorDto> sendOrder(OrderSendResponse response) {
        return this.genericWebClient.post()
                .uri("/transactions")
                .body(Mono.just(response), OrderSendResponse.class)
                .retrieve()
                .bodyToMono(OrchestatorDto.class)
                .doOnSuccess(result -> log.info("Successfully sent order: {}", result))
                .doOnError(error -> log.error("Failed to send order: {}", error.getMessage()));
    }
}
