package com.pja.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Accessors(chain = true)
public class ProductRequest {
    private String name;
    private Float price;
    private String category;
    private String description;
    private String imgUrl;
    private Integer stockQuantity;
}
