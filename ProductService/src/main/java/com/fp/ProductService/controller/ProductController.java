package com.fp.ProductService.controller;

import com.fp.ProductService.dto.Product;
import com.fp.ProductService.service.ProductService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Flux<Product> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) LocalDateTime updatedAt,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return productService.findProducts(name, category, price, updatedAt, sortBy, page, size);
    }
}

