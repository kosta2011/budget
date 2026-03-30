package com.budget.exception;

public class CategoryNotFoundException extends RuntimeException {
  public CategoryNotFoundException(String uuid) {
    super("Category not found: " + uuid);
  }
}
