package com.raynald.budget_tracker.repository;

import com.raynald.budget_tracker.entity.Budget;
import com.raynald.budget_tracker.entity.Category;
import com.raynald.budget_tracker.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("""
            SELECT b FROM Budget b
            JOIN FETCH b.category c
            WHERE b.user = :user AND b.year = :year AND b.month = :month
            ORDER BY c.name
            """)
    List<Budget> findForMonth(@Param("user") User user,
                              @Param("year") int year,
                              @Param("month") int month);

    Optional<Budget> findByUserAndCategoryAndYearAndMonth(User user, Category category, int year, int month);

    Optional<Budget> findByIdAndUser(Long id, User user);

    boolean existsByCategory(Category category);
}
