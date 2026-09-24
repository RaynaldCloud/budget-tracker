package com.raynald.budget_tracker.controller;

import com.raynald.budget_tracker.dto.AuthResponse;
import com.raynald.budget_tracker.dto.LoginRequest;
import com.raynald.budget_tracker.dto.RegisterRequest;
import com.raynald.budget_tracker.dto.UserResponse;
import com.raynald.budget_tracker.service.AuthService;
import com.raynald.budget_tracker.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    public AuthController(AuthService authService, CurrentUserService currentUserService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(currentUserService.getCurrentUser());
    }
}