package com.example.Badminton.repository;

import com.example.Badminton.entity.Customer;
import com.example.Badminton.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.type " +
            "LEFT JOIN FETCH p.playStyle " +
            "LEFT JOIN FETCH p.skillLevel " +
            "LEFT JOIN FETCH p.weight")
    List<Product> findAllWithAllDetails();

}
