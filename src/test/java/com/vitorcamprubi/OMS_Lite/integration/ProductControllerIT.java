package com.vitorcamprubi.OMS_Lite.integration;

import com.vitorcamprubi.OMS_Lite.domain.Product;
import com.vitorcamprubi.OMS_Lite.dto.product.CreateProductRequest;
import com.vitorcamprubi.OMS_Lite.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductControllerIT extends AbstractMySqlContainerTest {

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ProductRepository productRepository;

    @BeforeEach
    @Transactional
    void cleanState() {
        productRepository.deleteAll();
    }

    @Test
    void shouldCreateProduct_whenPostValidPayload() {
        var request = new CreateProductRequest("Teclado", "TEC-001", new BigDecimal("250.00"), 10);

        ResponseEntity<Product> response = rest.postForEntity("/api/products", request, Product.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull().isPositive();
        assertThat(response.getBody().getSku()).isEqualTo("TEC-001");
        assertThat(response.getHeaders().getLocation())
                .isNotNull()
                .satisfies(uri -> assertThat(uri.getPath()).startsWith("/api/products/"));
    }

    @Test
    void shouldReturnConflict_whenPostDuplicateSku() {
        var request = new CreateProductRequest("Teclado", "DUP-1", new BigDecimal("100.00"), 5);
        rest.postForEntity("/api/products", request, Product.class);

        ResponseEntity<String> second = rest.postForEntity("/api/products", request, String.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(second.getBody()).contains("sku");
    }

    @Test
    void shouldReturnBadRequest_whenPostInvalidPayload() {
        // sku in branco viola @NotBlank, unitPrice ausente viola @NotNull
        String invalid = """
                { "name": "x", "sku": "", "unitPrice": null, "stockQuantity": -1 }
                """;

        ResponseEntity<String> response = rest.postForEntity(
                "/api/products",
                requestWithJsonHeader(invalid),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnAllProducts_whenGetList() {
        rest.postForEntity("/api/products",
                new CreateProductRequest("A", "SKU-A", new BigDecimal("1.00"), 1), Product.class);
        rest.postForEntity("/api/products",
                new CreateProductRequest("B", "SKU-B", new BigDecimal("2.00"), 2), Product.class);

        ResponseEntity<Product[]> response = rest.getForEntity("/api/products", Product[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldReturnNotFound_whenGetByMissingId() {
        ResponseEntity<String> response = rest.getForEntity("/api/products/9999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private static org.springframework.http.HttpEntity<String> requestWithJsonHeader(String body) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new org.springframework.http.HttpEntity<>(body, headers);
    }
}
