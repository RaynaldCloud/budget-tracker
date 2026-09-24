package com.raynald.budget_tracker.service;

import com.raynald.budget_tracker.dto.CategorySummary;
import com.raynald.budget_tracker.dto.MonthlySummary;
import com.raynald.budget_tracker.entity.Budget;
import com.raynald.budget_tracker.entity.TransactionType;
import com.raynald.budget_tracker.entity.User;
import com.raynald.budget_tracker.repository.BudgetRepository;
import com.raynald.budget_tracker.repository.CategoryTotal;
import com.raynald.budget_tracker.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SummaryService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final CurrentUserService currentUserService;

    public SummaryService(TransactionRepository transactionRepository,
                          BudgetRepository budgetRepository,
                          CurrentUserService currentUserService) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public MonthlySummary getMonthlySummary(Integer year, Integer month) {
        YearMonth period = BudgetService.toYearMonth(year, month);
        User user = currentUserService.getCurrentUser();

        List<CategoryTotal> totals = transactionRepository.sumByCategoryAndType(
                user, period.atDay(1), period.atEndOfMonth());
        List<Budget> budgets = budgetRepository.findForMonth(
                user, period.getYear(), period.getMonthValue());

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<Long, BigDecimal> spentByCategory = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();

        for (CategoryTotal row : totals) {
            if (row.getTransactionType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(row.getTotal());
            } else {
                totalExpense = totalExpense.add(row.getTotal());
                spentByCategory.merge(row.getCategoryId(), row.getTotal(), BigDecimal::add);
                categoryNames.put(row.getCategoryId(), row.getCategoryName());
            }
        }

        Map<Long, BigDecimal> budgetByCategory = new HashMap<>();
        for (Budget budget : budgets) {
            budgetByCategory.put(budget.getCategory().getId(), budget.getAmount());
            categoryNames.put(budget.getCategory().getId(), budget.getCategory().getName());
        }

        // Every category with spending, a budget, or both; biggest spenders first
        List<CategorySummary> categories = categoryNames.keySet().stream()
                .map(id -> summarise(id, categoryNames.get(id),
                        spentByCategory.getOrDefault(id, BigDecimal.ZERO), budgetByCategory.get(id)))
                .sorted(Comparator.comparing(CategorySummary::spent).reversed())
                .toList();

        int overBudgetCount = (int) categories.stream().filter(CategorySummary::overBudget).count();

        return new MonthlySummary(period.getYear(), period.getMonthValue(), totalIncome, totalExpense,
                totalIncome.subtract(totalExpense), overBudgetCount, categories);
    }

    private CategorySummary summarise(Long id, String name, BigDecimal spent, BigDecimal budget) {
        if (budget == null) {
            return new CategorySummary(id, name, null, spent, null, null, false);
        }
        BigDecimal remaining = budget.subtract(spent);
        Integer percentUsed = budget.signum() == 0 ? null
                : spent.multiply(BigDecimal.valueOf(100)).divide(budget, 0, RoundingMode.HALF_UP).intValue();
        boolean overBudget = spent.compareTo(budget) > 0;
        return new CategorySummary(id, name, budget, spent, remaining, percentUsed, overBudget);
    }
}