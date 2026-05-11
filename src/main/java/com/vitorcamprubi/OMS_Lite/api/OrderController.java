package com.vitorcamprubi.OMS_Lite.api;

import com.vitorcamprubi.OMS_Lite.domain.Order;
import com.vitorcamprubi.OMS_Lite.dto.order.CreateOrderRequest;
import com.vitorcamprubi.OMS_Lite.exception.ApiError;
import com.vitorcamprubi.OMS_Lite.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Criação e consulta de pedidos")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(
            summary = "Criar pedido confirmado",
            description = """
                    Cria um pedido com status CONFIRMED. Para cada item:
                    valida existência do produto, baixa o estoque, calcula
                    o totalPrice da linha (unitPrice × quantity) e soma ao
                    totalAmount do pedido. Toda a operação é transacional.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Cliente ou produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Regra de negócio violada (ex.: estoque insuficiente)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Order> create(@Valid @RequestBody CreateOrderRequest request) {
        Order saved = orderService.createConfirmedOrder(request.customerId(), request.items());
        return ResponseEntity
                .created(URI.create("/api/orders/" + saved.getId()))
                .body(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public Order findById(@PathVariable Long id) {
        return orderService.findById(id);
    }
}
