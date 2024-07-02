package com.fp.ProductService.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Name is required")
    private String name;
    @Min(value = 1,message = "Price is required")
    private Float price;
    @NotBlank(message = "Category is required")
    private String category;
    private String description;
    @NotBlank(message = "Image is required")
    private String imgUrl;
    @Min(0)
    private Integer stockQuantity;
}
