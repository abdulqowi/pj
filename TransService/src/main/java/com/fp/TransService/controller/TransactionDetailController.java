package com.fp.TransService.controller;

import com.fp.TransService.dto.TransactionDetail;
import com.fp.TransService.service.TransactionDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transactions")
public class TransactionDetailController {

    private final TransactionDetailService transactionDetailService;

    @Autowired
    public TransactionDetailController(TransactionDetailService transactionDetailService) {
        this.transactionDetailService = transactionDetailService;
    }

    @PostMapping
    public Mono<TransactionDetail> createTransaction(@RequestBody TransactionDetail transactionDetail) {
        return transactionDetailService.saveTransactionDetail(transactionDetail);
    }

    @GetMapping("/{id}")
    public Mono<TransactionDetail> getTransactionDetailById(@PathVariable Integer id) {
        return transactionDetailService.getTransactionDetailById(Long.valueOf(id));
    }

    @GetMapping
    public Flux<TransactionDetail> getAllTransactionDetails() {
        return transactionDetailService.getAllTransactionDetails();
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteTransactionDetail(@PathVariable Integer id) {
        return transactionDetailService.deleteTransactionDetail(id)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
