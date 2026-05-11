package com.vitorcamprubi.OMS_Lite.integration;

import com.vitorcamprubi.OMS_Lite.domain.Customer;
import com.vitorcamprubi.OMS_Lite.domain.Order;
import com.vitorcamprubi.OMS_Lite.domain.Product;
import com.vitorcamprubi.OMS_Lite.dto.customer.CreateCustomerRequest;
import com.vitorcamprubi.OMS_Lite.dto.order.CreateOrderRequest;
import com.vitorcamprubi.OMS_Lite.dto.product.CreateProductRequest;
import com.vitorcamprubi.OMS_Lite.repository.CustomerRepository;
import com.vitorcamprubi.OMS_Lite.repository.OrderRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIT extends AbstractMySqlContainerTest {

    @Autowired TestRestTemplate rest;
    @Autowired CustomerRepository customerRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;

    @BeforeEach
    @Transactional
    void cleanState() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();
    }

    private Customer createCustomer() {
        return rest.postForEntity(
                "/api/customers",
                new CreateCustomerRequest("Cliente IT", "it@example.com", "111"),
                Customer.class
        ).getBody();
    }

    private Product createProduct(String sku, String price, int stock) {
        return rest.postForEntity(
                "/api/products",
                new CreateProductRequest("P-" + sku, sku, new BigDecimal(price), stock),
                Product.class
        ).getBody();
    }

    @Test
    void shouldCreateConfirmedOrder_whenRequestIsValid() {
        Customer c = createCustomer();
        Product p1 = createProduct("SKU-A", "100.00", 50);
        Product p2 = createProduct("SKU-B", "20.00", 30);

        var request = new CreateOrderRequest(c.getId(), List.of(
                new CreateOrderRequest.ItemRequest(p1.getId(), 2),
                new CreateOrderRequest.ItemRequest(p2.getId(), 3)
        ));

        ResponseEntity<Order> response = rest.postForEntity("/api/orders", request, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isPositive();
        assertThat(response.getBody().getTotalAmount()).isEqualByComparingTo("260.00");

        // Stock was decremented
        Product reloadedP1 = productRepository.findById(p1.getId()).orElseThrow();
        Product reloadedP2 = productRepository.findById(p2.getId()).orElseThrow();
        assertThat(reloadedP1.getStockQuantity()).isEqualTo(48);
        assertThat(reloadedP2.getStockQuantity()).isEqualTo(27);
    }

    @Test
    void shouldReturnNotFound_whenCustomerDoesNotExist() {
        Product p = createProduct("SKU-X", "10.00", 5);

        var request = new CreateOrderRequest(99_999L, List.of(
                new CreateOrderRequest.ItemRequest(p.getId(), 1)
        ));

        ResponseEntity<String> response = rest.postForEntity("/api/orders", request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFound_whenProductDoesNotExist() {
        Customer c = createCustomer();

        var request = new CreateOrderRequest(c.getId(), List.of(
                new CreateOrderRequest.ItemRequest(99_999L, 1)
        ));

        ResponseEntity<String> response = rest.postForEntity("/api/orders", request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnConflict_whenStockIsInsufficient() {
        Customer c = createCustomer();
        Product p = createProduct("SKU-LOW", "10.00", 1);

        var request = new CreateOrderRequest(c.getId(), List.of(
                new CreateOrderRequest.ItemRequest(p.getId(), 5)
        ));

        ResponseEntity<String> response = rest.postForEntity("/api/orders", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Estoque");

        // Stock must NOT have changed (transactional rollback)
        Product reloaded = productRepository.findById(p.getId()).orElseThrow();
        assertThat(reloaded.getStockQuantity()).isEqualTo(1);
    }

    @Test
    void shouldReturnBadRequest_whenItemsListIsEmpty() {
        Customer c = createCustomer();
        // The DTO @NotEmpty validation triggers before the service
        var request = new CreateOrderRequest(c.getId(), List.of());

        ResponseEntity<String> response = rest.postForEntity("/api/orders", request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
