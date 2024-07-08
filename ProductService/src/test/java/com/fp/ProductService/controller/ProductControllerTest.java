package com.fp.ProductService.controller;

import com.fp.ProductService.dto.GenericResponse;
import com.fp.ProductService.dto.Product;
import com.fp.ProductService.service.ProductService;
import com.fp.ProductService.util.AppConstant;
import com.pja.common.dto.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProducts() {
        Product product = new Product();
        when(productService.findProducts(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(Flux.just(product));

        StepVerifier.create(productController.getProducts("name", "category", 0, 10))
                .expectNextMatches(response ->
                        response.getStatus().equals(AppConstant.STATUS_SUCCESS) &&
                                response.getCode().equals(AppConstant.CODE_OK) &&
                                response.getMessage().equals(AppConstant.MESSAGE_PRODUCTS_RETRIEVED) &&
                                response.getData().equals(product)
                )
                .verifyComplete();
    }

    @Test
    void testUpdateProduct() {
        UUID id = UUID.randomUUID();
        ProductRequest request = new ProductRequest();
        Product updatedProduct = new Product();
        when(productService.update(id, request)).thenReturn(Mono.just(updatedProduct));

        StepVerifier.create(productController.updateProduct(id, request))
                .expectNextMatches(response ->
                        response.getStatus().equals(AppConstant.STATUS_SUCCESS) &&
                                response.getCode().equals(AppConstant.CODE_OK) &&
                                response.getMessage().equals(AppConstant.MESSAGE_PRODUCT_UPDATED) &&
                                response.getData().equals(updatedProduct)
                )
                .verifyComplete();
    }

    @Test
    void testAddProduct() {
        ProductRequest request = new ProductRequest();
        Product product = new Product();
        when(productService.save(request)).thenReturn(Mono.just(product));

        StepVerifier.create(productController.addProduct(request))
                .expectNextMatches(response ->
                        response.getStatus().equals(AppConstant.STATUS_SUCCESS) &&
                                response.getCode().equals(AppConstant.CODE_CREATED) &&
                                response.getMessage().equals(AppConstant.MESSAGE_PRODUCT_CREATED) &&
                                response.getData().equals(product)
                )
                .verifyComplete();
    }

    @Test
    void testGetProductId() {
        UUID id = UUID.randomUUID();
        Product product = new Product();
        when(productService.findById(id)).thenReturn(Mono.just(product));

        StepVerifier.create(productController.getProductId(id))
                .expectNextMatches(response ->
                        response.getStatus().equals(AppConstant.STATUS_SUCCESS) &&
                                response.getCode().equals(AppConstant.CODE_OK) &&
                                response.getMessage().equals(AppConstant.MESSAGE_PRODUCTS_RETRIEVED) &&
                                response.getData().equals(product)
                )
                .verifyComplete();
    }

    @Test
    void testDeleteProduct() {
        UUID id = UUID.randomUUID();
        when(productService.delete(id)).thenReturn(Mono.empty());

        StepVerifier.create(productController.deleteProduct(id))
                .verifyComplete();
    }
}