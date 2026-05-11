package com.vitorcamprubi.OMS_Lite.integration;

import com.vitorcamprubi.OMS_Lite.domain.Customer;
import com.vitorcamprubi.OMS_Lite.dto.customer.CreateCustomerRequest;
import com.vitorcamprubi.OMS_Lite.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerIT extends AbstractMySqlContainerTest {

    @Autowired
    TestRestTemplate rest;

    @Autowired
    CustomerRepository customerRepository;

    @BeforeEach
    @Transactional
    void cleanState() {
        customerRepository.deleteAll();
    }

    @Test
    void shouldCreateCustomer_whenPostValidPayload() {
        var request = new CreateCustomerRequest("Joana", "joana@example.com", "12345678900");

        ResponseEntity<Customer> response = rest.postForEntity("/api/customers", request, Customer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("joana@example.com");
    }

    @Test
    void shouldReturnConflict_whenPostDuplicateEmail() {
        var request = new CreateCustomerRequest("Joana", "dup@example.com", "111");
        rest.postForEntity("/api/customers", request, Customer.class);

        var dup = new CreateCustomerRequest("Outra", "dup@example.com", "222");
        ResponseEntity<String> response = rest.postForEntity("/api/customers", dup, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("email");
    }

    @Test
    void shouldReturnBadRequest_whenPostInvalidEmail() {
        String invalid = """
                { "name": "x", "email": "not-an-email", "document": "1" }
                """;

        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = rest.postForEntity(
                "/api/customers",
                new org.springframework.http.HttpEntity<>(invalid, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnNotFound_whenGetByMissingId() {
        ResponseEntity<String> response = rest.getForEntity("/api/customers/9999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
