package com.fp.ProductService.repository;

import com.fp.ProductService.dto.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ProductRepository extends R2dbcRepository<Product, UUID> {
    Flux<Product> findAllByNameContainingOrCategoryContainingOrPriceOrUpdatedAt(
            String name,
            String category,
            BigDecimal price,
            LocalDateTime updatedAt,
            Pageable pageable
    );}
