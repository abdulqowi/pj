package com.fp.OrderService.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "orders")
@Getter
@Setter
@RequiredArgsConstructor
public class Order {
    @Id
    private Long id;
    private String billingAddress;
    private Integer customerId;
    private LocalDateTime orderDate;
    private String paymentMethod;
    private String orderStatus;
    private String shippingAddress;
    private Double totalAmount;
}
