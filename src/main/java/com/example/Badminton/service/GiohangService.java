package com.example.Badminton.service;

import com.example.Badminton.entity.Cart;
import com.example.Badminton.entity.CartItem;
import com.example.Badminton.entity.Customer;
import com.example.Badminton.entity.Inventory;
import com.example.Badminton.repository.CartItemRepository;
import com.example.Badminton.repository.CartRepository;
import com.example.Badminton.repository.CustomerRepository;
import com.example.Badminton.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GiohangService {

    private static final Logger logger = LoggerFactory.getLogger(GiohangService.class);

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    public boolean checkStock(Integer productId, Integer quantity) {
        logger.info("Checking stock for productId: {}, quantity: {}", productId, quantity);
        Optional<Inventory> inventoryOpt = inventoryRepository.findById(productId);
        if (!inventoryOpt.isPresent()) {
            logger.error("Product with ID {} does not exist in inventory", productId);
            throw new RuntimeException("Sản phẩm không tồn tại");
        }
        boolean isAvailable = inventoryOpt.get().getQuantity() >= quantity;
        logger.info("Stock check result: {}", isAvailable);
        return isAvailable;
    }

    @Transactional
    public Map<String, Object> addToCart(String email, Integer productId, Integer quantity) {
        logger.info("Adding to cart: email={}, productId={}, quantity={}", email, productId, quantity);
        try {
            if (!checkStock(productId, quantity)) {
                logger.warn("Insufficient stock for productId: {}", productId);
                return Map.of("success", false, "message", "Sản phẩm không đủ hàng");
            }

            Optional<Customer> customerOpt = customerRepository.findByEmail(email);
            if (!customerOpt.isPresent()) {
                logger.error("Customer with email {} not found", email);
                return Map.of("success", false, "message", "Khách hàng không tồn tại");
            }
            Customer customer = customerOpt.get();
            logger.info("Found customer: ID={}", customer.getId());

            Optional<Cart> cartOpt = cartRepository.findByCustomerIdAndStatus(customer.getId(), "ACTIVE");
            Cart cart;
            if (cartOpt.isPresent()) {
                cart = cartOpt.get();
                logger.info("Found active cart: ID={}", cart.getId());
            } else {
                cart = new Cart();
                cart.setCustomer(customer);
                cart.setStatus("ACTIVE");
                cart.setCreatedAt(LocalDateTime.now());
                cart = cartRepository.save(cart);
                logger.info("Created new cart: ID={}", cart.getId());
            }

            Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
            if (existingItemOpt.isPresent()) {
                CartItem existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                cartItemRepository.save(existingItem);
                logger.info("Updated cart item: productId={}, new quantity={}", productId, existingItem.getQuantity());
            } else {
                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setProductId(productId);
                cartItem.setQuantity(quantity);
                cartItem.setCreatedAt(LocalDateTime.now());
                cartItemRepository.save(cartItem);
                logger.info("Added new cart item: productId={}, quantity={}", productId, quantity);
            }

            return Map.of("success", true, "message", "Thêm vào giỏ hàng thành công");
        } catch (Exception e) {
            logger.error("Error adding to cart: {}", e.getMessage(), e);
            return Map.of("success", false, "message", "Lỗi khi thêm vào giỏ hàng: " + e.getMessage());
        }
    }

    public void saveGuestCart(List<Map<String, Object>> cartItems) {
        logger.info("Saving guest cart: {}", cartItems);
        // Logic lưu giỏ hàng tạm thời vào server (nếu cần)
    }
}