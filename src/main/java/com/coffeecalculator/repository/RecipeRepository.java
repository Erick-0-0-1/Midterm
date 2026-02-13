package com.coffeecalculator.repository;

import com.coffeecalculator.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Recipe entity
 * Provides database operations for recipes
 */
@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    /**
     * Find recipes by drink name containing (case-insensitive)
     */
    List<Recipe> findByDrinkNameContainingIgnoreCase(String drinkName);

    /**
     * Find all recipes ordered by name
     */
    @Query("SELECT r FROM Recipe r ORDER BY r.drinkName ASC")
    List<Recipe> findAllOrderedByName();

    /**
     * Find recipes with selling price in range
     */
    @Query("SELECT r FROM Recipe r WHERE r.suggestedSellingPrice BETWEEN :minPrice AND :maxPrice")
    List<Recipe> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find recipes with margin greater than specified
     */
    @Query("SELECT r FROM Recipe r WHERE r.actualMarginPercent >= :minMargin")
    List<Recipe> findByMinimumMargin(@Param("minMargin") BigDecimal minMargin);

    /**
     * Check if recipe name exists
     */
    boolean existsByDrinkNameIgnoreCase(String drinkName);

    /**
     * Get total number of recipes
     */
    @Query("SELECT COUNT(r) FROM Recipe r")
    long countTotalRecipes();

    /**
     * Get average selling price across all recipes
     */
    @Query("SELECT AVG(r.suggestedSellingPrice) FROM Recipe r")
    BigDecimal getAverageSellingPrice();
}