package com.pja.common.dto;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class OrderSendResponse {
    private Long orderId;
    private Integer customerId;
    private Float amount;
    private String status;
}