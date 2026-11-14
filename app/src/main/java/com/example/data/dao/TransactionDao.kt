package com.example.data.dao

import androidx.room.*
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE monthYear = :monthYear ORDER BY dateMillis DESC")
    fun getTransactionsForMonth(monthYear: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 20): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE monthYear = :monthYear AND type = :type")
    fun getTotalAmountByMonthAndType(monthYear: String, type: TransactionType): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE monthYear = :monthYear AND type = 'EXPENSE' GROUP BY category")
    fun getCategoryTotalsForMonth(monthYear: String): Flow<List<CategoryTotalPair>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE monthYear = :monthYear ORDER BY dateMillis DESC")
    suspend fun getTransactionsForMonthSync(monthYear: String): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE title = :title AND ABS(amount - :amount) < 0.01 AND dateMillis >= :sinceMillis")
    suspend fun countRecentDuplicates(title: String, amount: Double, sinceMillis: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}

data class CategoryTotalPair(
    val category: String,
    val total: Double
)
