package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val bankName: String,
    val rawContent: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val extractedAmount: Double?,
    val extractedTitle: String?,
    val isProcessed: Boolean = true,
    val transactionId: Long? = null
)
