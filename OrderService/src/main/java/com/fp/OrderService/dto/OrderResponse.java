package com.fp.OrderService.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Data
@Accessors(chain = true)
public class OrderResponse {
    private Order order;
    private Flux<OrderItem> items;
}
