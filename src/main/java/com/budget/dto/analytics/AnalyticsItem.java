package com.budget.dto.analytics;

import java.math.BigDecimal;

public record AnalyticsItem(
        String categoryName,
        BigDecimal total,
        BigDecimal percent
) {}
