package com.fp.TransService.repository;

import com.fp.TransService.dto.CustomerBalance;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CustomerBalanceRepository extends ReactiveCrudRepository<CustomerBalance,Integer> {
}
