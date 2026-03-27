package com.budget.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @NotBlank(message = "Login is required") String login,
        @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String mail,
        @NotBlank(message = "Password is required") String password
) {}