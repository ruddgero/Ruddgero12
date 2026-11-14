package com.example.data.dao

import androidx.room.*
import com.example.data.model.RecurringExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1 ORDER BY dueDayOfMonth ASC")
    fun getActiveRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses ORDER BY dueDayOfMonth ASC")
    fun getAllRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpense(expense: RecurringExpenseEntity): Long

    @Update
    suspend fun updateRecurringExpense(expense: RecurringExpenseEntity)

    @Delete
    suspend fun deleteRecurringExpense(expense: RecurringExpenseEntity)

    @Query("SELECT * FROM recurring_expenses")
    suspend fun getAllRecurringExpensesSync(): List<RecurringExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<RecurringExpenseEntity>)

    @Query("DELETE FROM recurring_expenses")
    suspend fun clearAllRecurringExpenses()
}
