package com.fp.OrderService.controller;

import com.fp.OrderService.dto.*;
import com.fp.OrderService.service.OrderService;
import com.fp.OrderService.utils.AppConstant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GenericResponse<Order>> addOrder(@RequestBody OrderRequest request) {
        return orderService.save(request)
                .flatMap(order -> {
                    GenericResponse<Order> response = new GenericResponse<>();
                    response.setStatus(AppConstant.STATUS_SUCCESS);
                    response.setCode(AppConstant.CODE_CREATED);
                    response.setMessage(AppConstant.MESSAGE_ORDER_CREATED);
                    response.setData(order);
                    return Mono.just(response);
                });
    }
    @GetMapping("/{orderId}/details")
    public Mono<OrderResponse> getOrderDetails(@PathVariable Long orderId) {
       return orderService.getAllItems(orderId);
    }
    @PostMapping("/{orderId}/confirm")
    public Mono<OrderResponse> confirmOrder(@PathVariable Long orderId) {
        return orderService.confirmedOrder(orderId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<GenericResponse<Order>> updateOrder(@PathVariable Long id, @RequestBody OrderRequest request) {
        return orderService.update(id, request)
                .flatMap(updatedOrder -> {
                    GenericResponse<Order> response = new GenericResponse<>();
                    response.setStatus(AppConstant.STATUS_SUCCESS);
                    response.setCode(AppConstant.CODE_OK);
                    response.setMessage(AppConstant.MESSAGE_ORDER_UPDATED);
                    response.setData(updatedOrder);
                    return Mono.just(response);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    GenericResponse<Order> response = new GenericResponse<>();
                    response.setStatus(AppConstant.STATUS_ERROR);
                    response.setCode(AppConstant.STATUS_NOT_FOUND);
                    response.setMessage(AppConstant.MESSAGE_ORDER_NOT_FOUND);
                    return Mono.just(response);
                }));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<GenericResponse<Void>> deleteOrder(@PathVariable Long id) {
        return orderService.delete(id)
                .then(Mono.defer(() -> {
                    GenericResponse<Void> response = new GenericResponse<>();
                    response.setStatus(AppConstant.STATUS_SUCCESS);
                    response.setCode(AppConstant.CODE_NO_CONTENT);
                    response.setMessage(AppConstant.MESSAGE_ORDER_DELETED);
                    return Mono.just(response);
                }));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<GenericResponse<Order>> getOrderById(@PathVariable Long id) {
        return orderService.findById(id)
                .flatMap(order -> {
                    GenericResponse<Order> response = new GenericResponse<>();
                    response.setStatus(AppConstant.STATUS_SUCCESS);
                    response.setCode(AppConstant.CODE_OK);
                    response.setMessage(AppConstant.MESSAGE_ORDER_RETRIEVED);
                    response.setData(order);
                    return Mono.just(response);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    GenericResponse<Order> response = new GenericResponse<>();
                    response.setStatus(AppConstant.STATUS_ERROR);
                    response.setCode(AppConstant.STATUS_NOT_FOUND);
                    response.setMessage(AppConstant.MESSAGE_ORDER_NOT_FOUND);
                    return Mono.just(response);
                }));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<GenericResponse<Order>> getAllOrders() {
        return orderService.findAll()
                .map(order -> {
                    GenericResponse<Order> response = new GenericResponse<>();
                    response.setStatus(AppConstant.STATUS_SUCCESS);
                    response.setCode(AppConstant.CODE_OK);
                    response.setMessage(AppConstant.MESSAGE_ORDER_RETRIEVED);
                    response.setData(order);
                    return response;
                });
    }
}
