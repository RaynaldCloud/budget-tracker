package com.raynald.budget_tracker.repository;

import com.raynald.budget_tracker.entity.Category;
import com.raynald.budget_tracker.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserOrderByNameAsc(User user);
    Optional<Category> findByIdAndUser(Long id, User user);
    boolean existsByUserAndNameIgnoreCase(User user, String name);
}