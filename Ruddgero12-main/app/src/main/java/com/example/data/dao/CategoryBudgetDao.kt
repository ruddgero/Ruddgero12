package com.example.data.dao

import androidx.room.*
import com.example.data.model.CategoryBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryBudgetDao {
    @Query("SELECT * FROM category_budgets WHERE monthYear = :monthYear")
    fun getBudgetsForMonth(monthYear: String): Flow<List<CategoryBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: CategoryBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<CategoryBudgetEntity>)

    @Query("DELETE FROM category_budgets WHERE id = :id")
    suspend fun deleteBudget(id: Long)

    @Query("SELECT * FROM category_budgets")
    suspend fun getAllBudgetsSync(): List<CategoryBudgetEntity>

    @Query("DELETE FROM category_budgets")
    suspend fun clearAllBudgets()
}
