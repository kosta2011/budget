package com.budget.controller;

import com.budget.dto.categories.*;
import com.budget.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryCreateResponse create(@Valid @RequestBody CategoryCreateRequest request) {
        return categoryService.create(request);
    }

    @GetMapping
    public List<CategoryGetResponse> getCategories() {
        return categoryService.getAll();
    }

    @PutMapping("/{uuid}")
    public CategoryPutResponse update(@PathVariable String uuid, @Valid @RequestBody CategoryPutRequest request) {
        return categoryService.update(uuid, request);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String uuid) {
        categoryService.delete(uuid);
    }
}
