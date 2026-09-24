package com.raynald.budget_tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.raynald.budget_tracker.exception.BadRequestException;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class BudgetServiceTest {

    @Test
    void defaultsToCurrentMonthWhenYearAndMonthAreMissing() {
        assertThat(BudgetService.toYearMonth(null, null)).isEqualTo(YearMonth.now());
    }

    @Test
    void acceptsAValidYearAndMonth() {
        assertThat(BudgetService.toYearMonth(2026, 2)).isEqualTo(YearMonth.of(2026, 2));
    }

    @Test
    void rejectsMonthOutsideOneToTwelve() {
        assertThatThrownBy(() -> BudgetService.toYearMonth(2026, 13))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("between 1 and 12");
    }

    @Test
    void rejectsYearWithoutMonth() {
        assertThatThrownBy(() -> BudgetService.toYearMonth(2026, null))
                .isInstanceOf(BadRequestException.class);
    }
}