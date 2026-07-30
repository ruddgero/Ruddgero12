package com.example.data.repository

import com.example.data.dao.CategoryBudgetDao
import com.example.data.dao.CategoryTotalPair
import com.example.data.dao.NotificationLogDao
import com.example.data.dao.RecurringExpenseDao
import com.example.data.dao.TransactionDao
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.NotificationLogEntity
import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val categoryBudgetDao: CategoryBudgetDao,
    private val recurringExpenseDao: RecurringExpenseDao,
    private val notificationLogDao: NotificationLogDao
) {
    fun getTransactionsForMonth(monthYear: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForMonth(monthYear)
    }

    fun getRecentTransactions(limit: Int = 15): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentTransactions(limit)
    }

    fun getTotalIncomeForMonth(monthYear: String): Flow<Double?> {
        return transactionDao.getTotalAmountByMonthAndType(monthYear, TransactionType.INCOME)
    }

    fun getTotalExpenseForMonth(monthYear: String): Flow<Double?> {
        return transactionDao.getTotalAmountByMonthAndType(monthYear, TransactionType.EXPENSE)
    }

    fun getCategoryTotalsForMonth(monthYear: String): Flow<List<CategoryTotalPair>> {
        return transactionDao.getCategoryTotalsForMonth(monthYear)
    }

    fun getBudgetsForMonth(monthYear: String): Flow<List<CategoryBudgetEntity>> {
        return categoryBudgetDao.getBudgetsForMonth(monthYear)
    }

    fun getActiveRecurringExpenses(): Flow<List<RecurringExpenseEntity>> {
        return recurringExpenseDao.getActiveRecurringExpenses()
    }

    fun getAllRecurringExpenses(): Flow<List<RecurringExpenseEntity>> {
        return recurringExpenseDao.getAllRecurringExpenses()
    }

    fun getRecentNotificationLogs(): Flow<List<NotificationLogEntity>> {
        return notificationLogDao.getRecentNotificationLogs()
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun saveCategoryBudget(budget: CategoryBudgetEntity) {
        categoryBudgetDao.insertOrUpdateBudget(budget)
    }

    suspend fun insertRecurringExpense(expense: RecurringExpenseEntity): Long {
        return recurringExpenseDao.insertRecurringExpense(expense)
    }

    suspend fun deleteRecurringExpense(expense: RecurringExpenseEntity) {
        recurringExpenseDao.deleteRecurringExpense(expense)
    }

    suspend fun insertNotificationLog(log: NotificationLogEntity): Long {
        return notificationLogDao.insertLog(log)
    }

    suspend fun clearNotificationLogs() {
        notificationLogDao.clearLogs()
    }

    suspend fun getTransactionsForMonthSync(monthYear: String): List<TransactionEntity> {
        return transactionDao.getTransactionsForMonthSync(monthYear)
    }
}
