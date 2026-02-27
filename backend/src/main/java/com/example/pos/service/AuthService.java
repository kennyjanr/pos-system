package com.example.pos.service;

import com.example.pos.dto.AuthResponse;
import com.example.pos.entity.RefreshToken;
import com.example.pos.entity.Role;
import com.example.pos.entity.User;
import com.example.pos.repository.RefreshTokenRepository;
import com.example.pos.repository.RoleRepository;
import com.example.pos.repository.UserRepository;
import com.example.pos.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    public static class TokenPair {
        public final String accessToken;
        public final long expiresIn;
        public final String refreshToken;

        public TokenPair(String accessToken, long expiresIn, String refreshToken) {
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
            this.refreshToken = refreshToken;
        }
    }

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${REFRESH_TOKEN_EXPIRY_DAYS:30}")
    private int refreshExpiryDays;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public User registerUser(String username, String email, String password) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .build();

        // assign role: if first user -> ROLE_ADMIN else ROLE_MANAGER
        long count = userRepository.count();
        String roleName = count == 0 ? "ROLE_ADMIN" : "ROLE_MANAGER";
        Role role = roleRepository.findByName(roleName).orElseThrow(() -> new IllegalStateException("Role missing: " + roleName));
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    public TokenPair login(String usernameOrEmail, String password) {
        // resolve to username if email provided
        String username = usernameOrEmail;
        if (usernameOrEmail.contains("@")) {
            username = userRepository.findByEmail(usernameOrEmail).map(User::getUsername).orElse(usernameOrEmail);
        }

        // authenticate
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken rt = createRefreshToken(user);
        refreshTokenRepository.save(rt);

        return new TokenPair(accessToken, jwtService.getAccessTokenExpirySeconds(), rt.getToken());
    }

    private RefreshToken createRefreshToken(User user) {
        String token = generateSecureToken();
        Instant expires = Instant.now().plus(refreshExpiryDays, ChronoUnit.DAYS);
        RefreshToken rt = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expires)
                .revoked(false)
                .build();
        return rt;
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString() + Long.toHexString(secureRandom.nextLong());
    }

    @Transactional
    public Optional<TokenPair> refreshToken(String token) {
        return refreshTokenRepository.findByToken(token).map(rt -> {
            if (rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now())) {
                return null;
            }
            // revoke old
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);

            User user = rt.getUser();
            RefreshToken newRt = createRefreshToken(user);
            refreshTokenRepository.save(newRt);
            String accessToken = jwtService.generateAccessToken(user);
            return new TokenPair(accessToken, jwtService.getAccessTokenExpirySeconds(), newRt.getToken());
        });
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }
}
