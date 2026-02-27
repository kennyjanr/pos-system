package com.example.pos.service;

import com.example.pos.entity.Role;
import com.example.pos.entity.User;
import com.example.pos.repository.RoleRepository;
import com.example.pos.repository.RefreshTokenRepository;
import com.example.pos.repository.UserRepository;
import com.example.pos.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder().id(2L).name("ROLE_MANAGER").build();
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .roles(new HashSet<>(Set.of(testRole)))
                .build();
    }

    @Test
    void testRegisterUserSuccessfully() {
        when(userRepository.count()).thenReturn(1L); // not first user
        when(roleRepository.findByName("ROLE_MANAGER")).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.registerUser("testuser", "test@example.com", "password123");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoginSuccessfully() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", "password", new java.util.ArrayList<>()));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(testUser)).thenReturn("jwtToken");
        when(jwtService.getAccessTokenExpirySeconds()).thenReturn(900L);
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AuthService.TokenPair result = authService.login("testuser", "password123");

        assertNotNull(result);
        assertEquals("jwtToken", result.accessToken);
        assertEquals(900L, result.expiresIn);
        assertNotNull(result.refreshToken);
    }

    @Test
    void testRefreshTokenSuccess() {
        when(refreshTokenRepository.findByToken("refreshToken")).thenReturn(Optional.empty());

        Optional<AuthService.TokenPair> result = authService.refreshToken("refreshToken");

        assertTrue(result.isEmpty());
    }

    @Test
    void testRevokeRefreshToken() {
        authService.revokeRefreshToken("token");
        verify(refreshTokenRepository, times(1)).findByToken("token");
    }
}
