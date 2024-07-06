package com.fp.TransService.repository;

import com.fp.TransService.dto.CustomerBalance;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CustomerBalanceRepository extends ReactiveCrudRepository<CustomerBalance,Integer> {
    Mono<CustomerBalance>findByCustomerId(Integer customerId);
}
