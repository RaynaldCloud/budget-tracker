package com.raynald.budget_tracker.dto;

import com.raynald.budget_tracker.entity.User;

public record UserResponse(Long id, String email, String name) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }
}