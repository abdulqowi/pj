package com.fp.TransService.controller;

import com.fp.TransService.dto.CustomerBalance;
import com.fp.TransService.service.CustomerBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/balances")
public class CustomerBalanceController {

    private final CustomerBalanceService customerBalanceService;

    @Autowired
    public CustomerBalanceController(CustomerBalanceService customerBalanceService) {
        this.customerBalanceService = customerBalanceService;
    }

    @PostMapping
    public Mono<CustomerBalance> createCustomerBalance(@RequestBody CustomerBalance customerBalance) {
        return customerBalanceService.saveCustomerBalance(customerBalance);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<CustomerBalance>> getCustomerBalanceById(@PathVariable Integer id) {
        return customerBalanceService.getCustomerBalanceById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<CustomerBalance> getAllCustomerBalances() {
        return customerBalanceService.getAllCustomerBalances();
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteCustomerBalance(@PathVariable Integer id) {
        return customerBalanceService.deleteCustomerBalance(id)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
