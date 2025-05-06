//package com.example.Badminton.controller;
//
//import com.example.Badminton.dto.InventoryDTO;
//import com.example.Badminton.service.InventoryService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import java.util.List;
//
//@Controller
//public class InventoryController {
//
//    @Autowired
//    private InventoryService inventoryService;
//
//    // Trang hiển thị quản lý kho
//    @GetMapping("/quanly")
//    public String showInventoryPage(Model model) {
//        model.addAttribute("inventoryList", inventoryService.getAllInventory());
//        return "quanly"; // Trả về trang quanly.html
//    }
//
//    // API endpoint để trả về dữ liệu JSON
//    @GetMapping("/api/inventory")
//    @ResponseBody
//    public List<InventoryDTO> getAllInventory() {
//        return inventoryService.getAllInventory();
//    }
//}