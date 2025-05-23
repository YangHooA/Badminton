package com.example.Badminton.repository;

import com.example.Badminton.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.type " +
            "LEFT JOIN FETCH p.playStyle " +
            "LEFT JOIN FETCH p.skillLevel " +
            "LEFT JOIN FETCH p.weight")
    List<Product> findAllWithAllDetails();

    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.type.name = :typeName")
    Page<Product> findByTypeName(@Param("typeName") String typeName, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.type.name = :typeName")
    Page<Product> findByNameContainingIgnoreCaseAndTypeName(
            @Param("name") String name,
            @Param("typeName") String typeName,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:query%")
    List<Product> searchByName(@Param("query") String query);
}