package com.budget.controller;

import com.budget.dto.UserCreateRequest;
import com.budget.dto.UserResponse;
import com.budget.entity.User;
import com.budget.mapper.UserMapper;
import com.budget.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = userService.createUser(request);
        return userMapper.toResponse(user);
    }
}
