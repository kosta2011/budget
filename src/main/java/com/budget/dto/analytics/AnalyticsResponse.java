package com.budget.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AnalyticsResponse(
        LocalDate dateFrom,
        LocalDate dateTo,
        List<AnalyticsItem> items,
        BigDecimal grandTotal
) {}