package com.example.Badminton.repository;

import com.example.Badminton.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findAllByOrderCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}