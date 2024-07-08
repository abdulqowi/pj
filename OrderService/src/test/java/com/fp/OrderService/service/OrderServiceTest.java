package com.fp.OrderService.service;

import com.fp.OrderService.dto.*;
import com.fp.OrderService.repository.OrderItemRepository;
import com.fp.OrderService.repository.OrderRepository;
import com.pja.common.dto.OrderSendResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    private static MockWebServer mockWebServer;
    @Mock
    private WebClient genericWebClient;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private KafkaTemplate<String, OrderSendResponse> transTemplate;

    @InjectMocks
    private OrderService orderService;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        MockitoAnnotations.openMocks(this);
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        orderService = new OrderService(webClient,orderRepository, orderItemRepository, transTemplate);
    }

    @Test
    void testSave() {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId(1);

        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1);
        order.setOrderStatus("unconfirmed");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"id\": 1, \"balance\": 100}"));

        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.save(orderRequest))
                .expectNextMatches(savedOrder ->
                        savedOrder.getId().equals(1L) &&
                                savedOrder.getCustomerId().equals(1) &&
                                savedOrder.getOrderStatus().equals("unconfirmed")
                )
                .verifyComplete();
    }
    @Test
    void testGetAllItems() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1);

        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setOrderId(1L);

        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setOrderId(1L);

        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderItemRepository.findAllByOrderId(1L)).thenReturn(Flux.just(item1, item2));
        when(orderItemRepository.sumPriceByOrderId(1L)).thenReturn(Mono.just(100.0F));

        StepVerifier.create(orderService.getAllItems(1L))
                .expectNextMatches(orderResponse ->
                        orderResponse.getOrder().getId().equals(1L) &&
                                orderResponse.getItems().size() == 2 &&
                                orderResponse.getOrder().getTotalAmount() == 100.0F
                )
                .verifyComplete();
    }

    @Test
    void testConfirmedOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1);

        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setOrderId(1L);

        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderItemRepository.findAllByOrderId(1L)).thenReturn(Flux.just(item1));
        when(orderItemRepository.sumPriceByOrderId(1L)).thenReturn(Mono.just(50.0F));
        when(transTemplate.send(anyString(), any(OrderSendResponse.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        StepVerifier.create(orderService.confirmedOrder(1L))
                .expectNextMatches(orderResponse ->
                        orderResponse.getOrder().getId().equals(1L) &&
                                orderResponse.getItems().size() == 1 &&
                                orderResponse.getOrder().getTotalAmount() == 50.0F
                )
                .verifyComplete();

        verify(transTemplate, times(1)).send(eq("Order-Summary-Event"), any(OrderSendResponse.class));
    }

    @Test
    void testUpdateStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus("PENDING");

        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.updateStatus(1L))
                .expectNextMatches(updatedOrder ->
                        updatedOrder.getId().equals(1L) &&
                                updatedOrder.getOrderStatus().equals("COMPLETED")
                )
                .verifyComplete();
    }
}