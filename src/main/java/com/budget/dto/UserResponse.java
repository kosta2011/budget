package com.budget.dto;

import java.time.LocalDateTime;

public record UserResponse(
        String uuid,
        String login,
        String mail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
