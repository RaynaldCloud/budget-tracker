package com.raynald.budget_tracker.dto;

import com.raynald.budget_tracker.entity.Category;

public record CategoryResponse(Long id, String name) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
