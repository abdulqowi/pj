package com.pja.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ItemDto {
    private String productId;
    private Integer quantity;
}
