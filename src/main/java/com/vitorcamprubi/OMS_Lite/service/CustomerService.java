package com.vitorcamprubi.OMS_Lite.service;

import com.vitorcamprubi.OMS_Lite.domain.Customer;
import com.vitorcamprubi.OMS_Lite.dto.customer.CreateCustomerRequest;
import com.vitorcamprubi.OMS_Lite.exception.BusinessRuleException;
import com.vitorcamprubi.OMS_Lite.exception.ResourceNotFoundException;
import com.vitorcamprubi.OMS_Lite.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer create(CreateCustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw BusinessRuleException.duplicate("email", request.email());
        }
        if (customerRepository.existsByDocument(request.document())) {
            throw BusinessRuleException.duplicate("document", request.document());
        }

        Customer customer = new Customer();
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setDocument(request.document());
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.customer(id));
    }
}
