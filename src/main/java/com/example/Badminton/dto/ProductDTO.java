package com.example.Badminton.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private Integer id;
    private String name;
    private Double price;
    private String imageUrl;
    private String typeName;
    private String playStyleName;
    private String skillLevelName;
    private String weightName;
    private String description; // Thêm trường description

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getPlayStyleName() {
        return playStyleName;
    }

    public void setPlayStyleName(String playStyleName) {
        this.playStyleName = playStyleName;
    }

    public String getSkillLevelName() {
        return skillLevelName;
    }

    public void setSkillLevelName(String skillLevelName) {
        this.skillLevelName = skillLevelName;
    }

    public String getWeightName() {
        return weightName;
    }

    public void setWeightName(String weightName) {
        this.weightName = weightName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}