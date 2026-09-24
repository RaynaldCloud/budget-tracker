package com.raynald.budget_tracker.dto;

import java.math.BigDecimal;

public record CategorySummary(
        Long categoryId,
        String categoryName,
        BigDecimal budget,        // null if no budget is set
        BigDecimal spent,
        BigDecimal remaining,     // null if no budget is set
        Integer percentUsed,      // null if no budget is set
        boolean overBudget
) {}