/*

Controller Layer (Presentation Layer) is the entry point of your application.
It exposes REST API endpoints to the outside world.

* Annotated with @RestController. It should remain "skinny"—meaning
* zero business logic or direct database queries should happen here.
* It just delegates to the Service layer.
*
* */

package com.wehavecrown.creatorstore.controllers;

import com.wehavecrown.creatorstore.dto.OrderRequest;
import com.wehavecrown.creatorstore.entities.Order;
import com.wehavecrown.creatorstore.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Order createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    // Get all orders
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // Get order by id
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        // TODO: to be implemented
        //
        // get into the service layer,
        // write the supporting methods
        // once supporting method is written
        // wire it up here, test on requstly/postman

        return orderService.getOrderById(id);
    }
}
