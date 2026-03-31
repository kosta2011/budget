package com.budget.mapper;

import com.budget.dto.transactions.TransactionCreateRequest;
import com.budget.dto.transactions.TransactionResponse;
import com.budget.dto.transactions.TransactionUpdateRequest;
import com.budget.entity.Category;
import com.budget.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "transactionDate", source = "request.date")
    Transaction toEntity(TransactionCreateRequest request, Category category);

    @Mapping(target = "categoryUuid", source = "category.uuid")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "date", source = "transactionDate")
    @Mapping(target = "createdAt", source = "createdAt")
    TransactionResponse toResponse(Transaction transaction);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "transactionDate", source = "request.date")
    void updateEntity(@MappingTarget Transaction transaction, TransactionUpdateRequest request, Category category);
}