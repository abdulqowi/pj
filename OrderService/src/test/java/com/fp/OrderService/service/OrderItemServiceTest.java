package com.fp.OrderService.service;

import com.fp.OrderService.dto.Order;
import com.fp.OrderService.dto.OrderItem;
import com.fp.OrderService.repository.OrderItemRepository;
import com.fp.OrderService.repository.OrderRepository;
import com.pja.common.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, ProductDto> kafkaTemplate;

    @InjectMocks
    private OrderItemService orderItemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrderId(100L);
        orderItem.setProductId(UUID.randomUUID());
        orderItem.setQuantity(2);

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(orderItem));
        when(kafkaTemplate.send(anyString(), any(ProductDto.class))).thenReturn(null);

        StepVerifier.create(orderItemService.createOrderItem(orderItem, 100L))
                .expectNextMatches(savedItem ->
                        savedItem.getId().equals(1L) &&
                                savedItem.getOrderId().equals(100L) &&
                                savedItem.getStatus().equals("Unconfirmed")
                )
                .verifyComplete();

        verify(kafkaTemplate).send(eq("Order-Send-event"), any(ProductDto.class));
    }

    @Test
    void testConfirmedItem() {
        Order order = new Order();
        order.setId(100L);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrderId(100L);
        orderItem.setProductId(UUID.randomUUID());
        orderItem.setStatus("Unconfirmed");

        when(orderRepository.findById(100L)).thenReturn(Mono.just(order));
        when(orderItemRepository.findById(1L)).thenReturn(Mono.just(orderItem));

        // Mock the save method to return the item with updated status
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> {
            OrderItem savedItem = invocation.getArgument(0);
            savedItem.setStatus("Confirmed");
            return Mono.just(savedItem);
        });

        // Mock Kafka template send to return a successful future
        when(kafkaTemplate.send(anyString(), any(ProductDto.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        StepVerifier.create(orderItemService.confirmedItem(orderItem, 100L))
                .expectNextMatches(confirmedItem ->
                        confirmedItem.getId().equals(1L) &&
                                confirmedItem.getOrderId().equals(100L) &&
                                confirmedItem.getStatus().equals("Confirmed")
                )
                .verifyComplete();

        // Verify that Kafka message was sent
        verify(kafkaTemplate).send(anyString(), any(ProductDto.class));
    }
    @Test
    void testGetAllOrderItems() {
        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        OrderItem item2 = new OrderItem();
        item2.setId(2L);

        when(orderItemRepository.findAll()).thenReturn(Flux.just(item1, item2));

        StepVerifier.create(orderItemService.getAllOrderItems())
                .expectNext(item1, item2)
                .verifyComplete();
    }

    @Test
    void testGetOrderItemById() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);

        when(orderItemRepository.findById(1L)).thenReturn(Mono.just(orderItem));

        StepVerifier.create(orderItemService.getOrderItemById(1L))
                .expectNext(orderItem)
                .verifyComplete();
    }

    @Test
    void testGetOrderItemById_NotFound() {
        when(orderItemRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(orderItemService.getOrderItemById(1L))
                .expectError(NoSuchElementException.class)
                .verify();
    }

    @Test
    void testUpdateOrderItem() {
        OrderItem existingItem = new OrderItem();
        existingItem.setId(1L);
        existingItem.setPrice(10.0f);

        OrderItem updatedItem = new OrderItem();
        updatedItem.setId(1L);
        updatedItem.setPrice(20.0f);

        when(orderItemRepository.findById(1L)).thenReturn(Mono.just(existingItem));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(updatedItem));

        StepVerifier.create(orderItemService.updateOrderItem(1L, updatedItem))
                .expectNextMatches(item -> item.getPrice().equals(20.0f))
                .verifyComplete();
    }

    @Test
    void testDeleteOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);

        when(orderItemRepository.findById(1L)).thenReturn(Mono.just(orderItem));
        when(orderItemRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(orderItemService.deleteOrderItem(1L))
                .verifyComplete();

        verify(orderItemRepository).deleteById(1L);
    }
}