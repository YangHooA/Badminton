package com.example.Badminton.dto;

public class InventoryDTO {
    private int productId;
    private String productName;
    private int quantity;
    private String lastUpdated;

    // Constructor
    public InventoryDTO(int productId, String productName, int quantity, String lastUpdated) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
