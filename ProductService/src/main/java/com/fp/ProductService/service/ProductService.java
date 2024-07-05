package com.fp.ProductService.service;

import com.fp.ProductService.config.GlobalModelMapper;
import com.fp.ProductService.dto.Product;
import com.fp.ProductService.repository.ProductRepository;
import com.pja.common.dto.ProductDto;
import com.pja.common.dto.ProductRequest;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, ProductDto> kafkaTemplate;
    private final ModelMapper mapper = GlobalModelMapper.getModelMapper();

    public ProductService(ProductRepository productRepository, KafkaTemplate<String, ProductDto> kafkaTemplate) {
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

//    public Mono<Product> deduct(UUID id) {
//        return findById(id)
//                .switchIfEmpty(Mono.error(new NoSuchElementException("Product not found")))
//                .flatMap(product -> {
//                    if (product.getStockQuantity() == 0) {
//                        log.error("Product out of stock");
//                        return Mono.empty();
//                    }
//                    product.setStockQuantity(product.getStockQuantity() - 1);
//                    return productRepository.save(product)
//                            .doOnNext(updatedProduct -> kafkaTemplate.send("Product-deduct-event", updatedProduct));
//                })
//                .doOnError(ex -> log.error("Error deducting product stock", ex));
//    }

    public Flux<Product> findProducts(String name, String category, int page, int size) {
        int offset = page * size;
        return productRepository.findAllByNameOrCategory(name, category, size, offset);
    }

    public Mono<Product>findById(UUID id){
        return productRepository.findById(id);
    }

    public Mono<Product> save(ProductRequest request) {
        Product product = mapper.map(request, Product.class);
        product.setCreatedAt(LocalDateTime.now());
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

