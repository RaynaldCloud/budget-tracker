package com.raynald.budget_tracker.dto;

import java.math.BigDecimal;

public record BudgetRequest(Long categoryId, Integer year, Integer month, BigDecimal amount) {}