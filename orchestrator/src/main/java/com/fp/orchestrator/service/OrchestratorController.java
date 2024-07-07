package com.fp.orchestrator.service;

import com.pja.common.dto.OrderSendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/processor")

public class OrchestratorController {
    @Autowired
    OrchestratorService orchestratorService;
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void>sendMessage(@RequestBody OrderSendResponse response){
        return orchestratorService.sendToOrder(response);
    }
}
