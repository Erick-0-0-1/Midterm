package com.coffeecalculator.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * RecipeIngredient - Junction table for Recipe and Ingredient
 * Demonstrates: Variables, Conditional Logic
 */
@Entity
@Table(name = "recipe_ingredients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @JsonBackReference
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private BigDecimal quantity; // Quantity in base unit (g, ml, pc)

    @Column(nullable = false)
    private BigDecimal lineCost; // Calculated cost for this ingredient

    /**
     * Calculate line cost before persisting
     * Demonstrates: Conditional logic, mathematical operations
     */
    @PrePersist
    @PreUpdate
    public void calculateLineCost() {
        // Conditional: Check if ingredient and quantity are valid
        if (ingredient != null && ingredient.getCostPerBaseUnit() != null && quantity != null) {
            // Calculate line cost
            this.lineCost = ingredient.getCostPerBaseUnit()
                    .multiply(quantity)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.lineCost = BigDecimal.ZERO;
        }
    }

    /**
     * Get formatted quantity with unit
     */
    public String getFormattedQuantity() {
        if (ingredient != null) {
            return quantity + " " + ingredient.getBaseUnit();
        }
        return quantity.toString();
    }
}