package com.raynald.budget_tracker.service;

import com.raynald.budget_tracker.dto.CategoryRequest;
import com.raynald.budget_tracker.dto.CategoryResponse;
import com.raynald.budget_tracker.entity.Category;
import com.raynald.budget_tracker.entity.User;
import com.raynald.budget_tracker.exception.ConflictException;
import com.raynald.budget_tracker.exception.NotFoundException;
import com.raynald.budget_tracker.repository.CategoryRepository;
import com.raynald.budget_tracker.repository.TransactionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    public CategoryService(CategoryRepository categoryRepository,
                           TransactionRepository transactionRepository,
                           CurrentUserService currentUserService) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        User user = currentUserService.getCurrentUser();
        return categoryRepository.findByUserOrderByNameAsc(user).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        User user = currentUserService.getCurrentUser();
        String name = request.name().trim();
        if (categoryRepository.existsByUserAndNameIgnoreCase(user, name)) {
            throw new ConflictException("Category '" + name + "' already exists");
        }
        return CategoryResponse.from(categoryRepository.save(new Category(name, user)));
    }

    @Transactional
    public CategoryResponse renameCategory(Long id, CategoryRequest request) {
        Category category = findOwnedCategory(id);
        String newName = request.name().trim();
        boolean nameChanged = !category.getName().equalsIgnoreCase(newName);
        if (nameChanged && categoryRepository.existsByUserAndNameIgnoreCase(category.getUser(), newName)) {
            throw new ConflictException("Category '" + newName + "' already exists");
        }
        category.setName(newName);  // saved automatically when the transaction ends
        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = findOwnedCategory(id);
        if (transactionRepository.existsByCategory(category)) {
            throw new ConflictException("Category is used by existing transactions and can't be deleted");
        }
        categoryRepository.delete(category);
    }

    /** Finds a category only if it belongs to the current user. Reused by other services. */
    public Category findOwnedCategory(Long id) {
        User user = currentUserService.getCurrentUser();
        return categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Category " + id + " not found"));
    }
}