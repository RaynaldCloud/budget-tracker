package com.raynald.budget_tracker.controller;

import com.raynald.budget_tracker.dto.MonthlySummary;
import com.raynald.budget_tracker.service.SummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping
    public MonthlySummary get(@RequestParam(required = false) Integer year,
                              @RequestParam(required = false) Integer month) {
        return summaryService.getMonthlySummary(year, month);
    }
}