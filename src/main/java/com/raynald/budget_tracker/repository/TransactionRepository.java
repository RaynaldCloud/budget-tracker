package com.raynald.budget_tracker.repository;

import com.raynald.budget_tracker.entity.Category;
import com.raynald.budget_tracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByCategory(Category category);
}