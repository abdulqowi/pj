package com.fp.ProductService.controller;

import com.fp.ProductService.dto.GenericResponse;
import com.fp.ProductService.dto.Product;
import com.fp.ProductService.dto.ProductRequest;
import com.fp.ProductService.service.ProductService;
import com.fp.ProductService.util.AppConstant;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<GenericResponse<Product>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = AppConstant.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstant.DEFAULT_SIZE) int size
    ) {
        return productService.findProducts(name, category, page, size)
                .map(product -> {
                    GenericResponse<Product> response = new GenericResponse<>();
                    response.setStatus(AppConstant.STATUS_SUCCESS);
                    response.setCode(AppConstant.CODE_OK);
                    response.setMessage(AppConstant.MESSAGE_PRODUCTS_RETRIEVED);
                    response.setData(product);
                    return response;
                });
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<GenericResponse<Product>> updateProduct(@PathVariable UUID id, @RequestBody ProductRequest request) {
        return productService.update(id, request)
                .flatMap(updatedProduct -> {
                    GenericResponse<Product> response = new GenericResponse<>();
                    response.setStatus(AppConstant.STATUS_SUCCESS);
                    response.setCode(AppConstant.CODE_OK);
                    response.setMessage(AppConstant.MESSAGE_PRODUCT_UPDATED);
                    response.setData(updatedProduct);
                    return Mono.just(response);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    GenericResponse<Product> response = new GenericResponse<>();
                    response.setStatus(AppConstant.STATUS_ERROR);
                    response.setCode(AppConstant.STATUS_NOT_FOUND);
                    response.setMessage(AppConstant.MESSAGE_PRODUCT_NOT_FOUND);
                    return Mono.just(response);
                }));
    }
    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GenericResponse<Product>> addProduct(@Valid @RequestBody ProductRequest request) {
        return productService.save(request)
                .flatMap(product -> {
                    GenericResponse<Product> response = new GenericResponse<>();
                    response.setStatus(AppConstant.STATUS_SUCCESS);
                    response.setCode(AppConstant.CODE_CREATED);
                    response.setMessage(AppConstant.MESSAGE_PRODUCT_CREATED);
                    response.setData(product);
                    return Mono.just(response);
                });
    }
    @GetMapping("/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<GenericResponse<Product>> getProductId(@PathVariable UUID uuid) {
        return productService.findById(uuid)
                .flatMap(product -> {
                    GenericResponse<Product> response = new GenericResponse<>();
                    response.setStatus(AppConstant.STATUS_SUCCESS);
                    response.setCode(AppConstant.CODE_OK);
                    response.setMessage(AppConstant.MESSAGE_PRODUCTS_RETRIEVED);
                    response.setData(product);
                    return Mono.just(response);
                });
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<GenericResponse<Void>> deleteProduct(@PathVariable("uuid") UUID uuid) {
        return productService.delete(uuid)
                .flatMap(voidMono -> {
                    GenericResponse<Void> response = new GenericResponse<>();
                    response.setStatus(AppConstant.STATUS_SUCCESS);
                    response.setCode(AppConstant.CODE_NO_CONTENT);
                    response.setMessage(AppConstant.MESSAGE_PRODUCT_DELETED);
                    response.setData(voidMono);
                    return Mono.just(response);
                });
    }

    ///////////////////////////////////////// static method///////////////////////////
}
