package com.example.Badminton.service;

import com.example.Badminton.entity.*;
import com.example.Badminton.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;



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


    @Transactional
    public Map<String, Object> updateCartItemQuantity(String email, Integer productId, Integer quantity) {
        logger.info("Updating cart item: email={}, productId={}, quantity={}", email, productId, quantity);
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

            Optional<Cart> cartOpt = cartRepository.findByCustomerIdAndStatus(customer.getId(), "ACTIVE");
            if (!cartOpt.isPresent()) {
                logger.warn("No active cart found for customer: {}", email);
                return Map.of("success", false, "message", "Giỏ hàng không tồn tại");
            }
            Cart cart = cartOpt.get();

            Optional<CartItem> cartItemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
            if (!cartItemOpt.isPresent()) {
                logger.warn("Cart item not found: productId={}", productId);
                return Map.of("success", false, "message", "Sản phẩm không có trong giỏ hàng");
            }

            CartItem cartItem = cartItemOpt.get();
            cartItem.setQuantity(quantity);
            cartItem.setUpdatedAt(LocalDateTime.now());
            cartItemRepository.save(cartItem);
            logger.info("Updated cart item: productId={}, new quantity={}", productId, quantity);

            return Map.of("success", true, "message", "Cập nhật số lượng thành công");
        } catch (Exception e) {
            logger.error("Error updating cart item: {}", e.getMessage(), e);
            return Map.of("success", false, "message", "Lỗi khi cập nhật số lượng: " + e.getMessage());
        }
    }


    @Transactional
    public Map<String, Object> removeFromCart(String email, Integer productId) {
        logger.info("Removing from cart: email={}, productId={}", email, productId);
        try {
            Optional<Customer> customerOpt = customerRepository.findByEmail(email);
            if (!customerOpt.isPresent()) {
                logger.error("Customer with email {} not found", email);
                return Map.of("success", false, "message", "Khách hàng không tồn tại");
            }
            Customer customer = customerOpt.get();

            Optional<Cart> cartOpt = cartRepository.findByCustomerIdAndStatus(customer.getId(), "ACTIVE");
            if (!cartOpt.isPresent()) {
                logger.warn("No active cart found for customer: {}", email);
                return Map.of("success", false, "message", "Giỏ hàng không tồn tại");
            }
            Cart cart = cartOpt.get();

            Optional<CartItem> cartItemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
            if (!cartItemOpt.isPresent()) {
                logger.warn("Cart item not found: productId={}", productId);
                return Map.of("success", false, "message", "Sản phẩm không có trong giỏ hàng");
            }

            cartItemRepository.delete(cartItemOpt.get());
            logger.info("Removed cart item: productId={}", productId);
            return Map.of("success", true, "message", "Xóa sản phẩm khỏi giỏ hàng thành công");
        } catch (Exception e) {
            logger.error("Error removing from cart: {}", e.getMessage(), e);
            return Map.of("success", false, "message", "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
    }

    public void saveGuestCart(List<Map<String, Object>> cartItems) {
        logger.info("Saving guest cart: {}", cartItems);
        // Logic lưu giỏ hàng tạm thời vào server (nếu cần)
    }

    @Transactional
    public void syncGuestCart(String email, List<Map<String, Object>> guestCart) {
        logger.info("Syncing guest cart for email: {}", email);
        if (guestCart == null || guestCart.isEmpty()) {
            logger.info("Guest cart is empty, nothing to sync");
            return;
        }

        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if (!customerOpt.isPresent()) {
            logger.error("Customer with email {} not found", email);
            throw new RuntimeException("Khách hàng không tồn tại");
        }
        Customer customer = customerOpt.get();

        Optional<Cart> cartOpt = cartRepository.findByCustomerIdAndStatus(customer.getId(), "ACTIVE");
        Cart cart;
        if (cartOpt.isPresent()) {
            cart = cartOpt.get();
        } else {
            cart = new Cart();
            cart.setCustomer(customer);
            cart.setStatus("ACTIVE");
            cart.setCreatedAt(LocalDateTime.now());
            cart = cartRepository.save(cart);
        }

        for (Map<String, Object> item : guestCart) {
            Integer productId = (Integer) item.get("productId");
            Integer quantity = (Integer) item.get("quantity");

            if (productId == null || quantity == null || quantity <= 0) {
                logger.warn("Invalid guest cart item: productId={}, quantity={}", productId, quantity);
                continue;
            }

            if (!checkStock(productId, quantity)) {
                logger.warn("Insufficient stock for productId: {}", productId);
                continue;
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
        }
    }

    @Transactional
    public Map<String, Object> placeOrder(String email, String guestName, String guestEmail, String guestPhone, String guestAddress, String paymentMethod) {
        logger.info("Placing order: email={}, guestName={}, guestEmail={}, guestPhone={}, guestAddress={}, paymentMethod={}",
                email, guestName, guestEmail, guestPhone, guestAddress, paymentMethod);
        try {
            // Kiểm tra khách hàng
            Optional<Customer> customerOpt = email != null && !email.isEmpty() ?
                    customerRepository.findByEmail(email) : Optional.empty();
            Customer customer = customerOpt.orElse(null);

            // Lấy giỏ hàng
            Optional<Cart> cartOpt = customer != null ?
                    cartRepository.findByCustomerIdAndStatus(customer.getId(), "ACTIVE") :
                    Optional.empty();
            if (!cartOpt.isPresent() && customer != null) {
                logger.warn("No active cart found for customer: {}", email);
                return Map.of("success", false, "message", "Giỏ hàng không tồn tại");
            }

            List<CartItem> cartItems = cartOpt.isPresent() ? cartOpt.get().getCartItems() : new ArrayList<>();
            if (cartItems.isEmpty()) {
                logger.warn("Cart is empty for customer: {}", email);
                return Map.of("success", false, "message", "Giỏ hàng rỗng");
            }

            // Kiểm tra tồn kho
            for (CartItem item : cartItems) {
                if (!checkStock(item.getProductId(), item.getQuantity())) {
                    logger.warn("Insufficient stock for productId: {}", item.getProductId());
                    return Map.of("success", false, "message", "Sản phẩm không đủ hàng");
                }
            }

            // Tạo đơn hàng
            Order order = new Order();
            order.setCustomer(customer);
            order.setGuestName(guestName);
            order.setGuestEmail(guestEmail);
            order.setGuestPhone(guestPhone);
            order.setGuestAddress(guestAddress);
            order.setStatus(Order.OrderStatus.Pending);
            order.setCreatedAt(LocalDateTime.now());

            BigDecimal totalPrice = BigDecimal.ZERO;
            for (CartItem item : cartItems) {
                Optional<Inventory> inventoryOpt = inventoryRepository.findById(item.getProductId());
                if (!inventoryOpt.isPresent()) {
                    logger.error("Inventory not found for productId: {}", item.getProductId());
                    return Map.of("success", false, "message", "Sản phẩm không tồn tại");
                }
                Inventory inventory = inventoryOpt.get();
                Product product = inventory.getProduct();
                BigDecimal itemPrice = BigDecimal.valueOf(product.getPrice()); // Chuyển Double sang BigDecimal
                totalPrice = totalPrice.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            order.setTotalPrice(totalPrice);
            order = orderRepository.save(order);
            logger.info("Created order: ID={}", order.getId());

            // Tạo chi tiết đơn hàng
            for (CartItem item : cartItems) {
                Optional<Inventory> inventoryOpt = inventoryRepository.findById(item.getProductId());
                if (inventoryOpt.isPresent()) {
                    Inventory inventory = inventoryOpt.get();
                    Product product = inventory.getProduct();

                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setProduct(product); // Sử dụng setProduct thay vì setProductId
                    orderDetail.setQuantity(item.getQuantity());
                    orderDetail.setPrice(BigDecimal.valueOf(product.getPrice())); // Chuyển Double sang BigDecimal
                    orderDetailRepository.save(orderDetail);

                    // Cập nhật tồn kho
                    inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
                    inventoryRepository.save(inventory);
                    logger.info("Updated inventory: productId={}, new quantity={}", item.getProductId(), inventory.getQuantity());
                }
            }

            // Xóa giỏ hàng sau khi đặt hàng
            if (cartOpt.isPresent()) {
                cartItemRepository.deleteAll(cartItems);
                cartRepository.delete(cartOpt.get());
                logger.info("Cleared cart for customer: {}", email);
            }

            return Map.of(
                    "success", true,
                    "message", "Đặt hàng thành công",
                    "orderId", order.getId()
            );
        } catch (Exception e) {
            logger.error("Error placing order: {}", e.getMessage(), e);
            return Map.of("success", false, "message", "Lỗi khi đặt hàng: " + e.getMessage());
        }
    }

}