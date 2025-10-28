package com.example.demo.service;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.UserEntity;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.jwt.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(AuthenticationManager authManager, JwtService jwtService,
            UserRepository userRepo, PasswordEncoder encoder,
            RefreshTokenRepository refreshTokenRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public String register(AuthRequest req) {
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("User already exists!");
        }

        UserEntity user = new UserEntity();
        user.setUsername(req.getUsername());
        user.setPassword(encoder.encode(req.getPassword()));
        userRepo.save(user);

        return "Registered successfully ✅";
    }

    @Transactional
    public AuthResponse login(AuthRequest req) {
        try {
            // Authenticate user credentials
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

            // Get user from database
            var user = userRepo.findByUsername(req.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + req.getUsername()));

            // Create UserDetails for token generation
            var userDetails = User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities("ROLE_USER") // Changed from .roles("USER") to .authorities("ROLE_USER")
                    .build();

            // Generate access token using JwtService
            String accessToken = jwtService.generateToken(userDetails);

            // Generate refresh token
            String refreshToken = generateRefreshToken(user.getUsername());

            return new AuthResponse(accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        // Validate refresh token from database
        var storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Check if token is expired
        if (storedToken.getExpiryDate().before(new Date())) {
            refreshTokenRepository.delete(storedToken);
            throw new RuntimeException("Refresh token expired");
        }

        // Get user associated with the token
        var user = userRepo.findByUsername(storedToken.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create UserDetails for token generation
        var userDetails = User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();

        // Generate new access token using JwtService
        String newAccessToken = jwtService.generateToken(userDetails);

        // Update existing refresh token (instead of delete + create)
        String newRefreshToken = updateRefreshToken(storedToken);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    /**
     * Generate refresh token with proper transaction handling
     */
    private String generateRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString();

        // Try to find existing token first
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUsername(username);

        if (existingToken.isPresent()) {
            // Update existing token
            RefreshToken token = existingToken.get();
            token.setToken(refreshToken);
            token.setExpiryDate(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000));
            refreshTokenRepository.save(token);
        } else {
            // Create new token
            RefreshToken newToken = new RefreshToken();
            newToken.setToken(refreshToken);
            newToken.setUsername(username);
            newToken.setExpiryDate(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000));
            refreshTokenRepository.save(newToken);
        }

        return refreshToken;
    }

    /**
     * Update existing refresh token
     */
    private String updateRefreshToken(RefreshToken existingToken) {
        String newTokenValue = UUID.randomUUID().toString();
        existingToken.setToken(newTokenValue);
        existingToken.setExpiryDate(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000));
        refreshTokenRepository.save(existingToken);
        return newTokenValue;
    }

    @Transactional
    public void logout(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }

    /**
     * Validate refresh token without generating new tokens
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            var storedToken = refreshTokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

            return !storedToken.getExpiryDate().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get username from refresh token
     */
    public String getUsernameFromRefreshToken(String refreshToken) {
        var storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        return storedToken.getUsername();
    }
}
