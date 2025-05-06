//package com.example.Badminton.controller;
//
//import com.example.Badminton.dto.ProductDTO;
//import com.example.Badminton.service.ProductService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Controller
//public class ProductController {
//    @Autowired
//    private ProductService productService;
//
//    @GetMapping("/api/products")
//    @ResponseBody
//    public ResponseEntity<List<ProductDTO>> getAllProducts() {
//        List<ProductDTO> products = productService.getAllProducts();
//        return ResponseEntity.ok(products);
//    }
//
//    @GetMapping("/detail")
//    public String getProductDetail(@RequestParam("id") Integer id, Model model) {
//        ProductDTO product = productService.getProductById(id);
//        model.addAttribute("product", product);
//        return "/detail"; // Trả về tên template quanly.html
//    }
//}