package com.example.Badminton.repository;

import com.example.Badminton.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}