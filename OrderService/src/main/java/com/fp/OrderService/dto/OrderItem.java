package com.fp.OrderService.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table
@Getter
@Setter
@RequiredArgsConstructor
public class OrderItem {
    @Id
    private Long id;
    private Float price;
    private Integer quantity;
    private Long orderId;
}
