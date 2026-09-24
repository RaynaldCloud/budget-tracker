package com.raynald.budget_tracker.dto;

public record AuthResponse(String token, String tokenType, long expiresIn) {}