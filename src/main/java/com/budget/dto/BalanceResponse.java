package com.budget.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance
) {}

