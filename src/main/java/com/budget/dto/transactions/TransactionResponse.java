package com.budget.dto.transactions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(
        String uuid,
        String categoryUuid,
        String categoryName,
        BigDecimal amount,
        String type,
        String description,
        LocalDate date,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
