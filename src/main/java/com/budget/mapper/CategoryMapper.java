package com.budget.mapper;

import com.budget.dto.categories.CategoryCreateRequest;
import com.budget.dto.categories.CategoryCreateResponse;
import com.budget.dto.categories.CategoryGetResponse;
import com.budget.dto.categories.CategoryPutResponse;
import com.budget.entity.Category;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    DateTimeFormatter API_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    CategoryCreateResponse toResponse(Category category);

    List<CategoryGetResponse> toCategoryGet(List<Category> categories);

    Category toEntity(CategoryCreateRequest request);

    CategoryPutResponse toCategoryPutResponse(Category category);

    default String map(LocalDateTime value) {
        return value == null ? null : value.format(API_DATE_TIME);

    }
}
