package com.budget.service;

import com.budget.dto.categories.*;
import com.budget.entity.Category;
import com.budget.mapper.CategoryMapper;
import com.budget.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryCreateResponse create(CategoryCreateRequest request) {
        Category entity = categoryMapper.toEntity(request);

        Category savedCategory = categoryRepository.save(entity);

        return categoryMapper.toResponse(savedCategory);
    }

    public List<CategoryGetResponse> getAll() {
        List<Category> allCategories = categoryRepository.findAll();
        return categoryMapper.toCategoryGet(allCategories);
    }

    public CategoryPutResponse update(String uuid, CategoryPutRequest request) {
        Category category = categoryRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Категория с uuid " + uuid + " не найдена"));
        category.setName(request.name());   // если CategoryPutRequest — record
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryPutResponse(savedCategory);
    }

    public void delete(String uuid) {
        Category category = categoryRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Категория с uuid " + uuid + " не найдена"));
        categoryRepository.delete(category);
    }
}
