package com.example.Badminton.repository;

import com.example.Badminton.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByGoogleId(String googleId);
    Optional<Customer> findByGoogleIdAndProvider(String googleId, String provider);
}