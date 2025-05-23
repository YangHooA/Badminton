package com.example.Badminton.controller;

import com.example.Badminton.entity.*;
import com.example.Badminton.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/thanhtoan")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private PayOS payOS;

    @PostMapping(value = "/payos", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Transactional
    public Map<String, Object> createPayOSPayment(
            @RequestBody Map<String, Object> orderData,
            @AuthenticationPrincipal OAuth2User oAuth2User,
            HttpServletRequest request,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Kiểm tra dữ liệu đầu vào
            String customerName = (String) orderData.get("customerName");
            String customerPhone = (String) orderData.get("customerPhone");
            String customerAddress = (String) orderData.get("customerAddress");
            Number totalAmountNumber = (Number) orderData.get("totalAmount");
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");

            if (customerName == null || customerPhone == null || customerAddress == null ||
                    totalAmountNumber == null || items == null || items.isEmpty()) {
                throw new IllegalArgumentException("Dữ liệu đơn hàng không đầy đủ");
            }

            int totalAmount = totalAmountNumber.intValue();
            if (totalAmount <= 0) {
                throw new IllegalArgumentException("Tổng tiền phải lớn hơn 0");
            }

            // Kiểm tra tổng tiền
            int calculatedTotal = items.stream()
                    .mapToInt(item -> {
                        Number quantity = (Number) item.get("quantity");
                        Number price = (Number) item.get("price");
                        if (quantity == null || price == null) {
                            throw new IllegalArgumentException("Thông tin sản phẩm không hợp lệ");
                        }
                        return quantity.intValue() * price.intValue();
                    })
                    .sum();
            if (calculatedTotal != totalAmount) {
                throw new IllegalArgumentException("Tổng tiền không khớp với danh sách sản phẩm");
            }

            // Lấy thông tin người dùng
            Customer customer = null;
            if (oAuth2User != null) {
                String providerId = oAuth2User.getAttribute("sub") != null ? oAuth2User.getAttribute("sub") : oAuth2User.getAttribute("id");
                String provider = oAuth2User.getAttribute("sub") != null ? "google" : "facebook";

            } else if (request.getUserPrincipal() != null) {
                String emailOrPhone = request.getUserPrincipal().getName();
                customer = customerRepository.findByEmail(emailOrPhone).orElse(null);

            }

            // Tạo đơn hàng
            Order order = new Order();
            order.setCreatedAt(LocalDateTime.now());
            order.setTotalPrice(new BigDecimal(totalAmount));
            order.setStatus(Order.OrderStatus.Pending);
            order.setGuestName(customerName);
            order.setGuestPhone(customerPhone);
            order.setGuestAddress(customerAddress);
            order.setCustomer(customer);

            order = orderRepository.save(order);
            logger.info("Order saved with ID: {}", order.getId());

            // Lưu chi tiết đơn hàng
            for (Map<String, Object> item : items) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(order);
                Number productIdObj = (Number) item.get("productId");
                if (productIdObj == null) {
                    throw new IllegalArgumentException("ID sản phẩm không hợp lệ");
                }
                int productId = productIdObj.intValue();
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại: " + productId));
                orderDetail.setProduct(product);
                Number quantityObj = (Number) item.get("quantity");
                Number priceObj = (Number) item.get("price");
                if (quantityObj == null || priceObj == null) {
                    throw new IllegalArgumentException("Số lượng hoặc giá không hợp lệ");
                }
                orderDetail.setQuantity(quantityObj.intValue());
                orderDetail.setPrice(new BigDecimal(priceObj.doubleValue()));
                orderDetailRepository.save(orderDetail);
            }

            // Chuẩn bị dữ liệu thanh toán PayOS
            String baseUrl = getBaseUrl(request);
            String returnUrl = baseUrl + "/thanhtoan/success?orderId=" + order.getId();
            String cancelUrl = baseUrl + "/thanhtoan/cancel";

            List<ItemData> itemList = new ArrayList<>();
            for (Map<String, Object> item : items) {
                Number productIdObj = (Number) item.get("productId");
                Number quantityObj = (Number) item.get("quantity");
                Number priceObj = (Number) item.get("price");
                if (productIdObj == null || quantityObj == null || priceObj == null) {
                    throw new IllegalArgumentException("Thông tin sản phẩm không hợp lệ");
                }
                Product product = productRepository.findById(productIdObj.intValue()).orElse(null);
                String productName = product != null ? product.getName() : "Sản phẩm ID: " + productIdObj.intValue();
                itemList.add(ItemData.builder()
                        .name(productName)
                        .quantity(quantityObj.intValue())
                        .price(priceObj.intValue())
                        .build());
            }

            String description = "ĐH #" + order.getId();
            if (description.length() > 25) {
                description = description.substring(0, 25);
            }

            long orderCode = Long.parseLong(String.valueOf(order.getId()) + new Random().nextInt(1000));

            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(totalAmount)
                    .description(description)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .items(itemList)
                    .build();

            // Tạo link thanh toán
            CheckoutResponseData data = payOS.createPaymentLink(paymentData);
            logger.info("PayOS payment link created: {}", data.getCheckoutUrl());

            response.put("success", true);
            response.put("checkoutUrl", data.getCheckoutUrl());
            response.put("orderId", order.getId());
        } catch (Exception e) {
            logger.error("Error creating payment link: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Không thể tạo link thanh toán: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/success")
    public String success(@RequestParam(value = "orderId", required = false) Long orderId, Model model) {
        try {
            if (orderId != null) {
                Order order = orderRepository.findById(orderId).orElse(null);
                if (order != null) {
                    order.setStatus(Order.OrderStatus.Completed);
                    orderRepository.save(order);
                    logger.info("Order {} marked as completed", orderId);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing success: {}", e.getMessage(), e);
        }
        model.addAttribute("page", "success");
        model.addAttribute("pageTitle", "Thanh toán thành công");
        return "payment";
    }

    @GetMapping("/cancel")
    public String cancel(Model model) {
        model.addAttribute("page", "cancel");
        model.addAttribute("pageTitle", "Thanh toán thất bại");
        return "payment";
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        String url = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            url += ":" + serverPort;
        }
        url += contextPath;
        return url;
    }
}