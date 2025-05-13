package com.example.Badminton.controller;

import com.example.Badminton.entity.*;
import com.example.Badminton.repository.CartRepository;
import com.example.Badminton.repository.CustomerRepository;
import com.example.Badminton.repository.InventoryRepository;
import com.example.Badminton.service.GiohangService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class GiohangController {

    private static final Logger logger = LoggerFactory.getLogger(GiohangService.class);

    @Autowired
    private GiohangService giohangService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/check-stock")
    public ResponseEntity<?> checkStock(@RequestBody OrderRequest.CartItemRequest request) {
        try {
            boolean isAvailable = giohangService.checkStock(request.getProductId(), request.getQuantity());
            return ResponseEntity.ok().body(Map.of("success", true, "isAvailable", isAvailable));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody OrderRequest.CartItemRequest request) {
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

    @Transactional(readOnly = true)
    @GetMapping("/items")
    public ResponseEntity<?> getCartItems() {
        try {
            logger.info("Starting getCartItems");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                logger.warn("No authentication found");
                return ResponseEntity.ok().body(Map.of("success", false, "message", "Chưa đăng nhập", "items", List.of()));
            }

            String email = getEmailFromAuthentication(authentication);
            logger.info("Extracted email: {}", email);
            if (email == null || email.isEmpty()) {
                logger.warn("Invalid or empty email");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Không thể xác định email người dùng"));
            }

            Optional<Customer> customerOpt = customerRepository.findByEmail(email);
            Customer customer;
            if (!customerOpt.isPresent()) {
                logger.info("Creating new customer for email: {}", email);
                customer = new Customer();
                customer.setEmail(email);
                customer.setName("Guest");
                customer.setRole("USER");
                customer.setCreatedAt(LocalDateTime.now());
                customer = customerRepository.save(customer);
            } else {
                customer = customerOpt.get();
            }
            logger.info("Customer found or created: ID={}", customer.getId());

            Optional<Cart> cartOpt = cartRepository.findByCustomerIdAndStatus(customer.getId(), "ACTIVE");
            if (!cartOpt.isPresent()) {
                logger.info("No active cart found for customer: {}", email);
                return ResponseEntity.ok().body(Map.of("success", true, "message", "Giỏ hàng rỗng", "items", List.of()));
            }

            Cart cart = cartOpt.get();
            logger.info("Found active cart: ID={}", cart.getId());
            List<CartItem> cartItems = cart.getCartItems();
            List<Map<String, Object>> cartItemDetails = cartItems.stream()
                    .map(item -> {
                        try {
                            logger.info("Processing cart item: productId={}", item.getProductId());
                            Optional<Inventory> inventoryOpt = inventoryRepository.findById(item.getProductId());
                            if (inventoryOpt.isPresent()) {
                                Inventory inventory = inventoryOpt.get();
                                Product product = inventory.getProduct();
                                if (product == null) {
                                    logger.warn("Product is null for inventory ID: {}", item.getProductId());
                                    return null;
                                }
                                Map<String, Object> map = new HashMap<>();
                                map.put("productId", item.getProductId());
                                map.put("name", product.getName());
                                map.put("price", product.getPrice());
                                map.put("image", product.getImageUrl() != null ? product.getImageUrl() : "default-image.jpg");
                                map.put("description", product.getDescription() != null ? product.getDescription() : "");
                                map.put("quantity", item.getQuantity());
                                return map;
                            } else {
                                logger.warn("Inventory not found for productId: {}", item.getProductId());
                                return null;
                            }
                        } catch (Exception e) {
                            logger.error("Error processing cart item {}: {}", item.getProductId(), e.getMessage(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            logger.info("Returning {} cart items", cartItemDetails.size());
            return ResponseEntity.ok().body(Map.of("success", true, "items", cartItemDetails));
        } catch (Exception e) {
            logger.error("Unexpected error in getCartItems: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Lỗi server: " + e.getMessage()));
        }
    }


    private String getEmailFromAuthentication(Authentication authentication) {
        System.out.println("Authentication: " + authentication);
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();
            String email = oauth2User.getAttribute("email");
            System.out.println("OAuth2 email: " + email);
            return email;
        } else if (authentication.getPrincipal() instanceof Customer) {
            String email = ((Customer) authentication.getPrincipal()).getEmail();
            System.out.println("Customer email: " + email);
            return email;
        } else {
            String email = authentication.getName();
            System.out.println("Default email: " + email);
            return email;
        }
    }

    @PostMapping("/sync-guest-cart")
    public ResponseEntity<?> syncGuestCart(@RequestBody List<Map<String, Object>> guestCart, Authentication authentication) {
        try {
            String email = getEmailFromAuthentication(authentication);
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Không thể xác định email người dùng"));
            }

            giohangService.syncGuestCart(email, guestCart);
            return ResponseEntity.ok().body(Map.of("success", true, "message", "Đồng bộ giỏ hàng thành công"));
        } catch (Exception e) {
            logger.error("Error syncing guest cart: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi khi đồng bộ giỏ hàng: " + e.getMessage()));
        }
    }

    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request, Authentication authentication) {
        try {
            String email = authentication != null ? getEmailFromAuthentication(authentication) : null;
            Map<String, Object> result = giohangService.placeOrder(
                    email,
                    request.getGuestName(),
                    request.getGuestEmail(),
                    request.getGuestPhone(),
                    request.getGuestAddress(),
                    request.getPaymentMethod()
            );
            return (boolean) result.get("success") ?
                    ResponseEntity.ok().body(result) :
                    ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            logger.error("Error placing order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi khi đặt hàng: " + e.getMessage()));
        }
    }



}

class OrderRequest {
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String guestAddress;
    private String paymentMethod;

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }

    public String getGuestAddress() {
        return guestAddress;
    }

    public void setGuestAddress(String guestAddress) {
        this.guestAddress = guestAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
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
}