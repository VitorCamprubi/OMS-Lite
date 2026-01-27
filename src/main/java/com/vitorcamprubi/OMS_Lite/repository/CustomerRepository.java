package com.vitorcamprubi.OMS_Lite.repository;

import com.vitorcamprubi.OMS_Lite.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
