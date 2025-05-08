package com.example.Badminton.controller;

import com.example.Badminton.entity.*;
import com.example.Badminton.repository.CartRepository;
import com.example.Badminton.repository.CustomerRepository;
import com.example.Badminton.repository.InventoryRepository;
import com.example.Badminton.service.GiohangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class GiohangController {

    @Autowired
    private GiohangService giohangService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/check-stock")
    public ResponseEntity<?> checkStock(@RequestBody CartItemRequest request) {
        try {
            boolean isAvailable = giohangService.checkStock(request.getProductId(), request.getQuantity());
            return ResponseEntity.ok().body(Map.of("success", true, "isAvailable", isAvailable));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

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
        return (boolean) result.get("success") ? ResponseEntity.ok().body(result) : ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestBody Map<String, Integer> request) {
        Integer productId = request.get("productId");
        if (productId == null || productId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "ID sản phẩm không hợp lệ"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Chưa đăng nhập"));
        }

        String email = getEmailFromAuthentication(authentication);
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Không thể xác định email người dùng"));
        }

        Map<String, Object> result = giohangService.removeFromCart(email, productId);
        if ((boolean) result.get("success")) {
            Optional<Customer> customerOpt = customerRepository.findByEmail(email);
            if (customerOpt.isPresent()) {
                Optional<Cart> cartOpt = cartRepository.findByCustomerIdAndStatus(customerOpt.get().getId(), "ACTIVE");
                int itemCount = cartOpt.map(cart -> cart.getCartItems().size()).orElse(0);
                result.put("itemCount", itemCount);
            }
        }
        return (boolean) result.get("success") ? ResponseEntity.ok().body(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/items")
    public ResponseEntity<?> getCartItems() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                return ResponseEntity.ok().body(Map.of("success", false, "message", "Chưa đăng nhập", "items", List.of()));
            }

            String email = getEmailFromAuthentication(authentication);
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Không thể xác định email người dùng"));
            }

            System.out.println("Authentication type: " + authentication.getClass().getSimpleName());
            System.out.println("Principal: " + authentication.getPrincipal().getClass().getSimpleName());

            Optional<Customer> customerOpt = customerRepository.findByEmail(email);
            Customer customer;
            if (!customerOpt.isPresent()) {
                customer = new Customer();
                customer.setEmail(email);
                customer.setName("Guest");
                customer.setRole("USER");
                customer.setCreatedAt(LocalDateTime.now());
                customer = customerRepository.save(customer);
            } else {
                customer = customerOpt.get();
            }

            Optional<Cart> cartOpt = cartRepository.findByCustomerIdAndStatus(customer.getId(), "ACTIVE");
            if (!cartOpt.isPresent()) {
                return ResponseEntity.ok().body(Map.of("success", true, "message", "Giỏ hàng rỗng", "items", List.of()));
            }

            Cart cart = cartOpt.get();
            List<CartItem> cartItems = cart.getCartItems();
            List<Map<String, Object>> cartItemDetails = cartItems.stream()
                    .map(item -> {
                        Optional<Inventory> inventoryOpt = inventoryRepository.findById(item.getProductId());
                        if (inventoryOpt.isPresent()) {
                            Product product = inventoryOpt.get().getProduct();
                            Map<String, Object> map = new HashMap<>();
                            map.put("productId", item.getProductId());
                            map.put("name", product.getName());
                            map.put("price", product.getPrice());
                            map.put("image", product.getImageUrl() != null ? product.getImageUrl() : "default-image.jpg");
                            map.put("description", product.getDescription() != null ? product.getDescription() : "");
                            map.put("quantity", item.getQuantity());
                            return map;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ResponseEntity.ok().body(Map.of("success", true, "items", cartItemDetails));
        } catch (Exception e) {
            System.err.println("Error in getCartItems: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi khi lấy giỏ hàng: " + e.getMessage()));
        }
    }

    private String getEmailFromAuthentication(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();
            return oauth2User.getAttribute("email");
        } else if (authentication.getPrincipal() instanceof Customer) {
            return ((Customer) authentication.getPrincipal()).getEmail();
        } else {
            return authentication.getName();
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