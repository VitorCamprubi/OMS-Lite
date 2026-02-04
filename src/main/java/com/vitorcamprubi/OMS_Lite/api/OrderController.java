package com.vitorcamprubi.OMS_Lite.api;

import com.vitorcamprubi.OMS_Lite.domain.Order;
import com.vitorcamprubi.OMS_Lite.dto.order.CreateOrderRequest;
import com.vitorcamprubi.OMS_Lite.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createConfirmedOrder(request.customerId(), request.items());
    }
}
