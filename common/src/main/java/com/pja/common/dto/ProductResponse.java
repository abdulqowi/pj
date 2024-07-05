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
public class ProductResponse {
    private String uuid;
    private String name;
    private String price;
    private String category;
    private String description;
}
