package com.fp.OrderService.repository;

import com.fp.OrderService.dto.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}
