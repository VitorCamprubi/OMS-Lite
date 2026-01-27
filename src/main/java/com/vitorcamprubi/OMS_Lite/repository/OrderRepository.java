package com.vitorcamprubi.OMS_Lite.repository;

import com.vitorcamprubi.OMS_Lite.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
