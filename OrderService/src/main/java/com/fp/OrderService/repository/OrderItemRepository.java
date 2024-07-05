package com.fp.OrderService.repository;

import com.fp.OrderService.dto.OrderItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem,Long> {
    Flux<OrderItem>findAllByOrderId(Long orderId);
}
