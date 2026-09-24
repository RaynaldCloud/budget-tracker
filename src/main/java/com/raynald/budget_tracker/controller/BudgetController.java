package com.raynald.budget_tracker.controller;

import com.raynald.budget_tracker.dto.BudgetRequest;
import com.raynald.budget_tracker.dto.BudgetResponse;
import com.raynald.budget_tracker.service.BudgetService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<BudgetResponse> list(@RequestParam(required = false) Integer year,
                                     @RequestParam(required = false) Integer month) {
        return budgetService.listBudgets(year, month);
    }

    @PutMapping
    public BudgetResponse set(@RequestBody BudgetRequest request) {
        return budgetService.setBudget(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        budgetService.deleteBudget(id);
    }
}