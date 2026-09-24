package com.raynald.budget_tracker.service;

import com.raynald.budget_tracker.dto.BudgetRequest;
import com.raynald.budget_tracker.dto.BudgetResponse;
import com.raynald.budget_tracker.entity.Budget;
import com.raynald.budget_tracker.entity.Category;
import com.raynald.budget_tracker.entity.User;
import com.raynald.budget_tracker.exception.BadRequestException;
import com.raynald.budget_tracker.exception.NotFoundException;
import com.raynald.budget_tracker.repository.BudgetRepository;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;

    public BudgetService(BudgetRepository budgetRepository,
                         CategoryService categoryService,
                         CurrentUserService currentUserService) {
        this.budgetRepository = budgetRepository;
        this.categoryService = categoryService;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> listBudgets(Integer year, Integer month) {
        YearMonth period = toYearMonth(year, month);
        User user = currentUserService.getCurrentUser();
        return budgetRepository.findForMonth(user, period.getYear(), period.getMonthValue()).stream()
                .map(BudgetResponse::from)
                .toList();
    }

    /** Creates the budget, or updates it if one already exists for that category and month. */
    @Transactional
    public BudgetResponse setBudget(BudgetRequest request) {
        YearMonth period = toYearMonth(request.year(), request.month());
        User user = currentUserService.getCurrentUser();
        Category category = categoryService.findOwnedCategory(request.categoryId());

        Budget budget = budgetRepository
                .findByUserAndCategoryAndYearAndMonth(user, category, period.getYear(), period.getMonthValue())
                .orElseGet(() -> new Budget(user, category, period.getYear(), period.getMonthValue(), request.amount()));
        budget.setAmount(request.amount());

        return BudgetResponse.from(budgetRepository.save(budget));
    }

    @Transactional
    public void deleteBudget(Long id) {
        User user = currentUserService.getCurrentUser();
        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Budget " + id + " not found"));
        budgetRepository.delete(budget);
    }

    /** Converts a year and month into a YearMonth, defaulting to the current month if both are missing. */
    static YearMonth toYearMonth(Integer year, Integer month) {
        if (year == null && month == null) {
            return YearMonth.now();
        }
        if (year == null || month == null) {
            throw new BadRequestException("Provide both 'year' and 'month', or neither");
        }
        if (month < 1 || month > 12) {
            throw new BadRequestException("'month' must be between 1 and 12");
        }
        return YearMonth.of(year, month);
    }
}