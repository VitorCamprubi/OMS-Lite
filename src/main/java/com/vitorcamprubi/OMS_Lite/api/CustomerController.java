package com.vitorcamprubi.OMS_Lite.api;

import com.vitorcamprubi.OMS_Lite.domain.Customer;
import com.vitorcamprubi.OMS_Lite.dto.customer.CreateCustomerRequest;
import com.vitorcamprubi.OMS_Lite.exception.ApiError;
import com.vitorcamprubi.OMS_Lite.service.CustomerService;
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
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Operações de cadastro e consulta de clientes")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @Operation(summary = "Criar cliente", description = "Cria um novo cliente. E-mail e documento devem ser únicos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente criado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "E-mail ou documento já cadastrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Customer> create(@Valid @RequestBody CreateCustomerRequest request) {
        Customer saved = customerService.create(request);
        return ResponseEntity
                .created(URI.create("/api/customers/" + saved.getId()))
                .body(saved);
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Lista todos os clientes cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista de clientes")
    public List<Customer> findAll() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public Customer findById(@PathVariable Long id) {
        return customerService.findById(id);
    }
}
