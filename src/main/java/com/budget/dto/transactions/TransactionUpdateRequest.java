package com.budget.dto.transactions;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionUpdateRequest(
        String categoryUuid,
        @Positive BigDecimal amount,
        @NotBlank @Pattern(regexp = "INCOME|EXPENSE") String type,
        @Size(max = 500) String description,
        @NotNull @PastOrPresent LocalDate date
) {}
