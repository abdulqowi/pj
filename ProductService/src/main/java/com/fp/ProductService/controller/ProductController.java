package com.fp.ProductService.controller;

import com.fp.ProductService.dto.GenericResponse;
import com.fp.ProductService.dto.Product;
import com.fp.ProductService.dto.ProductRequest;
import com.fp.ProductService.service.ProductService;
import com.fp.ProductService.util.AppConstant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
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
    public GenericResponse<Flux<Product>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Float price,
            @RequestParam(required = false) LocalDateTime updatedAt,
            @RequestParam(defaultValue = AppConstant.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstant.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstant.DEFAULT_SIZE) int size
    ) {
        Flux<Product> products = productService.findProducts(name, category, price, updatedAt, sortBy, page, size);

        GenericResponse<Flux<Product>> response = new GenericResponse<>();
        response.setStatus(AppConstant.STATUS_SUCCESS);
        response.setCode(AppConstant.CODE_OK);
        response.setMessage(AppConstant.MESSAGE_PRODUCTS_RETRIEVED);
        response.setData(products);

        return response;
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public GenericResponse<Mono<Product>> addProduct(@RequestBody ProductRequest request) {
        var products = productService.save(request);
        return createProductResponse(products, AppConstant.CODE_CREATED, AppConstant.MESSAGE_PRODUCT_CREATED);
    }

    @GetMapping("/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public GenericResponse<Mono<Product>> getProductId(@PathVariable UUID uuid) {
        var products = productService.findById(uuid);
        return createProductResponse(products, AppConstant.CODE_OK, AppConstant.MESSAGE_PRODUCTS_RETRIEVED);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public GenericResponse<Mono<Void>> deleteProduct(@PathVariable("uuid") UUID uuid) {
        var products = productService.delete(uuid);
        GenericResponse<Mono<Void>> response = new GenericResponse<>();
        response.setStatus(AppConstant.STATUS_SUCCESS);
        response.setCode(AppConstant.CODE_NO_CONTENT);
        response.setMessage(AppConstant.MESSAGE_PRODUCT_DELETED);
        response.setData(products);
        return response;
    }

    ///////////////////////////////////////// static method///////////////////////////
    private static GenericResponse<Mono<Product>> createProductResponse(Mono<Product> products, String code, String message) {
        GenericResponse<Mono<Product>> response = new GenericResponse<>();
        response.setStatus(AppConstant.STATUS_SUCCESS);
        response.setCode(code);
        response.setMessage(message);
        response.setData(products);

        return response;
    }
}
