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
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT " +
            "SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) AS totalIncome, " +
            "SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS totalExpense " +
            "FROM Transaction t " +
            "WHERE (:dateFrom IS NULL OR t.transactionDate >= :dateFrom) " +
            "AND (:dateTo IS NULL OR t.transactionDate <= :dateTo)")
    Tuple getIncomeExpenseSum(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

    @Query("SELECT " +
            "c.uuid AS categoryUuid, " +
            "COALESCE(c.name, 'Без категории') AS categoryName, " +
            "SUM(t.amount) AS total " +
            "FROM Transaction t " +
            "LEFT JOIN t.category c " +
            "WHERE t.transactionDate BETWEEN :dateFrom AND :dateTo " +
            "AND t.type = :type " +
            "GROUP BY c.uuid, c.name " +   // группировка по uuid и name
            "ORDER BY total DESC")
    List<Object[]> getCategorySummary(@Param("dateFrom") LocalDate dateFrom,
                                      @Param("dateTo") LocalDate dateTo,
                                      @Param("type") String type);

}
