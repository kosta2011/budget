package com.budget.service;

import com.budget.dto.BalanceResponse;
import com.budget.dto.transactions.TransactionCreateRequest;
import com.budget.dto.transactions.TransactionResponse;
import com.budget.dto.transactions.TransactionUpdateRequest;
import com.budget.dto.analytics.AnalyticsItem;
import com.budget.dto.analytics.AnalyticsResponse;
import com.budget.entity.Category;
import com.budget.entity.Transaction;
import com.budget.entity.User;
import com.budget.exception.CategoryNotFoundException;
import com.budget.exception.TransactionNotFoundException;
import com.budget.mapper.TransactionMapper;
import com.budget.repository.CategoryRepository;
import com.budget.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.Tuple;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final EntityManager entityManager;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new AuthenticationCredentialsNotFoundException("Principal is not of type User");
        }
        return (User) principal;
    }

    @Transactional
    public TransactionResponse create(TransactionCreateRequest request) {
        User currentUser = getCurrentUser();
        Category category = null;
        if (request.categoryUuid() != null) {
            category = categoryRepository.findById(request.categoryUuid())
                    .orElseThrow(() -> new CategoryNotFoundException(request.categoryUuid()));
        }
        Transaction transaction = transactionMapper.toEntity(request, category);
        transaction.setUser(currentUser);
        transaction = transactionRepository.saveAndFlush(transaction);
        entityManager.refresh(transaction);
        return transactionMapper.toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAll(String categoryUuid, String type,
                                            LocalDate dateFrom, LocalDate dateTo,
                                            Pageable pageable) {
        User currentUser = getCurrentUser();

        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom cannot be after dateTo");
        }

        Specification<Transaction> spec = (root, query, cb) ->
                cb.equal(root.get("user").get("uuid"), currentUser.getUuid());

        if (categoryUuid != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("uuid"), categoryUuid));
        }
        if (type != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("type"), type));
        }
        if (dateFrom != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("transactionDate"), dateFrom));
        }
        if (dateTo != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("transactionDate"), dateTo));
        }
        return transactionRepository.findAll(spec, pageable)
                .map(transactionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getByUuid(String uuid) {
        Transaction transaction = transactionRepository.findById(uuid)
                .orElseThrow(() -> new TransactionNotFoundException(uuid));
        User currentUser = getCurrentUser();
        if (!transaction.getUser().getUuid().equals(currentUser.getUuid())) {
            throw new AccessDeniedException("You do not own this transaction");
        }
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse update(String uuid, TransactionUpdateRequest request) {
        Transaction transaction = transactionRepository.findById(uuid)
                .orElseThrow(() -> new TransactionNotFoundException(uuid));
        User currentUser = getCurrentUser();
        if (!transaction.getUser().getUuid().equals(currentUser.getUuid())) {
            throw new AccessDeniedException("You do not own this transaction");
        }
        Category category = null;
        if (request.categoryUuid() != null) {
            category = categoryRepository.findById(request.categoryUuid())
                    .orElseThrow(() -> new CategoryNotFoundException(request.categoryUuid()));
        }
        transactionMapper.updateEntity(transaction, request, category);
        transaction = transactionRepository.save(transaction);
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public void delete(String uuid) {
        Transaction transaction = transactionRepository.findById(uuid)
                .orElseThrow(() -> new TransactionNotFoundException(uuid));
        User currentUser = getCurrentUser();
        if (!transaction.getUser().getUuid().equals(currentUser.getUuid())) {
            throw new AccessDeniedException("You do not own this transaction");
        }
        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom cannot be after dateTo");
        }
        User currentUser = getCurrentUser();
        Tuple result = transactionRepository.getIncomeExpenseSum(currentUser.getUuid(), dateFrom, dateTo);
        BigDecimal totalIncome = result.get("totalIncome", BigDecimal.class);
        BigDecimal totalExpense = result.get("totalExpense", BigDecimal.class);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;
        BigDecimal balance = totalIncome.subtract(totalExpense);
        return new BalanceResponse(totalIncome, totalExpense, balance);
    }

    public AnalyticsResponse getCategorySummary(LocalDate dateFrom, LocalDate dateTo, String type) {
        if (dateFrom == null || dateTo == null) {
            throw new IllegalArgumentException("dateFrom and dateTo are required");
        }
        if (dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom cannot be after dateTo");
        }
        if (type == null || type.isBlank()) {
            type = "EXPENSE";
        } else if ("INCOME".equalsIgnoreCase(type)) {
            type = "INCOME";
        } else if ("EXPENSE".equalsIgnoreCase(type)) {
            type = "EXPENSE";
        } else {
            throw new IllegalArgumentException("type must be INCOME or EXPENSE");
        }

        User currentUser = getCurrentUser();
        List<Object[]> rows = transactionRepository.getCategorySummary(currentUser.getUuid(), dateFrom, dateTo, type);
        BigDecimal grandTotal = BigDecimal.ZERO;
        List<AnalyticsItem> items = new ArrayList<>();
        for (Object[] row : rows) {
            String categoryUuid = (String) row[0];
            String categoryName = (String) row[1];
            BigDecimal total = (BigDecimal) row[2];
            grandTotal = grandTotal.add(total);
            items.add(new AnalyticsItem(categoryUuid, categoryName, total, null));
        }
        if (grandTotal.compareTo(BigDecimal.ZERO) == 0) {
            for (int i = 0; i < items.size(); i++) {
                AnalyticsItem item = items.get(i);
                items.set(i, new AnalyticsItem(item.categoryUuid(), item.categoryName(), item.total(), BigDecimal.ZERO));
            }
        } else {
            for (int i = 0; i < items.size(); i++) {
                AnalyticsItem item = items.get(i);
                BigDecimal percent = item.total()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(grandTotal, 2, RoundingMode.HALF_UP);
                items.set(i, new AnalyticsItem(item.categoryUuid(), item.categoryName(), item.total(), percent));
            }
        }
        return new AnalyticsResponse(dateFrom, dateTo, items, grandTotal);
    }
}
