package com.raynald.budget_tracker.dto;

import com.raynald.budget_tracker.entity.Budget;
import java.math.BigDecimal;

public record BudgetResponse(
        Long id, Long categoryId, String categoryName, int year, int month, BigDecimal amount) {

    public static BudgetResponse from(Budget b) {
        return new BudgetResponse(b.getId(), b.getCategory().getId(), b.getCategory().getName(),
                b.getYear(), b.getMonth(), b.getAmount());
    }
}