package com.coffeecalculator.repository;

import com.coffeecalculator.model.OperatingExpenses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OperatingExpenseRepository extends JpaRepository<OperatingExpenses, Long> {

    List<OperatingExpenses> findByCategory(String category);

    // Fixed: Changed method name to match field 'isFixed' correctly (Spring handles boolean naming tricky sometimes)
    List<OperatingExpenses> findByIsFixed(boolean isFixed);

    // FIXED: Changed "OperatingExpense" to "OperatingExpenses" (Added 's')
    @Query("SELECT SUM(e.monthlyAmount) FROM OperatingExpenses e")
    BigDecimal getTotalMonthlyExpenses();

    // FIXED: Changed "OperatingExpense" to "OperatingExpenses" (Added 's')
    @Query("SELECT DISTINCT e.category FROM OperatingExpenses e ORDER BY e.category")
    List<String> findAllDistinctCategories();
}