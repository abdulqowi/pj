package com.fp.OrderService.controller;

import com.fp.OrderService.dto.OrderItem;
import com.fp.OrderService.service.OrderItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderItemController.class)
class OrderItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderItemService orderItemService;

    @Test
    void createOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);

        when(orderItemService.confirmedItem(any(OrderItem.class), anyLong())).thenReturn(Mono.just(orderItem));
        when(orderItemService.getOrderItemById(anyLong())).thenReturn(Mono.just(orderItem));

        webTestClient.post()
                .uri("/orderitems/1")
                .bodyValue(orderItem)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderItem.class);
    }

    @Test
    void getAllOrderItems() {
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setId(1L);

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setId(2L);

        when(orderItemService.getAllOrderItems()).thenReturn(Flux.just(orderItem1, orderItem2));

        webTestClient.get()
                .uri("/orderitems")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderItem.class);
    }

    @Test
    void getOrderItemById() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);

        when(orderItemService.getOrderItemById(1L)).thenReturn(Mono.just(orderItem));

        webTestClient.get()
                .uri("/orderitems/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderItem.class);
    }

    @Test
    void updateOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);

        when(orderItemService.updateOrderItem(eq(1L), any(OrderItem.class))).thenReturn(Mono.just(orderItem));

        webTestClient.put()
                .uri("/orderitems/1")
                .bodyValue(orderItem)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderItem.class);
    }

    @Test
    void deleteOrderItem() {
        when(orderItemService.deleteOrderItem(1L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/orderitems/1")
                .exchange()
                .expectStatus().isNoContent();
    }
}