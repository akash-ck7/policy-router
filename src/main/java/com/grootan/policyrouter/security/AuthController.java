package com.grootan.policyrouter.security;

import com.grootan.policyrouter.domain.model.AppUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AppUserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(JwtUtil jwtUtil,
                          AppUserDetailsService userDetailsService,
                          @Lazy BCryptPasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            log.info("Login attempt for username: {}", request.getUsername());

            org.springframework.security.core.userdetails.UserDetails userDetails =
                    userDetailsService.loadUserByUsername(request.getUsername());

            log.info("User found: {}", userDetails.getUsername());
            log.info("Password matches: {}",
                    passwordEncoder.matches(request.getPassword(), userDetails.getPassword()));

            if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
                log.warn("Password mismatch for user: {}", request.getUsername());
                return ResponseEntity.status(401).build();
            }

            String token = jwtUtil.generateToken(request.getUsername());
            String role = userDetails.getAuthorities()
                    .iterator().next().getAuthority();

            log.info("Login successful for user: {}", request.getUsername());
            return ResponseEntity.ok(
                    new AuthResponse(token, request.getUsername(), role));

        } catch (Exception e) {
            log.error("Login failed for user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        try {
            AppUser user = userDetailsService.register(
                    request.getUsername(), request.getPassword());
            return ResponseEntity.ok(
                    "User registered successfully: " + user.getUsername());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}