package com.example.Badminton.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(name = "description")
    private String description; // Thêm trường description

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private TypeOfProduct type;

    @ManyToOne
    @JoinColumn(name = "play_style_id")
    private PlayStyle playStyle;

    @ManyToOne
    @JoinColumn(name = "skill_level_id")
    private SkillLevel skillLevel;

    @ManyToOne
    @JoinColumn(name = "weight_id")
    private Weight weight;

    public String getImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getImageUrl(); // Lấy imageUrl từ ảnh đầu tiên
        }
        return null;
    }

    // Getters và Setters
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TypeOfProduct getType() {
        return type;
    }

    public void setType(TypeOfProduct type) {
        this.type = type;
    }

    public PlayStyle getPlayStyle() {
        return playStyle;
    }

    public void setPlayStyle(PlayStyle playStyle) {
        this.playStyle = playStyle;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }

    public Weight getWeight() {
        return weight;
    }

    public void setWeight(Weight weight) {
        this.weight = weight;
    }

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();
}