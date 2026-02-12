package com.coffeecalculator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Operating Expense Entity - Represents monthly business expenses
 * Examples: Rent, Electricity, Gas, Water, Salaries, etc.
 * Demonstrates: Variables, Conditional Logic, Switch Statement
 */
@Entity
@Table(name = "operating_expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperatingExpenses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Expense name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category; // Rent, Utilities, Labor, Marketing, Equipment, Others

    @NotNull(message = "Monthly amount is required")
    @Positive(message = "Amount must be positive")
    @Column(nullable = false)
    private BigDecimal monthlyAmount; // Monthly cost in PHP

    @Column(name = "is_fixed", nullable = false)
    private boolean isFixed = true; // Fixed (rent) vs Variable (electricity)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 500)
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
     * Get expense category display name
     * Demonstrates: SWITCH STATEMENT
     */
    public String getCategoryDisplayName() {
        // SWITCH STATEMENT for category names
        switch (this.category.toLowerCase()) {
            case "rent":
                return "Rent / Lease";
            case "utilities":
                return "Utilities (Electric, Water, Gas)";
            case "labor":
                return "Labor / Salaries";
            case "marketing":
                return "Marketing / Advertising";
            case "equipment":
                return "Equipment / Maintenance";
            case "supplies":
                return "General Supplies";
            case "insurance":
                return "Insurance";
            case "taxes":
                return "Taxes / Permits";
            case "others":
                return "Other Expenses";
            default:
                return "Miscellaneous";
        }
    }

    /**
     * Validate if category is valid
     * Demonstrates: SWITCH STATEMENT, CONDITIONAL LOGIC
     */
    public boolean isValidCategory() {
        switch (this.category.toLowerCase()) {
            case "rent":
            case "utilities":
            case "labor":
            case "marketing":
            case "equipment":
            case "supplies":
            case "insurance":
            case "taxes":
            case "others":
                return true;
            default:
                return false;
        }
    }

    /**
     * Calculate daily expense allocation
     * Demonstrates: MATHEMATICAL OPERATIONS
     */
    public BigDecimal getDailyAmount() {
        // Assuming 30 days per month
        return monthlyAmount.divide(new BigDecimal("30"), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Get expense type description
     * Demonstrates: CONDITIONAL LOGIC
     */
    public String getExpenseType() {
        if (isFixed) {
            return "Fixed Expense";
        } else {
            return "Variable Expense";
        }
    }
}
