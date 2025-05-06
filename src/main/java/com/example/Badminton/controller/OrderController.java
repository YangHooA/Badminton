//package com.example.Badminton.controller;
//
//import com.example.Badminton.dto.OrderDTO;
//import com.example.Badminton.service.OrderService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Collections;
//import java.util.List;
//
//@Controller
//public class OrderController {
//
//    @Autowired
//    private OrderService orderService;
//
//    // Trang hiển thị danh sách đơn hàng
//    @GetMapping("/quanly/donhang")
//    public String showOrderList(@RequestParam(defaultValue = "0") int page,
//                                @RequestParam(defaultValue = "10") int size,
//                                @RequestParam(required = false) String status,
//                                @RequestParam(required = false) String keyword,
//                                Model model) {
//        try {
//            Pageable pageable = PageRequest.of(page, size);
//            Page<OrderDTO> orderPage = orderService.getOrdersByStatus(status, keyword, pageable);
//            model.addAttribute("orders", orderPage.getContent());
//            model.addAttribute("currentPage", orderPage.getNumber());
//            model.addAttribute("totalPages", orderPage.getTotalPages());
//            model.addAttribute("status", status);
//            model.addAttribute("keyword", keyword);
//            return "quanly"; // Trả về trang quanly.html
//        } catch (Exception e) {
//            model.addAttribute("error", "Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
//            return "quanly";
//        }
//    }
//
//    // API trả về danh sách đơn hàng dạng JSON
//    @GetMapping("/api/orders")
//    @ResponseBody
//    public List<OrderDTO> getAllOrders() {
//        return orderService.getAllOrders();
//    }
//
//    // API lấy chi tiết đơn hàng
//    @GetMapping("/api/orders/{id}")
//    @ResponseBody
//    public OrderDTO getOrderById(@PathVariable Long id) {
//        return orderService.getOrderById(id);
//    }
//
//    // API xóa đơn hàng
//    @DeleteMapping("/api/orders/{id}")
//    @ResponseBody
//    public void deleteOrder(@PathVariable Long id) {
//        orderService.deleteOrder(id);
//    }
//
//    @GetMapping("/api/orders/paged")
//    @ResponseBody
//    public ResponseEntity<?> getPagedOrders(@RequestParam(defaultValue = "0") int page,
//                                            @RequestParam(defaultValue = "10") int size,
//                                            @RequestParam(required = false) String status,
//                                            @RequestParam(required = false) String keyword) {
//        try {
//            Pageable pageable = PageRequest.of(page, size);
//            Page<OrderDTO> orderPage = orderService.getOrdersByStatus(status, keyword, pageable);
//            return ResponseEntity.ok(orderPage);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("message", "Lỗi khi tải danh sách đơn hàng: " + e.getMessage()));
//        }
//    }
//}