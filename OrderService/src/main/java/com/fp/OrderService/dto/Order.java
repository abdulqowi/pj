package com.fp.OrderService.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
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
    private Integer customerId;
    private String billingAddress;
    private LocalDateTime orderDate;
    private String paymentMethod;
    private String orderStatus;
    private String shippingAddress;
    private Float totalAmount;
}
