package com.example.Badminton.repository;

import com.example.Badminton.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE (:keyword IS NULL OR o.guestName LIKE %:keyword% OR CAST(o.id AS string) LIKE %:keyword%) " +
            "AND (:status IS NULL OR o.status = :status)")
    Page<Order> searchOrders(@Param("keyword") String keyword, @Param("status") Order.OrderStatus status, Pageable pageable);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}