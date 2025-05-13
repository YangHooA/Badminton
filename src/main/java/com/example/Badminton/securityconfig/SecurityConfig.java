package com.example.Badminton.securityconfig;

import com.example.Badminton.entity.Customer;
import com.example.Badminton.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.Cookie;
import java.net.URLEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/static/**",
                                "/CSS/**",
                                "/js/**",
                                "/images/**",
                                "/logo.png",
                                "/logonewT.png",
                                "/",
                                "/login",
                                "/dangkyaccount",
                                "/register",
                                "/api/register",
                                "/save-profile",
                                "/update-price",
                                "/api/quanly/**",
                                "/products",
                                "/inventory-by-product/{productId}",
                                "/giohang",
                                "/payment/**",
                                "/sanpham/**",
                                "/product-images/**",
                                "/cart/**",
                                "/products-by-doituong",
                                "/quanly",
                                "/api/login",
                                "/thanhtoan/success",
                                "/thanhtoan/cancel",
                                "/thanhtoan/payos/webhook",
                                "/submit-review",
                                "/get-reviews",
                                "/check-auth",
                                "/login/oauth2/code/google",
                                "/login/oauth2/code/facebook",
                                "/error",
                                "/detail",
                                "/api/cart/**",
                                "/api/validate-token",
                                "/api/cart/check-stock" // Chỉ cho phép check-stock công khai
                        ).permitAll()
                        .requestMatchers("/thanhtoan/payos").authenticated()
                        .requestMatchers("/quanly/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/quanly/**",
                                "/save-profile",
                                "/products",
                                "/api/login",
                                "/api/register",
                                "/thanhtoan/payos",
                                "/thanhtoan/payos/webhook",
                                "/submit-review",
                                "/get-reviews",
                                "/check-auth",
                                "/api/cart/**",
                                "/detail",
                                "/api/validate-token"

                        )
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService())
                        )
                        .successHandler((request, response, authentication) -> {
                            try {
                                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                                String provider = request.getRequestURI().contains("google") ? "google" : "facebook";
                                String providerId = provider.equals("google") ? oAuth2User.getAttribute("sub") : oAuth2User.getAttribute("id");
                                String name = oAuth2User.getAttribute("name");
                                String email = oAuth2User.getAttribute("email");
                                String picture = oAuth2User.getAttribute("picture") != null ? oAuth2User.getAttribute("picture") : "default-picture.jpg";

                                if (providerId == null) {
                                    response.sendRedirect("/error?message=ProviderIdNull");
                                    return;
                                }

                                Customer customer = customerService.findOrCreateCustomer(providerId, name, email, picture, provider);
                                String jwtToken = jwtUtil.generateToken(customer);

                                Cookie jwtCookie = new Cookie("jwtToken", jwtToken);
                                jwtCookie.setHttpOnly(false);
                                jwtCookie.setSecure(false); // Tắt Secure cho localhost
                                jwtCookie.setPath("/");
                                jwtCookie.setMaxAge(10 * 60 * 60);
                                response.addCookie(jwtCookie);

                                if (!customerService.isProfileComplete(customer)) {
                                    response.sendRedirect("/register?providerId=" + providerId + "&provider=" + provider);
                                } else if ("ADMIN".equals(customer.getRole())) {
                                    response.sendRedirect("/quanly");
                                } else {
                                    response.sendRedirect("/");
                                }
                            } catch (Exception e) {
                                response.sendRedirect("/error?message=" + URLEncoder.encode(e.getMessage(), "UTF-8"));
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            exception.printStackTrace();
                            response.sendRedirect("/login?error=OAuth2AuthenticationFailed");
                        })
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .deleteCookies("jwtToken")
                        .permitAll())
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, customerService), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return userRequest -> delegate.loadUser(userRequest);
    }
}