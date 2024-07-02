package com.fp.OrderService.controller;
// Create
import com.fp.OrderService.dto.OrderItem;
import com.fp.OrderService.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orderitems")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    // Create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderItem> createOrderItem(@RequestBody OrderItem orderItem) {
        return orderItemService.createOrderItem(orderItem);
    }

    // Read all
    @GetMapping
    public Flux<OrderItem> getAllOrderItems() {
        return orderItemService.getAllOrderItems();
    }

    // Read by ID
    @GetMapping("/{id}")
    public Mono<ResponseEntity<OrderItem>> getOrderItemById(@PathVariable Long id) {
        return orderItemService.getOrderItemById(id)
                .map(item -> ResponseEntity.ok(item))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public Mono<ResponseEntity<OrderItem>> updateOrderItem(@PathVariable Long id, @RequestBody OrderItem orderItem) {
        return orderItemService.updateOrderItem(id, orderItem)
                .map(updatedItem -> ResponseEntity.ok(updatedItem))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteOrderItem(@PathVariable Long id) {
        return orderItemService.deleteOrderItem(id);
    }
}