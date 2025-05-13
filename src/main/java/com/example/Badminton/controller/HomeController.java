package com.example.Badminton.controller;

import com.example.Badminton.dto.ProductDTO;
import com.example.Badminton.entity.Customer;
import com.example.Badminton.securityconfig.JwtUtil;
import com.example.Badminton.service.CustomerService;
import com.example.Badminton.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/")
    public String home(Model model, OAuth2AuthenticationToken authentication) {
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);

        if (authentication != null) {
            OAuth2User oAuth2User = authentication.getPrincipal();
            String providerId = oAuth2User.getAttribute("sub") != null ? oAuth2User.getAttribute("sub") : oAuth2User.getAttribute("id");
            String provider = oAuth2User.getAttribute("sub") != null ? "google" : "facebook";

            if (providerId == null) {
                logger.warn("ProviderId is null for OAuth2User");
                model.addAttribute("error", "Không lấy được providerId từ OAuth2User");
                return "body";
            }

            Optional<Customer> userOpt = customerService.findByGoogleIdAndProvider(providerId, provider);
            if (userOpt.isPresent()) {
                Customer loggedInUser = userOpt.get();
                model.addAttribute("loggedInUser", loggedInUser);
                if ("ADMIN".equals(loggedInUser.getRole())) {
                    return "redirect:/quanly";
                }
            } else {
                String name = oAuth2User.getAttribute("name");
                String email = oAuth2User.getAttribute("email");
                String picture = oAuth2User.getAttribute("picture") != null ? oAuth2User.getAttribute("picture") : "default-picture.jpg";
                Customer newCustomer = customerService.findOrCreateCustomer(providerId, name, email, picture, provider);
                model.addAttribute("loggedInUser", newCustomer);
            }
        }

        return "body";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Tắt secure cho localhost
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
            return "register";
        }
        return "redirect:/";
    }

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
    public Map<String, Object> checkAuth(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy token từ cookie
            String token = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwtToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        logger.info("Found jwtToken in cookie: {}", token);
                        break;
                    }
                }
            }

            if (token != null) {
                String email = jwtUtil.extractEmail(token);
                if (email != null && jwtUtil.validateToken(token, email)) {
                    Optional<Customer> customerOpt = customerService.findByEmail(email);
                    if (customerOpt.isPresent()) {
                        Customer customer = customerOpt.get();
                        Map<String, String> user = new HashMap<>();
                        user.put("name", customer.getName());
                        user.put("picture", customer.getPicture());
                        user.put("email", customer.getEmail());
                        user.put("role", customer.getRole());
                        response.put("authenticated", true);
                        response.put("user", user);
                        response.put("token", token); // Trả về token hiện tại
                        logger.info("Check-auth: authenticated=true, email={}", customer.getEmail());
                        return response;
                    }
                }
            }
            logger.info("Check-auth: authenticated=false");
            response.put("authenticated", false);
            return response;
        } catch (Exception e) {
            logger.error("Error in check-auth: {}", e.getMessage(), e);
            response.put("authenticated", false);
            response.put("error", "Lỗi khi kiểm tra trạng thái đăng nhập: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/api/validate-token")
    @ResponseBody
    public Map<String, Object> validateToken(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String token = request.get("token");
        try {
            String email = jwtUtil.extractEmail(token);
            boolean isValid = jwtUtil.validateToken(token, email);
            response.put("valid", isValid);
            return response;
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", "Invalid token: " + e.getMessage());
            return response;
        }
    }
}