package com.budget.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class UserCreateRequest {

    @NotBlank(message = "Login is required")
    private String login;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String mail;

    @NotBlank(message = "Password is required")
    private String password;
}