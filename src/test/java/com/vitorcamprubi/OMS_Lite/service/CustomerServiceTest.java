package com.vitorcamprubi.OMS_Lite.service;

import com.vitorcamprubi.OMS_Lite.domain.Customer;
import com.vitorcamprubi.OMS_Lite.dto.customer.CreateCustomerRequest;
import com.vitorcamprubi.OMS_Lite.exception.BusinessRuleException;
import com.vitorcamprubi.OMS_Lite.exception.ResourceNotFoundException;
import com.vitorcamprubi.OMS_Lite.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldCreateCustomer_whenRequestIsValid() {
        var request = new CreateCustomerRequest("Joana", "joana@example.com", "12345678900");

        when(customerRepository.existsByEmail(request.email())).thenReturn(false);
        when(customerRepository.existsByDocument(request.document())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer saved = customerService.create(request);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());

        assertThat(captor.getValue().getName()).isEqualTo("Joana");
        assertThat(captor.getValue().getEmail()).isEqualTo("joana@example.com");
        assertThat(captor.getValue().getDocument()).isEqualTo("12345678900");
        assertThat(saved).isNotNull();
    }

    @Test
    void shouldThrowBusinessRule_whenCreateWithDuplicateEmail() {
        var request = new CreateCustomerRequest("Joana", "dup@example.com", "12345678900");
        when(customerRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("email")
                .hasMessageContaining("dup@example.com");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessRule_whenCreateWithDuplicateDocument() {
        var request = new CreateCustomerRequest("Joana", "joana@example.com", "12345678900");
        when(customerRepository.existsByEmail("joana@example.com")).thenReturn(false);
        when(customerRepository.existsByDocument("12345678900")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("document")
                .hasMessageContaining("12345678900");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldReturnAllCustomers_whenFindAll() {
        Customer a = new Customer();
        a.setName("A");
        Customer b = new Customer();
        b.setName("B");
        when(customerRepository.findAll()).thenReturn(List.of(a, b));

        List<Customer> result = customerService.findAll();

        assertThat(result).hasSize(2).extracting(Customer::getName).containsExactly("A", "B");
    }

    @Test
    void shouldReturnCustomer_whenFindByIdExists() {
        Customer existing = new Customer();
        existing.setName("Joana");
        when(customerRepository.findById(7L)).thenReturn(Optional.of(existing));

        Customer found = customerService.findById(7L);

        assertThat(found.getName()).isEqualTo("Joana");
    }

    @Test
    void shouldThrowResourceNotFound_whenFindByIdNotExists() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
