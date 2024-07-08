package com.fp.ProductService.service;

import com.fp.ProductService.dto.Product;
import com.fp.ProductService.repository.ProductRepository;
import com.fp.ProductService.service.ProductService;
import com.pja.common.dto.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest productRequest;
    private UUID productId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productId = UUID.randomUUID();
        product = new Product();
        product.setUuid(productId);
        product.setName("Test Product");
        product.setStockQuantity(10);
        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setStockQuantity(10);
    }

    @Test
    void testDeduct_success() {
        UUID productId = UUID.randomUUID();
        int quantity = 5;

        Product product = new Product();
        product.setUuid(productId);
        product.setName("Test Product");
        product.setStockQuantity(10);
        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setStockQuantity(10);

        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product));

        Mono<Product> result = productService.deduct(productId, quantity);

        StepVerifier.create(result)
                .expectNextMatches(updatedProduct -> updatedProduct.getStockQuantity() == 5)
                .verifyComplete();
    }

    @Test
    void testDeduct_productNotFound() {
        UUID productId = UUID.randomUUID();
        int quantity = 5;

        when(productRepository.findById(productId)).thenReturn(Mono.empty());

        Mono<Product> result = productService.deduct(productId, quantity);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Product not found"))
                .verify();
    }
    @Test
    void testCreateProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product));

        Mono<Product> result = productService.save(productRequest);

        StepVerifier.create(result)
                .expectNextMatches(savedProduct -> savedProduct.getName().equals("Test Product"))
                .verifyComplete();
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(productId)).thenReturn(Mono.just(product));

        Mono<Product> result = productService.findById(productId);

        StepVerifier.create(result)
                .expectNextMatches(fetchedProduct -> fetchedProduct.getUuid().equals(productId))
                .verifyComplete();
    }

    @Test
    void testUpdateProduct() {

        Product updatedProduct = new Product();
        updatedProduct.setUuid(productId);
        updatedProduct.setName("Updated Product");
        updatedProduct.setStockQuantity(20);

        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(updatedProduct));

        Mono<Product> result = productService.update(productId, productRequest);

        StepVerifier.create(result)
                .expectNextMatches(savedProduct -> savedProduct.getName().equals("Updated Product") && savedProduct.getStockQuantity() == 20)
                .verifyComplete();
    }

    @Test
    void testDeleteProduct() {
        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(productRepository.deleteById(productId)).thenReturn(Mono.empty());

        Mono<Void> result = productService.delete(productId);

        StepVerifier.create(result)
                .verifyComplete();
    }
}
