package com.budget.dto.transactions;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionCreateRequest(
        String categoryUuid,    // может быть null
        @Positive(message = "Amount must be positive")
        BigDecimal amount,
        @NotBlank(message = "Type is required")
        @Pattern(regexp = "INCOME|EXPENSE", message = "Type must be INCOME or EXPENSE")
        String type,
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,
        @NotNull(message = "Date is required")
        @PastOrPresent(message = "Date must be in the past or present")
        LocalDate date
) {}
