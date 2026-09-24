package com.raynald.budget_tracker.service;

import com.raynald.budget_tracker.dto.AuthResponse;
import com.raynald.budget_tracker.dto.LoginRequest;
import com.raynald.budget_tracker.dto.RegisterRequest;
import com.raynald.budget_tracker.dto.UserResponse;
import com.raynald.budget_tracker.entity.User;
import com.raynald.budget_tracker.exception.ConflictException;
import com.raynald.budget_tracker.exception.UnauthorizedException;
import com.raynald.budget_tracker.repository.UserRepository;
import com.raynald.budget_tracker.security.TokenService;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normaliseEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        String hash = passwordEncoder.encode(request.password());
        User user = userRepository.save(new User(email, hash, request.name().trim()));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normaliseEmail(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        return new AuthResponse(tokenService.createToken(user), "Bearer", tokenService.getExpirySeconds());
    }

    private static String normaliseEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}