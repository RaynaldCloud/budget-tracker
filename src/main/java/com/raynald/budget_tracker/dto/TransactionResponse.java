package com.raynald.budget_tracker.dto;

import com.raynald.budget_tracker.entity.Transaction;
import com.raynald.budget_tracker.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        Long categoryId,
        String categoryName,
        TransactionType type,
        BigDecimal amount,
        LocalDate date,
        String note
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getCategory().getId(),
                t.getCategory().getName(),
                t.getType(),
                t.getAmount(),
                t.getDate(),
                t.getNote()
        );
    }
}