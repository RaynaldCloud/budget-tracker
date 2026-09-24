package com.raynald.budget_tracker.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummary(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal net,
        int overBudgetCount,
        List<CategorySummary> categories
) {}