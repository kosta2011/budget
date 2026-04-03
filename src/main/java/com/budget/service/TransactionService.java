package com.budget.service;

import com.budget.dto.BalanceResponse;
import com.budget.dto.transactions.TransactionCreateRequest;
import com.budget.dto.transactions.TransactionResponse;
import com.budget.dto.transactions.TransactionUpdateRequest;
import com.budget.entity.Category;
import com.budget.entity.Transaction;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Tuple;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final EntityManager entityManager;

    @Transactional
    public TransactionResponse create(TransactionCreateRequest request) {
        Category category = null;
        if (request.categoryUuid() != null) {
            category = categoryRepository.findById(request.categoryUuid())
                    .orElseThrow(() -> new CategoryNotFoundException(request.categoryUuid()));
        }
        Transaction transaction = transactionMapper.toEntity(request, category);
        transaction = transactionRepository.saveAndFlush(transaction);
        entityManager.refresh(transaction);
        return transactionMapper.toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAll(String categoryUuid, String type,
                                            LocalDate dateFrom, LocalDate dateTo,
                                            Pageable pageable) {
        // Валидация диапазона дат
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom cannot be after dateTo");
        }

        // Начинаем с условия "всегда true" (конъюнкция)
        Specification<Transaction> spec = (root, query, cb) -> cb.conjunction();

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
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse update(String uuid, TransactionUpdateRequest request) {
        Transaction transaction = transactionRepository.findById(uuid)
                .orElseThrow(() -> new TransactionNotFoundException(uuid));
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
        transactionRepository.delete(transaction);
    }

    public BalanceResponse getBalance(LocalDate dateFrom, LocalDate dateTo) {
        // Проверка корректности диапазона дат
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom cannot be after dateTo");
        }
        Tuple result = transactionRepository.getIncomeExpenseSum(dateFrom, dateTo);
        BigDecimal totalIncome = result.get("totalIncome", BigDecimal.class);
        BigDecimal totalExpense = result.get("totalExpense", BigDecimal.class);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;
        BigDecimal balance = totalIncome.subtract(totalExpense);
        return new BalanceResponse(totalIncome, totalExpense, balance);
    }

}
