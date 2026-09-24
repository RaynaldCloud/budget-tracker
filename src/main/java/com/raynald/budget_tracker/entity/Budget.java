package com.raynald.budget_tracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budgets",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"user_id", "category_id", "budget_year", "budget_month"}))
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "budget_year", nullable = false)
    private int year;

    @Column(name = "budget_month", nullable = false)
    private int month;  // 1 to 12

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    protected Budget() {}

    public Budget(User user, Category category, int year, int month, BigDecimal amount) {
        this.user = user;
        this.category = category;
        this.year = year;
        this.month = month;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Category getCategory() { return category; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public BigDecimal getAmount() { return amount; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }
}