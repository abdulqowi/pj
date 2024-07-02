package com.fp.OrderService.repository;

import com.fp.OrderService.dto.OrderItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem,Long> {
}
