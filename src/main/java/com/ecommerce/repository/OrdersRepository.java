package com.ecommerce.repository;

import com.ecommerce.entity.Customer;
import com.ecommerce.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByOrderId(String orderId);

    List<Orders> findByCustomer(Customer customer);
}
