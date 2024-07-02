package com.fp.OrderService.service;

import com.fp.OrderService.dto.OrderItem;
import com.fp.OrderService.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    public Mono<OrderItem> createOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    public Flux<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    public Mono<OrderItem> getOrderItemById(Long id) {
        return orderItemRepository.findById(id);
    }

    public Mono<OrderItem> updateOrderItem(Long id, OrderItem orderItemDetails) {
        return orderItemRepository.findById(id)
                .flatMap(existingItem -> {
                    existingItem.setPrice(orderItemDetails.getPrice());
                    existingItem.setQuantity(orderItemDetails.getQuantity());
                    existingItem.setOrderId(orderItemDetails.getOrderId());
                    return orderItemRepository.save(existingItem);
                });
    }
    public Mono<Void> deleteOrderItem(Long id) {
        return orderItemRepository.deleteById(id);
    }
}