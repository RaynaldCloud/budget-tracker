package com.raynald.budget_tracker.repository;

import com.raynald.budget_tracker.entity.TransactionType;
import java.math.BigDecimal;

/** One row of totals: the sum of a category's income or expenses for a period. */
public interface CategoryTotal {
    Long getCategoryId();
    String getCategoryName();
    TransactionType getTransactionType();
    BigDecimal getTotal();
}
