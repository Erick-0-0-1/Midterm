package com.coffeecalculator.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Recipe Entity - Represents coffee drink recipes
 * Demonstrates: Variables, Loops, Conditional Statements, Switch Statements
 */
@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Recipe name is required")
    @Column(nullable = false)
    private String drinkName;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @NotNull(message = "Target margin is required")
    @Positive(message = "Target margin must be positive")
    @Column(nullable = false)
    private BigDecimal targetMarginPercent; // User input: desired profit margin %

    @Column(nullable = false)
    private BigDecimal suggestedSellingPrice = BigDecimal.ZERO; // Calculated based on margin

    @Column(nullable = false)
    private BigDecimal grossProfit = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal actualMarginPercent = BigDecimal.ZERO;

    // NEW: Operating expense allocation fields
    @Column(nullable = false)
    private BigDecimal allocatedExpensePerItem = BigDecimal.ZERO; // Overhead cost per drink

    @Column(nullable = false)
    private BigDecimal netProfit = BigDecimal.ZERO; // Gross profit - allocated expenses

    @Column(nullable = false)
    private BigDecimal netMarginPercent = BigDecimal.ZERO; // Net profit / Selling price

    @Column(nullable = false)
    private BigDecimal finalSellingPrice = BigDecimal.ZERO; // Price including overhead

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 1000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate all costs and pricing
     * Demonstrates: LOOPS, CONDITIONAL STATEMENTS, VARIABLES
     */
    public void calculateCosts() {
        // Initialize total cost variable
        BigDecimal calculatedTotalCost = BigDecimal.ZERO;

        // LOOP DEMONSTRATION: Iterate through all ingredients
        for (RecipeIngredient recipeIngredient : ingredients) {
            // Conditional: Check if ingredient data is valid
            if (recipeIngredient.getIngredient() != null &&
                    recipeIngredient.getQuantity() != null) {

                // Calculate line cost for each ingredient
                BigDecimal lineCost = recipeIngredient.getIngredient()
                        .getCostPerBaseUnit()
                        .multiply(recipeIngredient.getQuantity())
                        .setScale(2, RoundingMode.HALF_UP);

                // Accumulate total cost
                calculatedTotalCost = calculatedTotalCost.add(lineCost);
            }
        }

        // Set the total cost
        this.totalCost = calculatedTotalCost;

        // Calculate suggested selling price based on target margin
        calculateSuggestedSellingPrice();
    }

    /**
     * Calculate suggested selling price to achieve target margin
     * Formula: Selling Price = Cost / (1 - (Target Margin % / 100))
     * Demonstrates: CONDITIONAL STATEMENTS, MATHEMATICAL OPERATIONS
     */
    public void calculateSuggestedSellingPrice() {
        // Conditional: Check if total cost and margin are valid
        if (totalCost != null && totalCost.compareTo(BigDecimal.ZERO) > 0 &&
                targetMarginPercent != null && targetMarginPercent.compareTo(BigDecimal.ZERO) > 0) {

            // Convert percentage to decimal
            BigDecimal marginDecimal = targetMarginPercent.divide(
                    new BigDecimal("100"), 4, RoundingMode.HALF_UP);

            // Calculate: Selling Price = Cost / (1 - Margin)
            BigDecimal divisor = BigDecimal.ONE.subtract(marginDecimal);

            // Conditional: Prevent division by zero or invalid margin
            if (divisor.compareTo(BigDecimal.ZERO) > 0) {
                this.suggestedSellingPrice = totalCost.divide(divisor, 2, RoundingMode.HALF_UP);

                // Calculate gross profit
                this.grossProfit = suggestedSellingPrice.subtract(totalCost)
                        .setScale(2, RoundingMode.HALF_UP);

                // Calculate actual margin achieved
                if (suggestedSellingPrice.compareTo(BigDecimal.ZERO) > 0) {
                    this.actualMarginPercent = grossProfit.divide(suggestedSellingPrice, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .setScale(2, RoundingMode.HALF_UP);
                }
            } else {
                // Invalid margin (100% or more)
                this.suggestedSellingPrice = totalCost;
                this.grossProfit = BigDecimal.ZERO;
                this.actualMarginPercent = BigDecimal.ZERO;
            }
        }
    }

    /**
     * Calculate net profit including operating expense allocation
     * NEW METHOD: Calculates final price that covers both ingredients AND overhead
     * Demonstrates: CONDITIONAL LOGIC, MATHEMATICAL OPERATIONS
     */
    public void calculateNetProfitWithExpenses(BigDecimal expensePerItem) {
        // Store the allocated expense
        this.allocatedExpensePerItem = expensePerItem != null ? expensePerItem : BigDecimal.ZERO;

        // Calculate net profit = Gross Profit - Allocated Expenses
        this.netProfit = this.grossProfit.subtract(this.allocatedExpensePerItem)
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate net margin percentage
        // CONDITIONAL: Prevent division by zero
        if (suggestedSellingPrice.compareTo(BigDecimal.ZERO) > 0) {
            this.netMarginPercent = netProfit.divide(suggestedSellingPrice, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.netMarginPercent = BigDecimal.ZERO;
        }

        // Calculate final selling price that covers ingredients + overhead + target margin
        // Formula: Final Price = (Total Cost + Expense Per Item) / (1 - Target Margin)
        BigDecimal totalCostWithOverhead = this.totalCost.add(this.allocatedExpensePerItem);

        if (targetMarginPercent != null && targetMarginPercent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal marginDecimal = targetMarginPercent.divide(
                    new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            BigDecimal divisor = BigDecimal.ONE.subtract(marginDecimal);

            if (divisor.compareTo(BigDecimal.ZERO) > 0) {
                this.finalSellingPrice = totalCostWithOverhead.divide(divisor, 2, RoundingMode.HALF_UP);
            } else {
                this.finalSellingPrice = totalCostWithOverhead;
            }
        } else {
            this.finalSellingPrice = this.suggestedSellingPrice;
        }
    }

    /**
     * Get profitability status based on net margin
     * Demonstrates: CONDITIONAL STATEMENTS (if-else chain)
     */
    public String getProfitabilityStatus() {
        if (netMarginPercent == null || netMarginPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return "Unprofitable";
        } else if (netMarginPercent.compareTo(new BigDecimal("10")) < 0) {
            return "Low Profit";
        } else if (netMarginPercent.compareTo(new BigDecimal("20")) < 0) {
            return "Moderate Profit";
        } else if (netMarginPercent.compareTo(new BigDecimal("30")) < 0) {
            return "Good Profit";
        } else {
            return "Excellent Profit";
        }
    }

    /**
     * Add ingredient to recipe
     * Demonstrates: CONDITIONAL LOGIC, LIST OPERATIONS
     */
    public void addIngredient(RecipeIngredient recipeIngredient) {
        // Conditional: Validate ingredient before adding
        if (recipeIngredient != null && recipeIngredient.getIngredient() != null) {
            ingredients.add(recipeIngredient);
            recipeIngredient.setRecipe(this);
            calculateCosts(); // Recalculate after adding
        }
    }

    /**
     * Remove ingredient from recipe
     * Demonstrates: CONDITIONAL LOGIC, LIST OPERATIONS
     */
    public void removeIngredient(RecipeIngredient recipeIngredient) {
        if (recipeIngredient != null) {
            ingredients.remove(recipeIngredient);
            recipeIngredient.setRecipe(null);
            calculateCosts(); // Recalculate after removing
        }
    }

    /**
     * Get recipe complexity level based on ingredient count
     * Demonstrates: SWITCH STATEMENT, CONDITIONAL LOGIC
     */
    public String getComplexityLevel() {
        int ingredientCount = ingredients.size();

        // SWITCH STATEMENT DEMONSTRATION
        switch (ingredientCount) {
            case 0:
            case 1:
            case 2:
                return "Simple";
            case 3:
            case 4:
            case 5:
                return "Moderate";
            case 6:
            case 7:
            case 8:
                return "Complex";
            default:
                return "Very Complex";
        }
    }

    /**
     * Get pricing category based on selling price
     * Demonstrates: CONDITIONAL STATEMENTS (if-else chain)
     */
    public String getPricingCategory() {
        if (suggestedSellingPrice == null) {
            return "Unknown";
        }

        // Conditional chain for pricing categories
        if (suggestedSellingPrice.compareTo(new BigDecimal("100")) < 0) {
            return "Budget";
        } else if (suggestedSellingPrice.compareTo(new BigDecimal("150")) < 0) {
            return "Standard";
        } else if (suggestedSellingPrice.compareTo(new BigDecimal("200")) < 0) {
            return "Premium";
        } else {
            return "Luxury";
        }
    }

    /**
     * Validate recipe completeness
     * Demonstrates: CONDITIONAL LOGIC, BOOLEAN OPERATIONS
     */
    public boolean isComplete() {
        // Multiple conditional checks
        boolean hasName = drinkName != null && !drinkName.trim().isEmpty();
        boolean hasIngredients = ingredients != null && !ingredients.isEmpty();
        boolean hasMargin = targetMarginPercent != null &&
                targetMarginPercent.compareTo(BigDecimal.ZERO) > 0;

        return hasName && hasIngredients && hasMargin;
    }

    /**
     * Get ingredient count by category
     * Demonstrates: LOOPS, CONDITIONAL STATEMENTS, SWITCH
     */
    public int getIngredientCountByCategory(String category) {
        int count = 0;

        // Loop through all ingredients
        for (RecipeIngredient ri : ingredients) {
            // Conditional check
            if (ri.getIngredient() != null &&
                    ri.getIngredient().getCategory().equalsIgnoreCase(category)) {
                count++;
            }
        }

        return count;
    }
}