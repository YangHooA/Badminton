package com.example.Badminton.controller;

import com.example.Badminton.service.GiohangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class GiohangController {

    @Autowired
    private GiohangService giohangService;

    // API kiểm tra tồn kho
    @PostMapping("/check-stock")
    public ResponseEntity<?> checkStock(@RequestBody CartItemRequest request) {
        try {
            boolean isAvailable = giohangService.checkStock(request.getProductId(), request.getQuantity());
            return ResponseEntity.ok().body(Map.of("success", true, "message", "Sản phẩm có đủ hàng"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // API thêm sản phẩm vào giỏ hàng cho khách đăng nhập
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItemRequest request) {
        if (request.getProductId() == null || request.getProductId() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "ID sản phẩm không hợp lệ"));
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số lượng không hợp lệ"));
        }
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email không hợp lệ"));
        }

        Map<String, Object> result = giohangService.addToCart(request.getEmail(), request.getProductId(), request.getQuantity());
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok().body(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }



}

class CartItemRequest {
    private Integer productId;
    private Integer quantity;
    private String email;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}