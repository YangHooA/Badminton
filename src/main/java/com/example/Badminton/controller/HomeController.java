package com.example.Badminton.controller;

import com.example.Badminton.dto.ProductDTO;
import com.example.Badminton.entity.Customer;
import com.example.Badminton.service.CustomerService;
import com.example.Badminton.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/")
    public String home(Model model, OAuth2AuthenticationToken authentication) {
        // Lấy danh sách sản phẩm
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);

        // Kiểm tra trạng thái đăng nhập
        if (authentication != null) {
            OAuth2User oAuth2User = authentication.getPrincipal();
            String providerId = oAuth2User.getAttribute("sub") != null ? oAuth2User.getAttribute("sub") : oAuth2User.getAttribute("id");
            String provider = oAuth2User.getAttribute("sub") != null ? "google" : "facebook";

            if (providerId == null) {
                model.addAttribute("error", "Không lấy được providerId từ OAuth2User");
                return "body";
            }

            Optional<Customer> userOpt = customerService.findByGoogleIdAndProvider(providerId, provider);
            if (userOpt.isPresent()) {
                Customer loggedInUser = userOpt.get();
                model.addAttribute("loggedInUser", loggedInUser);
                if ("ADMIN".equals(loggedInUser.getRole())) {
                    return "redirect:/quanly"; // Chuyển hướng ADMIN đến trang quản lý
                }
            } else {
                // Tạo mới khách hàng nếu chưa tồn tại
                String name = oAuth2User.getAttribute("name");
                String email = oAuth2User.getAttribute("email");
                String picture = oAuth2User.getAttribute("picture") != null ? oAuth2User.getAttribute("picture") : "default-picture.jpg";
                Customer newCustomer = customerService.findOrCreateCustomer(providerId, name, email, picture, provider);
                model.addAttribute("loggedInUser", newCustomer);
            }
        }

        return "body"; // USER hoặc không đăng nhập vào trang chủ
    }

    // Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true); // Set thành true nếu dùng HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        return "redirect:/";
    }

    @GetMapping("/register")
    public String showRegisterForm(@RequestParam String providerId, @RequestParam String provider, Model model) {
        Optional<Customer> customerOpt = customerService.findByGoogleIdAndProvider(providerId, provider);
        if (customerOpt.isPresent()) {
            model.addAttribute("providerId", providerId);
            model.addAttribute("provider", provider);
            return "register"; // Trả về trang register.html
        }
        return "redirect:/"; // Nếu không tìm thấy customer, quay về trang chủ
    }

    // Lưu thông tin hồ sơ
    @PostMapping("/save-profile")
    public String saveProfile(@RequestParam String providerId, @RequestParam String phone, @RequestParam String address, @RequestParam String provider) {
        Optional<Customer> customerOpt = customerService.findByGoogleIdAndProvider(providerId, provider);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.setPhone(phone);
            customer.setAddress(address);
            customerService.save(customer);
        }
        return "redirect:/";
    }



    @GetMapping("/check-auth")
    @ResponseBody
    public Map<String, Object> checkAuth(OAuth2AuthenticationToken authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication != null) {
            OAuth2User oAuth2User = authentication.getPrincipal();
            String providerId = oAuth2User.getAttribute("sub") != null ? oAuth2User.getAttribute("sub") : oAuth2User.getAttribute("id");
            String provider = oAuth2User.getAttribute("sub") != null ? "google" : "facebook";

            Optional<Customer> customerOpt = customerService.findByGoogleIdAndProvider(providerId, provider);
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                Map<String, String> user = new HashMap<>();
                user.put("name", customer.getName());
                user.put("picture", customer.getPicture());
                user.put("email", customer.getEmail());
                user.put("role", customer.getRole());
                response.put("authenticated", true);
                response.put("user", user);
                return response;
            }
        }
        response.put("authenticated", false);
        return response;
    }


}