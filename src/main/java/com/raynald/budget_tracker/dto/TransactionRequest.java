package com.raynald.budget_tracker.dto;

import com.raynald.budget_tracker.entity.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "Category is required")
        Long categoryId,

        @NotNull(message = "Type is required (INCOME or EXPENSE)")
        TransactionType type,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        @Digits(integer = 10, fraction = 2, message = "Amount can have at most 2 decimal places")
        BigDecimal amount,

        @NotNull(message = "Date is required")
        LocalDate date,

        @Size(max = 255, message = "Note must be at most 255 characters")
        String note
) {}