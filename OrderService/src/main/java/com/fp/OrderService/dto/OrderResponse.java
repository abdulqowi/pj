package com.fp.OrderService.dto;

import lombok.*;
import lombok.experimental.Accessors;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Accessors(chain = true)
public class OrderResponse {
    private Order order;
    private List<OrderItem> items;
}
