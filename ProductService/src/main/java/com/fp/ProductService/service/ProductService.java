package com.fp.ProductService.service;

import com.fp.ProductService.dto.Product;
import com.fp.ProductService.repository.ProductRepository;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Flux<Product> findProducts(String name, String category, BigDecimal price, LocalDateTime updatedAt, String sortBy, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc(sortBy)));
        return productRepository.findAllByNameContainingOrCategoryContainingOrPriceOrUpdatedAt(name, category, price, updatedAt, pageable);
    }

    public Mono<Product> save(Product product){
        return productRepository.save(product);
    }
    public Mono<Product>update(UUID id,Product product){
        return productRepository.findById(id)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(optional ->{
                    if (optional.isPresent()){

                    }
                })
    }
}

