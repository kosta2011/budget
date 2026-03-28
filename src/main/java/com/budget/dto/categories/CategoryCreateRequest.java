package com.budget.dto.categories;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequest(@NotBlank(message = "Category name is required") String name) {

}
