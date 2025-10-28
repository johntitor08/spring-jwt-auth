package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    // Cookie configuration
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7 days in seconds
    private static final boolean HTTP_ONLY = true;
    private static final boolean SECURE = false; // Set to true in production with HTTPS
    private static final String SAME_SITE = "Lax";

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        try {
            String result = authService.register(req);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req, HttpServletResponse response) {
        try {
            AuthResponse authResponse = authService.login(req);

            // Set refresh token as HTTP-only cookie
            setRefreshTokenCookie(response, authResponse.getRefreshToken());

            // Return only access token in response body
            AuthResponse responseBody = new AuthResponse(authResponse.getAccessToken(), null);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(authResponse.getRefreshToken()))
                    .body(responseBody);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Get refresh token from cookie
            String refreshToken = extractRefreshTokenFromCookie(request);

            if (refreshToken == null) {
                return ResponseEntity.badRequest().body("Refresh token not found");
            }

            AuthResponse authResponse = authService.refreshToken(refreshToken);

            // Set new refresh token as HTTP-only cookie
            setRefreshTokenCookie(response, authResponse.getRefreshToken());

            // Return only access token in response body
            AuthResponse responseBody = new AuthResponse(authResponse.getAccessToken(), null);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(authResponse.getRefreshToken()))
                    .body(responseBody);

        } catch (Exception e) {
            // Clear invalid refresh token cookie
            clearRefreshTokenCookie(response);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Get username from refresh token
            String refreshToken = extractRefreshTokenFromCookie(request);
            if (refreshToken != null) {
                try {
                    String username = authService.getUsernameFromRefreshToken(refreshToken);
                    authService.logout(username);
                } catch (Exception e) {
                    // If token is invalid, still clear the cookie
                }
            }

            // Clear refresh token cookie
            clearRefreshTokenCookie(response);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, createExpiredRefreshTokenCookie())
                    .body("Logged out successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Logout failed");
        }
    }

    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndpoint() {
        return ResponseEntity.ok("Bu endpoint'e sadece token ile erişilebilir ✅");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running ✅");
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        try {
            String refreshToken = extractRefreshTokenFromCookie(request);
            if (refreshToken == null) {
                return ResponseEntity.ok().body("No refresh token found");
            }

            boolean isValid = authService.validateRefreshToken(refreshToken);
            return ResponseEntity.ok().body("Token valid: " + isValid);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Validation failed: " + e.getMessage());
        }
    }

    // ============ HELPER METHODS ============

    /**
     * Extract refresh token from HTTP-only cookie
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Set refresh token as HTTP-only cookie using Servlet Cookie
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(HTTP_ONLY);
        cookie.setSecure(SECURE);
        cookie.setPath("/");
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
        response.addCookie(cookie);
    }

    /**
     * Create refresh token cookie string for Response Header
     */
    private String createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path("/")
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .sameSite(SAME_SITE)
                .build()
                .toString();
    }

    /**
     * Create expired refresh token cookie to clear it
     */
    private String createExpiredRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path("/")
                .maxAge(0)
                .sameSite(SAME_SITE)
                .build()
                .toString();
    }

    /**
     * Clear refresh token cookie using Servlet Cookie
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(HTTP_ONLY);
        cookie.setSecure(SECURE);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
