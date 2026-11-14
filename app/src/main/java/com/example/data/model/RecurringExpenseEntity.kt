package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val estimatedAmount: Double,
    val category: String,
    val dueDayOfMonth: Int, // 1 to 31
    val isActive: Boolean = true,
    val notes: String? = null
)
