package com.example.pos.controller;

import com.example.pos.dto.*;
import com.example.pos.entity.User;
import com.example.pos.repository.UserRepository;
import com.example.pos.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Value("${REFRESH_TOKEN_EXPIRY_DAYS:30}")
    private int refreshExpiryDays;

    @Value("${FRONTEND_ORIGIN:http://localhost:5173}")
    private String frontendOrigin;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already taken");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already in use");
        }
        User user = authService.registerUser(req.getUsername(), req.getEmail(), req.getPassword());
        UserResponse resp = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        AuthService.TokenPair pair = authService.login(req.getUsernameOrEmail(), req.getPassword());
        // set refresh token cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", pair.refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(refreshExpiryDays))
                .sameSite("Lax")
                .domain("localhost")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok(new AuthResponse(pair.accessToken, "Bearer", pair.expiresIn));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refreshToken".equals(c.getName())) {
                    token = c.getValue();
                }
            }
        }
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return authService.refreshToken(token).map(tp -> {
            ResponseCookie cookie = ResponseCookie.from("refreshToken", tp.refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofDays(refreshExpiryDays))
                    .sameSite("Lax")
                    .domain("localhost")
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());
            return ResponseEntity.ok(new AuthResponse(tp.accessToken, "Bearer", tp.expiresIn));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refreshToken".equals(c.getName())) token = c.getValue();
            }
        }
        if (token != null) authService.revokeRefreshToken(token);
        // clear cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .domain("localhost")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String username = a.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        UserResponse resp = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .build();
        return ResponseEntity.ok(resp);
    }
}
