package com.fp.ProductService.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fp.ProductService.dto.Product;
import com.fp.ProductService.service.ProductService;
import com.pja.common.dto.ItemDto;
import com.pja.common.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductKafkaListenerTest {

    @Mock
    private ProductService productService;

    @Mock
    private KafkaTemplate<String, ProductDto> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductKafkaListener productKafkaListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListenProduct_ProductFound_EnoughStock() throws Exception {
        String jsonMessage = "{\"uuid\":\"123e4567-e89b-12d3-a456-556642440000\",\"quantity\":5}";
        ProductDto dto = new ProductDto();
        dto.setUuid("123e4567-e89b-12d3-a456-556642440000");
        dto.setQuantity(5);

        Product product = new Product();
        product.setStockQuantity(10);
        product.setPrice(100.0f);

        when(objectMapper.readValue(eq(jsonMessage), eq(ProductDto.class))).thenReturn(dto);
        when(productService.findById(any(UUID.class))).thenReturn(Mono.just(product));

        productKafkaListener.listenProduct(jsonMessage);

        verify(kafkaTemplate).send(eq("Order-listen-event"), argThat(sentDto ->
                sentDto.getStatus().equals("Success") &&
                        sentDto.getPrice() == 100.0f
        ));
    }

    @Test
    void testListenProduct_ProductFound_NotEnoughStock() throws Exception {
        String jsonMessage = "{\"uuid\":\"123e4567-e89b-12d3-a456-556642440000\",\"quantity\":15}";
        ProductDto dto = new ProductDto();
        dto.setUuid("123e4567-e89b-12d3-a456-556642440000");
        dto.setQuantity(15);

        Product product = new Product();
        product.setStockQuantity(10);

        when(objectMapper.readValue(eq(jsonMessage), eq(ProductDto.class))).thenReturn(dto);
        when(productService.findById(any(UUID.class))).thenReturn(Mono.just(product));

        productKafkaListener.listenProduct(jsonMessage);

        verify(kafkaTemplate).send(eq("Order-listen-event"), argThat(sentDto ->
                sentDto.getStatus().equals("Unavailable")
        ));
    }

    @Test
    void testListenProduct_ProductNotFound() throws Exception {
        String jsonMessage = "{\"uuid\":\"123e4567-e89b-12d3-a456-556642440000\",\"quantity\":5}";
        ProductDto dto = new ProductDto();
        dto.setUuid("123e4567-e89b-12d3-a456-556642440000");
        dto.setQuantity(5);

        when(objectMapper.readValue(eq(jsonMessage), eq(ProductDto.class))).thenReturn(dto);
        when(productService.findById(any(UUID.class))).thenReturn(Mono.empty());

        productKafkaListener.listenProduct(jsonMessage);

        verify(kafkaTemplate).send(eq("Order-listen-event"), argThat(sentDto ->
                sentDto.getStatus().equals("Failed")
        ));
    }

    @Test
    void testListen_SuccessfulDeduction() {
        ItemDto item1 = new ItemDto();
        item1.setProductId(UUID.randomUUID().toString());
        item1.setQuantity(5);

        ItemDto item2 = new ItemDto();
        item2.setProductId(UUID.randomUUID().toString());
        item2.setQuantity(3);

        when(productService.deduct(any(UUID.class), anyInt())).thenReturn(Mono.just(new Product()));

        productKafkaListener.listen(Arrays.asList(item1, item2));

        verify(productService, times(2)).deduct(any(UUID.class), anyInt());
    }

    @Test
    void testListen_ErrorDuringDeduction() {
        ItemDto item = new ItemDto();
        item.setProductId(UUID.randomUUID().toString());
        item.setQuantity(5);

        when(productService.deduct(any(UUID.class), anyInt())).thenReturn(Mono.error(new RuntimeException("Deduction failed")));

        productKafkaListener.listen(Arrays.asList(item));

        verify(productService).deduct(any(UUID.class), anyInt());
    }
}