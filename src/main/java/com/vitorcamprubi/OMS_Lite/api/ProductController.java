package com.vitorcamprubi.OMS_Lite.api;

import com.vitorcamprubi.OMS_Lite.domain.Product;
import com.vitorcamprubi.OMS_Lite.dto.product.CreateProductRequest;
import com.vitorcamprubi.OMS_Lite.exception.ApiError;
import com.vitorcamprubi.OMS_Lite.service.ProductService;
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
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Operações de cadastro e consulta de produtos")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Criar produto", description = "Cria um novo produto. O SKU deve ser único.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "SKU já cadastrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Product> create(@Valid @RequestBody CreateProductRequest request) {
        Product saved = productService.create(request);
        return ResponseEntity
                .created(URI.create("/api/products/" + saved.getId()))
                .body(saved);
    }

    @GetMapping
    @Operation(summary = "Listar produtos")
    @ApiResponse(responseCode = "200", description = "Lista de produtos")
    public List<Product> findAll() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public Product findById(@PathVariable Long id) {
        return productService.findById(id);
    }
}
