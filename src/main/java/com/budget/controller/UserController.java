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
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PutMapping("/telegram")
    public ResponseEntity<?> setTelegramChatId(@RequestBody Map<String, String> payload) {
        User user = getCurrentUser();
        user.setTelegramChatId(payload.get("chatId"));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/limit")
    public ResponseEntity<?> setExpenseLimit(@RequestBody Map<String, BigDecimal> payload) {
        User user = getCurrentUser();
        user.setExpenseLimit(payload.get("limit"));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

}
