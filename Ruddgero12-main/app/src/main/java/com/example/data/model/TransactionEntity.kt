package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE, // Saída
    INCOME   // Entrada
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val monthYear: String, // Format "YYYY-MM" (e.g., "2026-07")
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "Outros",
    val bankName: String? = null,
    val sourceNotification: String? = null,
    val isAutoCaptured: Boolean = false,
    val isRecurring: Boolean = false,
    val notes: String? = null
)
