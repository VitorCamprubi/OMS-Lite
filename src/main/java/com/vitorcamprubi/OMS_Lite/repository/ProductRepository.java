package com.vitorcamprubi.OMS_Lite.repository;

import com.vitorcamprubi.OMS_Lite.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
