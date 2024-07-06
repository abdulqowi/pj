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
    private Integer customerId;
    private String billingAddress;
    private String paymentMethod;
    private String shippingAddress;
}
