package com.fp.OrderService.repository;

import com.fp.OrderService.dto.OrderItem;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem,Long> {
    Flux<OrderItem>findAllByOrderId(Long orderId);
    @Query("SELECT SUM(o.price) FROM order_item o WHERE o.order_id = :orderId")
    Mono<Double> sumPriceByOrderId(@Param("orderId") Long orderId);
}
