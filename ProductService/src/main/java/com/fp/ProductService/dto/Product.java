package com.fp.ProductService.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Table
@Getter
@Setter
@RequiredArgsConstructor
public class Product {
    @Id
    private UUID id;
    private String name;
    private Float price;
    private String category;
    private String description;
    private String imgUrl;
    private Integer stockQuantity;
    @CreatedDate
    @JsonIgnore
    private LocalDateTime createdAt;
    @JsonIgnore
    private LocalDateTime updatedAt;
}
