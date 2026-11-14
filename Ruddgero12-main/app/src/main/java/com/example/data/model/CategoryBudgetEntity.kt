package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthYear: String, // "YYYY-MM"
    val categoryName: String,
    val budgetLimit: Double
)
