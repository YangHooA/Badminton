package com.example.Badminton.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Getter
@Setter
@Entity
@Table(name = "skill_level")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillLevel {
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "note")
    private String note;

    @OneToMany(mappedBy = "skillLevel", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products;
}