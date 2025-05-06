package com.example.Badminton.securityconfig;

import com.example.Badminton.entity.Customer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private String SECRET_KEY = "L2cUlPEXrmxeuzaYhNcJWicZh6sME8eFQPmnsUWeyJbSVnq7DMjt8CYA5yhbi1cg"; // Thay bằng khóa bí mật mạnh hơn
    private long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 giờ



    public String generateToken(Customer customer) {
        try {
            System.out.println("Generating token for customer: " + customer.getEmail());
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", customer.getRole());
            claims.put("email", customer.getEmail());
            String token = createToken(claims, customer.getEmail());
            System.out.println("Generated token: " + token);
            return token;
        } catch (Exception e) {
            System.err.println("Error generating token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    private String createToken(Map<String, Object> claims, String subject) {
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                    .compact();
        } catch (Exception e) {
            System.err.println("Error creating token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create token", e);
        }
    }


    public Boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}