package com.example.Badminton.controller;

import com.example.Badminton.dto.CustomerDTO;
import com.example.Badminton.dto.InventoryDTO;
import com.example.Badminton.dto.OrderDTO;
import com.example.Badminton.dto.ProductDTO;
import com.example.Badminton.service.CustomerService;
import com.example.Badminton.service.InventoryService;
import com.example.Badminton.service.OrderService;
import com.example.Badminton.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
public class ManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ManagementController.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private InventoryService inventoryService;


    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @GetMapping("/quanly")
    public String showManagementPage(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("inventoryList", inventoryService.getAllInventory());
        return "quanly";
    }

    @GetMapping("/quanly/donhang")
    public String showOrderList(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String keyword,
                                Model model) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDTO> orderPage = orderService.getOrdersByStatus(status, keyword, pageable);
            model.addAttribute("orders", orderPage.getContent());
            model.addAttribute("currentPage", orderPage.getNumber());
            model.addAttribute("totalPages", orderPage.getTotalPages());
            model.addAttribute("status", status);
            model.addAttribute("keyword", keyword);
            return "quanly";
        } catch (Exception e) {
            logger.error("Error loading order list: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
            return "quanly";
        }
    }

    @GetMapping("/detail")
    public String getProductDetail(@RequestParam("id") Integer id, Model model) {
        try {
            ProductDTO product = productService.getProductById(id);
            if (product == null) {
                logger.warn("Product with ID {} not found", id);
                model.addAttribute("error", "Sản phẩm không tồn tại!");
                return "error";
            }
            model.addAttribute("product", product);
            return "detail";
        } catch (Exception e) {
            logger.error("Error loading product detail for ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải chi tiết sản phẩm: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/api/customers")
    @ResponseBody
    public List<CustomerDTO> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/api/inventory")
    @ResponseBody
    public List<InventoryDTO> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/orders")
    @ResponseBody
    public List<OrderDTO> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/api/orders/paged")
    @ResponseBody
    public ResponseEntity<?> getPagedOrders(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String keyword) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDTO> orderPage = orderService.getOrdersByStatus(status, keyword, pageable);
            return ResponseEntity.ok(orderPage);
        } catch (Exception e) {
            logger.error("Error loading paged orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Lỗi khi tải danh sách đơn hàng: " + e.getMessage()));
        }
    }

    @GetMapping("/api/orders/{id}")
    @ResponseBody
    public OrderDTO getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @DeleteMapping("/api/orders/{id}")
    @ResponseBody
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }
}