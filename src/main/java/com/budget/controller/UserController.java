package com.budget.controller;

import com.budget.dto.UserCreateRequest;
import com.budget.dto.UserResponse;
import com.budget.entity.User;
import com.budget.repository.UserRepository;
import com.budget.mapper.UserMapper;
import com.budget.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = userService.createUser(request);
        return userMapper.toResponse(user);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new IllegalStateException("Principal is not of type User");
    }

    @PutMapping("/telegram")
    public ResponseEntity<?> setTelegramChatId(@RequestBody Map<String, String> payload) {
        String chatId = payload.get("chatId");
        if (chatId == null || chatId.isBlank()) {
            return ResponseEntity.badRequest().body("chatId must be non-blank");
        }
        User user = getCurrentUser();
        user.setTelegramChatId(chatId);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/limit")
    public ResponseEntity<?> setExpenseLimit(@RequestBody Map<String, BigDecimal> payload) {
        BigDecimal limit = payload.get("limit");
        if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("limit must be positive");
        }
        User user = getCurrentUser();
        user.setExpenseLimit(limit);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

}
