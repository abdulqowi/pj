package com.fp.OrderService.service;

import com.fp.OrderService.config.GlobalModelMapper;
import com.fp.OrderService.dto.Order;
import com.fp.OrderService.dto.OrderItem;
import com.fp.OrderService.dto.OrderRequest;
import com.fp.OrderService.repository.OrderItemRepository;
import com.fp.OrderService.repository.OrderRepository;
import com.pja.common.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ModelMapper mapper = GlobalModelMapper.getModelMapper();

    public Mono<Order> save(OrderRequest orderRequest) {
        Order order = mapper.map(orderRequest, Order.class);
        return orderRepository.save(order);
    }

    public Flux<OrderItem>getAllItems(Long id){
        return orderItemRepository.findAllByOrderId(id);
    }
    public Mono<Order> update(Long id, OrderRequest orderRequest) {
        return orderRepository.findById(id)
                .flatMap(existingOrder -> {
                    mapper.map(orderRequest, existingOrder);
                    return orderRepository.save(existingOrder);
                })
                .switchIfEmpty(Mono.empty());
    }

    public Mono<Void> delete(Long id) {
        return orderRepository.deleteById(id);
    }

    public Mono<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Flux<Order> findAll() {
        return orderRepository.findAll();
    }

}
