package com.example.Badminton.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Getter
@Setter
@Entity
@Table(name = "types_of_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeOfProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "describle")
    private String description;

    @OneToMany(mappedBy = "type", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products;
}