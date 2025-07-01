package com.fyp.fitRoute.security.Utilities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class JwtUtils {
    @Value("${jwt.secret}")
    private String SECRET;

    public loginResponse generateToken(UserDetails userDetails) {
        Map<String, String> claims = new HashMap<>();
        // sets issuer you can set other related info by setting claims like this
        claims.put("iss", "Fit Route Security");
        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());
        String role = roles.contains("ROLE_ADMIN") ? "ADMIN": "USER"; // Default to USER if no roles
        return new loginResponse(Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(Instant.now()))
                .signWith(generateKey())
                .compact(), role);
    }

    private SecretKey generateKey() {
        byte[] decodedKey = Base64.getDecoder().decode(SECRET);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    public String extractUsername(String jwt) {
        Claims claims = getClaims(jwt);
        return claims.getSubject();
    }

    private Claims getClaims(String jwt) {
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
}
