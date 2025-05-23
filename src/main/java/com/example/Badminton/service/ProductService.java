package com.example.Badminton.service;

import com.example.Badminton.dto.ProductDTO;
import com.example.Badminton.entity.Product;
import com.example.Badminton.entity.ProductImage;
import com.example.Badminton.repository.ProductImageRepository;
import com.example.Badminton.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    public Page<ProductDTO> getPagedProducts(Pageable pageable, String keyword, String category) {
        Page<Product> productPage;
        if (keyword != null && !keyword.isEmpty() && category != null && !category.isEmpty()) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndTypeName(keyword, category, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (category != null && !category.isEmpty()) {
            productPage = productRepository.findByTypeName(category, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        return productPage.map(this::convertToDTO);
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tìm thấy"));
        return convertToDTO(product);
    }

    private ProductDTO convertToDTO(Product product) {
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