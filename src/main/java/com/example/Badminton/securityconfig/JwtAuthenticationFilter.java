package com.example.Badminton.securityconfig;

import com.example.Badminton.entity.Customer;
import com.example.Badminton.service.CustomerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class JwtAuthenticationFilter implements jakarta.servlet.Filter {

    private final JwtUtil jwtUtil;
    private final CustomerService customerService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomerService customerService) {
        this.jwtUtil = jwtUtil;
        this.customerService = customerService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Bỏ qua tài nguyên tĩnh
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.startsWith("/CSS/") ||
                requestURI.startsWith("/js/") ||
                requestURI.startsWith("/images/") ||
                requestURI.startsWith("/image/") ||
                requestURI.equals("/logo.png") ||
                requestURI.equals("/logonewT.png")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String header = httpRequest.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                System.out.println("Extracted token: " + token);
                String email = jwtUtil.extractEmail(token);
                System.out.println("Extracted email: " + email);
                if (email != null && jwtUtil.validateToken(token, email)) {
                    String role = jwtUtil.extractRole(token);
                    Optional<Customer> userOpt = customerService.findByEmail(email);
                    if (userOpt.isPresent()) {
                        Customer customer = userOpt.get();
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(customer, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("Authentication set for user: " + email);
                    } else {
                        System.out.println("User not found for email: " + email);
                        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                        return;
                    }
                } else {
                    System.out.println("Invalid token or email");
                    httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                }
            }
            chain.doFilter(request, response);
        } catch (Exception e) {
            System.err.println("Error in JwtAuthenticationFilter: " + e.getMessage());
            e.printStackTrace();
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Filter error: " + e.getMessage());
        }
    }

    @Override
    public void init(jakarta.servlet.FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}