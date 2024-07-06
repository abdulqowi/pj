package com.fp.TransService.repository;

import com.fp.TransService.dto.TransactionDetail;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface TransactionRepository extends ReactiveCrudRepository<TransactionDetail,Integer> {
    Mono<TransactionDetail>findByOrderId(Long orderId);
}
