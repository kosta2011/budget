package com.budget.service;

import com.budget.dto.UserCreateRequest;
import com.budget.entity.User;
import com.budget.mapper.UserMapper;
import com.budget.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(UserCreateRequest request) {
        // Проверка на уникальность логина
        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            throw new RuntimeException("Login already exists");
        }
        // Проверка на уникальность email
        if (userRepository.findByMail(request.getMail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Преобразуем DTO в Entity
        User user = userMapper.toEntity(request);

        // Шифруем пароль
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Сохраняем (uuid сгенерируется в конструкторе)
        return userRepository.save(user);
    }
}
