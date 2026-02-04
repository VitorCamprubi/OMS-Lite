package com.vitorcamprubi.OMS_Lite.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateOrderRequest(
        @NotNull @Positive Long customerId,
        @NotEmpty @Valid List<ItemRequest> items
) {
    public record ItemRequest(
            @NotNull @Positive Long productId,
            @NotNull @Positive Integer quantity
    ) {}
}
