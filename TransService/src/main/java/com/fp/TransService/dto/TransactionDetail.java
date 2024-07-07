package com.fp.TransService.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table
@Getter
@Setter
@RequiredArgsConstructor
public class TransactionDetail {
    @Id
    private Integer id;
    private Long orderId;
    private Integer customerId;
    private Float amount;
    private String status;
    private String referenceNumber;
    private LocalDateTime paymentDate;
}
