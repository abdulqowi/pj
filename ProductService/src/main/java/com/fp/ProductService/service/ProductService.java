package com.fp.ProductService.service;

import com.fp.ProductService.config.GlobalModelMapper;
import com.fp.ProductService.dto.Product;
import com.fp.ProductService.dto.ProductRequest;
import com.fp.ProductService.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ModelMapper mapper = GlobalModelMapper.getModelMapper();

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Flux<Product> findProducts(String name, String category, Float price, LocalDateTime updatedAt, String sortBy, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc(sortBy)));
        return productRepository.findAllByNameContainingOrCategoryContainingOrPriceOrUpdatedAt(name, category, price, updatedAt, pageable);
    }
    public Mono<Product>findById(UUID id){
        return productRepository.findById(id);
    }

    public Mono<Product> save(ProductRequest request){
        var product = mapper.map(request, Product.class);
        return productRepository.save(product);
    }

    public Mono<Product> update(UUID id, ProductRequest request) {
        return productRepository.findById(id)
                .flatMap(existingProduct -> {
                    mapper.map(request, existingProduct);
                    return productRepository.save(existingProduct);
                })
                .switchIfEmpty(Mono.empty());
    }

    public Mono<Void>delete(UUID id){
        return productRepository.deleteById(id);
    }
}

