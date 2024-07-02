package com.fp.OrderService.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class OrderRequest {
    private String billingAddress;
    private Integer customerId;
    private LocalDateTime orderDate;
    private String paymentMethod;
    private String orderStatus;
    private String shippingAddress;
    private Double totalAmount;
}
