package com.example.Badminton.repository;

import com.example.Badminton.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByCustomerIdAndStatus(Long customerId, String status);
}