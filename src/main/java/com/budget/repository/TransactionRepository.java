package com.budget.repository;

import com.budget.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.Tuple;

public interface TransactionRepository extends JpaRepository<Transaction, String>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT " +
            "SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) AS totalIncome, " +
            "SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS totalExpense " +
            "FROM Transaction t " +
            "WHERE (:dateFrom IS NULL OR t.transactionDate >= :dateFrom) " +
            "AND (:dateTo IS NULL OR t.transactionDate <= :dateTo)")
    Tuple getIncomeExpenseSum(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

}
