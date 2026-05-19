package com.budget.repository;

import com.budget.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.Tuple;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT " +
            "SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) AS totalIncome, " +
            "SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS totalExpense " +
            "FROM Transaction t " +
            "WHERE t.user.uuid = :userId " +
            "AND (:dateFrom IS NULL OR t.transactionDate >= :dateFrom) " +
            "AND (:dateTo IS NULL OR t.transactionDate <= :dateTo)")
    Tuple getIncomeExpenseSum(@Param("userId") String userId,
                              @Param("dateFrom") LocalDate dateFrom,
                              @Param("dateTo") LocalDate dateTo);

    @Query("SELECT " +
            "c.uuid AS categoryUuid, " +
            "COALESCE(c.name, 'Без категории') AS categoryName, " +
            "SUM(t.amount) AS total " +
            "FROM Transaction t " +
            "LEFT JOIN t.category c " +
            "WHERE t.user.uuid = :userId " +
            "AND t.transactionDate BETWEEN :dateFrom AND :dateTo " +
            "AND t.type = :type " +
            "GROUP BY c.uuid, c.name " +
            "ORDER BY total DESC")
    List<Object[]> getCategorySummary(@Param("userId") String userId,
                                      @Param("dateFrom") LocalDate dateFrom,
                                      @Param("dateTo") LocalDate dateTo,
                                      @Param("type") String type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.uuid = :userId AND t.type = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getExpenseSumByUserAndDateRange(@Param("userId") String userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t WHERE t.user.uuid = :userId AND t.type = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findExpensesByUserAndDateRange(@Param("userId") String userId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t WHERE t.user.uuid = :userId AND t.type = 'EXPENSE' AND t.category.name = :categoryName AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findExpensesByUserAndCategory(@Param("userId") String userId,
                                                    @Param("categoryName") String categoryName,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

}
