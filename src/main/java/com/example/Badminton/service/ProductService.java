package com.example.Badminton.service;

import com.example.Badminton.dto.ProductDTO;
import com.example.Badminton.entity.Product;
import com.example.Badminton.entity.ProductImage;
import com.example.Badminton.repository.ProductImageRepository;
import com.example.Badminton.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(product -> {
            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setPrice(product.getPrice());
            dto.setDescription(product.getDescription()); // Thêm description
            dto.setImageUrl(getFirstImageUrl(product.getId()));
            dto.setTypeName(product.getType().getName());
            dto.setPlayStyleName(product.getPlayStyle() != null ? product.getPlayStyle().getName() : null);
            dto.setSkillLevelName(product.getSkillLevel() != null ? product.getSkillLevel().getName() : null);
            dto.setWeightName(product.getWeight() != null ? product.getWeight().getName() : null);
            return dto;
        }).collect(Collectors.toList());
    }

    public ProductDTO getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(getFirstImageUrl(product.getId()));
        dto.setTypeName(product.getType().getName());
        dto.setPlayStyleName(product.getPlayStyle() != null ? product.getPlayStyle().getName() : null);
        dto.setSkillLevelName(product.getSkillLevel() != null ? product.getSkillLevel().getName() : null);
        dto.setWeightName(product.getWeight() != null ? product.getWeight().getName() : null);
        return dto;
    }

    public String getFirstImageUrl(Integer productId) {
        List<ProductImage> images = productImageRepository.findByProductId(productId);
        return images.isEmpty() ? "default-image.jpg" : images.get(0).getImageUrl();
    }
}