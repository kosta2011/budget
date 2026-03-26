package com.budget.mapper;

import com.budget.dto.UserCreateRequest;
import com.budget.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Преобразует UserCreateRequest в User
    @Mapping(target = "uuid", ignore = true)          // uuid будет сгенерирован в конструкторе
    @Mapping(target = "createdAt", ignore = true)     // заполнятся автоматически
    @Mapping(target = "updatedAt", ignore = true)     // заполнятся автоматически
    User toEntity(UserCreateRequest request);
}