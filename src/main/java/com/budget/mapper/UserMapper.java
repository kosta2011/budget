package com.budget.mapper;

import com.budget.dto.UserCreateRequest;
import com.budget.dto.UserResponse;
import com.budget.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);

    // Преобразует UserCreateRequest в User
    @Mapping(target = "uuid", ignore = true)          // uuid будет сгенерирован в конструкторе
    @Mapping(target = "createdAt", ignore = true)     // заполнятся автоматически
    @Mapping(target = "updatedAt", ignore = true)     // заполнятся автоматически
    User toEntity(UserCreateRequest request);
}