package com.fp.OrderService.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class OrderItem {
    @Id
    private Long id;
    private Float price;
    private Integer quantity;
    private Long orderId;
    private UUID productId;
}
