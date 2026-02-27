package com.example.pos.security;

import com.example.pos.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final Key signingKey;
    private final long accessTokenExpirySeconds;

    public JwtService(@Value("${JWT_SECRET:replace-with-secure-secret-key-at-least-32-chars}") String jwtSecret,
                      @Value("${ACCESS_TOKEN_EXPIRY_SECONDS:900}") String expirySecondsStr) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirySeconds = Long.parseLong(expirySecondsStr);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenExpirySeconds);

        List<String> roles = user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public long getAccessTokenExpirySeconds() {
        return accessTokenExpirySeconds;
    }

}
