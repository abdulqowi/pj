package com.fp.OrderService.controller;

import com.fp.OrderService.dto.*;
import com.fp.OrderService.service.OrderService;
import com.fp.OrderService.utils.AppConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddOrder() {
        OrderRequest request = new OrderRequest();
        Order order = new Order();
        when(orderService.save(any(OrderRequest.class))).thenReturn(Mono.just(order));

        StepVerifier.create(orderController.addOrder(request))
                .expectNextMatches(response ->
                        response.getStatus().equals(AppConstant.STATUS_SUCCESS) &&
                                response.getCode().equals(AppConstant.CODE_CREATED) &&
                                response.getMessage().equals(AppConstant.MESSAGE_ORDER_CREATED) &&
                                response.getData().equals(order)
                )
                .verifyComplete();
    }

    @Test
    void testGetOrderDetails() {
        Long orderId = 1L;
        OrderResponse orderResponse = new OrderResponse();
        when(orderService.getAllItems(orderId)).thenReturn(Mono.just(orderResponse));

        StepVerifier.create(orderController.getOrderDetails(orderId))
                .expectNext(orderResponse)
                .verifyComplete();
    }

    @Test
    void testConfirmOrder() {
        Long orderId = 1L;
        OrderResponse orderResponse = new OrderResponse();
        when(orderService.confirmedOrder(orderId)).thenReturn(Mono.just(orderResponse));

        StepVerifier.create(orderController.confirmOrder(orderId))
                .expectNext(orderResponse)
                .verifyComplete();
    }

    @Test
    void testUpdateOrder() {
        Long id = 1L;
        OrderRequest request = new OrderRequest();
        Order updatedOrder = new Order();
        when(orderService.update(id, request)).thenReturn(Mono.just(updatedOrder));

        StepVerifier.create(orderController.updateOrder(id, request))
                .expectNextMatches(response ->
                        response.getStatus().equals(AppConstant.STATUS_SUCCESS) &&
                                response.getCode().equals(AppConstant.CODE_OK) &&
                                response.getMessage().equals(AppConstant.MESSAGE_ORDER_UPDATED) &&
                                response.getData().equals(updatedOrder)
                )
                .verifyComplete();
    }

    @Test
    void testDeleteOrder() {
        Long id = 1L;
        when(orderService.delete(id)).thenReturn(Mono.empty());

        StepVerifier.create(orderController.deleteOrder(id))
                .expectNextMatches(response ->
                        response.getStatus().equals(AppConstant.STATUS_SUCCESS) &&
                                response.getCode().equals(AppConstant.CODE_NO_CONTENT) &&
                                response.getMessage().equals(AppConstant.MESSAGE_ORDER_DELETED)
                )
                .verifyComplete();
    }

    @Test
    void testGetOrderById() {
        Long id = 1L;
        Order order = new Order();
        when(orderService.findById(id)).thenReturn(Mono.just(order));

        StepVerifier.create(orderController.getOrderById(id))
                .expectNextMatches(response ->
                        response.getStatus().equals(AppConstant.STATUS_SUCCESS) &&
                                response.getCode().equals(AppConstant.CODE_OK) &&
                                response.getMessage().equals(AppConstant.MESSAGE_ORDER_RETRIEVED) &&
                                response.getData().equals(order)
                )
                .verifyComplete();
    }

    @Test
    void testGetAllOrders() {
        Order order1 = new Order();
        Order order2 = new Order();
        when(orderService.findAll()).thenReturn(Flux.just(order1, order2));

        StepVerifier.create(orderController.getAllOrders())
                .expectNextMatches(response ->
                        response.getStatus().equals(AppConstant.STATUS_SUCCESS) &&
                                response.getCode().equals(AppConstant.CODE_OK) &&
                                response.getMessage().equals(AppConstant.MESSAGE_ORDER_RETRIEVED) &&
                                response.getData().equals(order1)
                )
                .expectNextMatches(response ->
                        response.getStatus().equals(AppConstant.STATUS_SUCCESS) &&
                                response.getCode().equals(AppConstant.CODE_OK) &&
                                response.getMessage().equals(AppConstant.MESSAGE_ORDER_RETRIEVED) &&
                                response.getData().equals(order2)
                )
                .verifyComplete();
    }
}