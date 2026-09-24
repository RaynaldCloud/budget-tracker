package com.raynald.budget_tracker.service;

import com.raynald.budget_tracker.dto.TransactionRequest;
import com.raynald.budget_tracker.dto.TransactionResponse;
import com.raynald.budget_tracker.entity.Category;
import com.raynald.budget_tracker.entity.Transaction;
import com.raynald.budget_tracker.entity.User;
import com.raynald.budget_tracker.exception.BadRequestException;
import com.raynald.budget_tracker.exception.NotFoundException;
import com.raynald.budget_tracker.repository.TransactionRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryService categoryService,
                              CurrentUserService currentUserService) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listTransactions(LocalDate from, LocalDate to, Long categoryId) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("'from' date must be on or before 'to' date");
        }
        User user = currentUserService.getCurrentUser();
        return transactionRepository.search(user, from, to, categoryId).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(Long id) {
        return TransactionResponse.from(findOwnedTransaction(id));
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = currentUserService.getCurrentUser();
        Category category = categoryService.findOwnedCategory(request.categoryId());
        Transaction transaction = new Transaction(
                user, category, request.type(), request.amount(), request.date(), request.note());
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Transaction transaction = findOwnedTransaction(id);
        transaction.setCategory(categoryService.findOwnedCategory(request.categoryId()));
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setDate(request.date());
        transaction.setNote(request.note());
        return TransactionResponse.from(transaction);  // saved automatically (dirty checking)
    }

    @Transactional
    public void deleteTransaction(Long id) {
        transactionRepository.delete(findOwnedTransaction(id));
    }

    private Transaction findOwnedTransaction(Long id) {
        User user = currentUserService.getCurrentUser();
        return transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Transaction " + id + " not found"));
    }
}