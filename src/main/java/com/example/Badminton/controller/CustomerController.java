//package com.example.Badminton.controller;
//
//import com.example.Badminton.dto.CustomerDTO;
//import com.example.Badminton.service.CustomerService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//import java.util.List;
//
//@Controller
//public class CustomerController {
//
//    @Autowired
//    private CustomerService customerService;
//
//    @GetMapping("/quanly")
//    public String showCustomerList(Model model) {
//        model.addAttribute("customers", customerService.getAllCustomers());
//        return "quanly";  // Trả về trang quanly.html
//    }
//
//    // Endpoint API trả về danh sách khách hàng dưới dạng JSON
//    @GetMapping("/api/customers")
//    public List<CustomerDTO> getAllCustomers() {
//        return customerService.getAllCustomers();
//    }
//
//
//
//}
