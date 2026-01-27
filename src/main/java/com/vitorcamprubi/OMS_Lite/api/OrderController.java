package com.vitorcamprubi.OMS_Lite.api;

import com.vitorcamprubi.OMS_Lite.domain.Order;
import com.vitorcamprubi.OMS_Lite.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // DTO do corpo da requisição
    public static class CreateOrderRequest {
        private Long customerId;
        private List<OrderService.ItemRequest> items;

        public CreateOrderRequest() {
        }

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public List<OrderService.ItemRequest> getItems() {
            return items;
        }

        public void setItems(List<OrderService.ItemRequest> items) {
            this.items = items;
        }
    }

    @PostMapping
    public Order create(@RequestBody CreateOrderRequest request) {
        return orderService.createConfirmedOrder(
                request.getCustomerId(),
                request.getItems()
        );
    }
}
