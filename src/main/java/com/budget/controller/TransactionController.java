package com.budget.controller;

import com.budget.dto.BalanceResponse;
import com.budget.dto.transactions.TransactionCreateRequest;
import com.budget.dto.transactions.TransactionResponse;
import com.budget.dto.transactions.TransactionUpdateRequest;
import com.budget.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody TransactionCreateRequest request) {
        return transactionService.create(request);
    }

    @GetMapping
    public Page<TransactionResponse> getAll(
            @RequestParam(required = false) String categoryUuid,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return transactionService.getAll(categoryUuid, type, dateFrom, dateTo, pageable);
    }

    @GetMapping("/{uuid}")
    public TransactionResponse getByUuid(@PathVariable String uuid) {
        return transactionService.getByUuid(uuid);
    }

    @PutMapping("/{uuid}")
    public TransactionResponse update(@PathVariable String uuid, @Valid @RequestBody TransactionUpdateRequest request) {
        return transactionService.update(uuid, request);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String uuid) {
        transactionService.delete(uuid);
    }

    @GetMapping("/balance")
    public BalanceResponse getBalance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return transactionService.getBalance(dateFrom, dateTo);
    }

}
