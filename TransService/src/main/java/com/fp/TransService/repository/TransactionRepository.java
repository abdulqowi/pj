package com.fp.TransService.repository;

import com.fp.TransService.dto.TransactionDetail;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TransactionRepository extends ReactiveCrudRepository<TransactionDetail,Integer> {
}
