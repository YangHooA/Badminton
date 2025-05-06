package com.example.Badminton.controller;

import com.example.Badminton.dto.CustomerDTO;
import com.example.Badminton.dto.InventoryDTO;
import com.example.Badminton.dto.OrderDTO;
import com.example.Badminton.dto.ProductDTO;
import com.example.Badminton.service.CustomerService;
import com.example.Badminton.service.InventoryService;
import com.example.Badminton.service.OrderService;
import com.example.Badminton.service.ProductService;
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

    @Autowired
    private CustomerService customerService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    // Trang hiển thị quản lý tổng hợp (khách hàng, kho, đơn hàng)
    @GetMapping("/quanly")
    public String showManagementPage(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("inventoryList", inventoryService.getAllInventory());
        return "quanly"; // Trả về trang quanly.html
    }

    // Trang hiển thị danh sách đơn hàng
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
            return "quanly"; // Trả về trang quanly.html
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
            return "quanly";
        }
    }

    // Trang hiển thị chi tiết sản phẩm
    @GetMapping("/detail")
    public String getProductDetail(@RequestParam("id") Integer id, Model model) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "detail"; // Trả về trang detail.html
    }

    // API trả về danh sách khách hàng
    @GetMapping("/api/customers")
    @ResponseBody
    public List<CustomerDTO> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    // API trả về danh sách kho
    @GetMapping("/api/inventory")
    @ResponseBody
    public List<InventoryDTO> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    // API trả về danh sách sản phẩm
    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // API trả về danh sách đơn hàng
    @GetMapping("/api/orders")
    @ResponseBody
    public List<OrderDTO> getAllOrders() {
        return orderService.getAllOrders();
    }

    // API trả về danh sách đơn hàng phân trang
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Lỗi khi tải danh sách đơn hàng: " + e.getMessage()));
        }
    }

    // API lấy chi tiết đơn hàng
    @GetMapping("/api/orders/{id}")
    @ResponseBody
    public OrderDTO getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    // API xóa đơn hàng
    @DeleteMapping("/api/orders/{id}")
    @ResponseBody
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }
}