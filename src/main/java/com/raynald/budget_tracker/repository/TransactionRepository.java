package com.raynald.budget_tracker.repository;

import com.raynald.budget_tracker.entity.Category;
import com.raynald.budget_tracker.entity.Transaction;
import com.raynald.budget_tracker.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByCategory(Category category);

    Optional<Transaction> findByIdAndUser(Long id, User user);

    @Query("""
            SELECT t FROM Transaction t
            JOIN FETCH t.category
            WHERE t.user = :user
              AND (:from IS NULL OR t.date >= :from)
              AND (:to IS NULL OR t.date <= :to)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
            ORDER BY t.date DESC, t.id DESC
            """)
    List<Transaction> search(@Param("user") User user,
                             @Param("from") LocalDate from,
                             @Param("to") LocalDate to,
                             @Param("categoryId") Long categoryId);
}