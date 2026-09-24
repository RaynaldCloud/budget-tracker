package com.raynald.budget_tracker.dto;

import com.raynald.budget_tracker.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        Long categoryId,
        TransactionType type,
        BigDecimal amount,
        LocalDate date,
        String note
) {}