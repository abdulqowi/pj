package com.fp.ProductService.repository;

import com.fp.ProductService.dto.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ProductRepository extends ReactiveCrudRepository<Product, UUID> {

    @Query(value = "SELECT * FROM product p WHERE " +
            "(:name IS NULL OR p.name LIKE CONCAT('%', :name, '%')) " +
            "AND (:category IS NULL OR p.category LIKE CONCAT('%', :category, '%')) " +
            "ORDER BY p.created_at DESC " +
            "LIMIT :size OFFSET :offset")
    Flux<Product> findAllByNameOrCategory(
            @Param("name") String name,
            @Param("category") String category,
            @Param("size") int size,
            @Param("offset") int offset
    );
}



