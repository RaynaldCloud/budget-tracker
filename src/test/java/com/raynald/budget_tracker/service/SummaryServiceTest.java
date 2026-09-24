package com.raynald.budget_tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.raynald.budget_tracker.dto.CategorySummary;
import com.raynald.budget_tracker.dto.MonthlySummary;
import com.raynald.budget_tracker.entity.Budget;
import com.raynald.budget_tracker.entity.Category;
import com.raynald.budget_tracker.entity.TransactionType;
import com.raynald.budget_tracker.entity.User;
import com.raynald.budget_tracker.repository.BudgetRepository;
import com.raynald.budget_tracker.repository.CategoryTotal;
import com.raynald.budget_tracker.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock BudgetRepository budgetRepository;
    @Mock CurrentUserService currentUserService;

    @InjectMocks SummaryService summaryService;

    private final User user = new User("test@example.com", "hash", "Test");

    /** A simple stand-in for one row of the SUM ... GROUP BY query. */
    private record Row(Long categoryId, String categoryName,
                       TransactionType transactionType, BigDecimal total) implements CategoryTotal {
        public Long getCategoryId() { return categoryId; }
        public String getCategoryName() { return categoryName; }
        public TransactionType getTransactionType() { return transactionType; }
        public BigDecimal getTotal() { return total; }
    }

    private Category category(long id, String name) {
        Category category = new Category(name, user);
        ReflectionTestUtils.setField(category, "id", id);  // ids are normally set by the database
        return category;
    }

    @Test
    void calculatesTotalsAndFlagsOverBudgetCategories() {
        Category food = category(1, "Food");
        Category transport = category(2, "Transport");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.sumByCategoryAndType(eq(user), any(), any())).thenReturn(List.of(
                new Row(1L, "Food", TransactionType.EXPENSE, new BigDecimal("33.70")),
                new Row(3L, "Salary", TransactionType.INCOME, new BigDecimal("1500.00"))));
        when(budgetRepository.findForMonth(user, 2026, 9)).thenReturn(List.of(
                new Budget(user, food, 2026, 9, new BigDecimal("20.00")),
                new Budget(user, transport, 2026, 9, new BigDecimal("50.00"))));

        MonthlySummary summary = summaryService.getMonthlySummary(2026, 9);

        assertThat(summary.totalIncome()).isEqualByComparingTo("1500.00");
        assertThat(summary.totalExpense()).isEqualByComparingTo("33.70");
        assertThat(summary.net()).isEqualByComparingTo("1466.30");
        assertThat(summary.overBudgetCount()).isEqualTo(1);

        // Income categories don't appear in the spending breakdown
        assertThat(summary.categories()).hasSize(2);

        // Biggest spender first
        CategorySummary foodSummary = summary.categories().get(0);
        assertThat(foodSummary.categoryName()).isEqualTo("Food");
        assertThat(foodSummary.spent()).isEqualByComparingTo("33.70");
        assertThat(foodSummary.remaining()).isEqualByComparingTo("-13.70");
        assertThat(foodSummary.percentUsed()).isEqualTo(169);
        assertThat(foodSummary.overBudget()).isTrue();

        CategorySummary transportSummary = summary.categories().get(1);
        assertThat(transportSummary.categoryName()).isEqualTo("Transport");
        assertThat(transportSummary.spent()).isEqualByComparingTo("0");
        assertThat(transportSummary.percentUsed()).isEqualTo(0);
        assertThat(transportSummary.overBudget()).isFalse();
    }

    @Test
    void categoryWithoutBudgetHasNoBudgetFields() {
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.sumByCategoryAndType(eq(user), any(), any())).thenReturn(List.of(
                new Row(1L, "Food", TransactionType.EXPENSE, new BigDecimal("12.00"))));
        when(budgetRepository.findForMonth(user, 2026, 9)).thenReturn(List.of());

        CategorySummary food = summaryService.getMonthlySummary(2026, 9).categories().get(0);

        assertThat(food.budget()).isNull();
        assertThat(food.remaining()).isNull();
        assertThat(food.percentUsed()).isNull();
        assertThat(food.overBudget()).isFalse();
    }
}