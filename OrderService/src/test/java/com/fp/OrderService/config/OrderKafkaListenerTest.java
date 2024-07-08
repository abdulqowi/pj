package com.fp.OrderService.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fp.OrderService.dto.OrderItem;
import com.fp.OrderService.repository.OrderItemRepository;
import com.fp.OrderService.service.OrderItemService;
import com.fp.OrderService.service.OrderService;
import com.pja.common.dto.ItemDto;
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

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderKafkaListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private KafkaTemplate<Object, List<ItemDto>> kafkaTemplate;

    @InjectMocks
    private OrderKafkaListener orderKafkaListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListenProduct_SuccessStatus() throws Exception {
        String jsonMessage = "{\"status\":\"success\",\"uuid\":\"123e4567-e89b-12d3-a456-556642440000\",\"price\":100.0,\"quantity\":2,\"orderId\":\"1\",\"itemId\":\"1\"}";
        ProductDto dto = new ProductDto();
        dto.setStatus("success");
        dto.setUuid("123e4567-e89b-12d3-a456-556642440000");
        dto.setPrice(100.0f);
        dto.setQuantity(2);
        dto.setOrderId("1");
        dto.setItemId(1L);

        when(objectMapper.readValue(eq(jsonMessage), eq(ProductDto.class))).thenReturn(dto);
        when(orderItemService.updateOrderItem(anyLong(), any(OrderItem.class))).thenReturn(Mono.empty());

        orderKafkaListener.listenProduct(jsonMessage);

        verify(orderItemService).updateOrderItem(eq(1L), any(OrderItem.class));
    }

    @Test
    void testListenProduct_FailureStatus() throws Exception {
        String jsonMessage = "{\"status\":\"failed\",\"itemId\":\"1\"}";
        ProductDto dto = new ProductDto();
        dto.setStatus("failed");
        dto.setItemId(1L);

        when(objectMapper.readValue(eq(jsonMessage), eq(ProductDto.class))).thenReturn(dto);
        when(orderItemService.deleteOrderItem(anyLong())).thenReturn(Mono.empty());

        orderKafkaListener.listenProduct(jsonMessage);

        verify(orderItemService).deleteOrderItem(1L);
    }

    @Test
    void testListenOrderProductEvent() throws Exception {
        String jsonMessage = "{\"orderId\":1}";
        HashMap<String, Integer> map = new HashMap<>();
        map.put("orderId", 1);

        when(objectMapper.readValue(eq(jsonMessage), eq(Map.class))).thenReturn(map);
        when(orderService.updateStatus(anyLong())).thenReturn(Mono.empty());
        when(orderItemRepository.findAllByOrderId(anyLong())).thenReturn(Flux.empty());

        orderKafkaListener.listenOrderProductEvent(jsonMessage);
    }

    @Test
    void testUpdateOrderAndSendToKafka() {
        long orderId = 1L;
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(UUID.randomUUID());
        orderItem.setQuantity(2);

        when(orderService.updateStatus(orderId)).thenReturn(Mono.empty());
        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(Flux.just(orderItem));
        when(kafkaTemplate.send(anyString(), anyList())).thenReturn(null);

        StepVerifier.create(orderKafkaListener.updateOrderAndSendToKafka(orderId))
                .verifyComplete();
    }
    @Test
    void testGetAllOrderItems() {
        long orderId = 1L;
        OrderItem orderItem = new OrderItem();
        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(Flux.just(orderItem));

        StepVerifier.create(orderKafkaListener.getAllOrderItems(orderId))
                .expectNext(Collections.singletonList(orderItem))
                .verifyComplete();
    }

    @Test
    void testSendKafkaMessage() {
        List<ItemDto> productInfos = Collections.singletonList(new ItemDto());
        when(kafkaTemplate.send(anyString(), anyList())).thenReturn(null);

        StepVerifier.create(orderKafkaListener.sendKafkaMessage(productInfos))
                .verifyComplete();

        verify(kafkaTemplate).send("Product-deduct-event", productInfos);
    }
}