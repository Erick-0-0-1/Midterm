package com.coffeecalculator.repository;

import com.coffeecalculator.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Ingredient entity
 * Provides database operations for ingredients
 */
@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    /**
     * Find ingredients by category
     */
    List<Ingredient> findByCategory(String category);

    /**
     * Find ingredients by name containing (case-insensitive search)
     */
    List<Ingredient> findByNameContainingIgnoreCase(String name);

    /**
     * Find all ingredients ordered by category and name
     */
    @Query("SELECT i FROM Ingredient i ORDER BY i.category ASC, i.name ASC")
    List<Ingredient> findAllOrderedByCategoryAndName();

    /**
     * Find ingredients by base unit
     */
    List<Ingredient> findByBaseUnit(String baseUnit);

    /**
     * Check if ingredient name exists
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Get all distinct categories
     */
    @Query("SELECT DISTINCT i.category FROM Ingredient i ORDER BY i.category")
    List<String> findAllDistinctCategories();
}