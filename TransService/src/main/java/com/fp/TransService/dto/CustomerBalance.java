package com.fp.TransService.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table
@Getter
@Setter
@RequiredArgsConstructor
public class CustomerBalance {
    @Id
    private Integer id;
    private Integer customerId;
    private Float amount;
}
