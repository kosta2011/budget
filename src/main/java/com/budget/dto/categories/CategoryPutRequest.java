package com.budget.dto.categories;

import jakarta.validation.constraints.NotBlank;

public record CategoryPutRequest(@NotBlank(message = "Category name is required") String name) {
}
