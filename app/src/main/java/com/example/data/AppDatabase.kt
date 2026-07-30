package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Database(
    entities = [
        TransactionEntity::class,
        CategoryBudgetEntity::class,
        RecurringExpenseEntity::class,
        NotificationLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun notificationLogDao(): NotificationLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_control_db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: AppDatabase) {
                val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

                // Default Budgets for Current Month
                val defaultBudgets = listOf(
                    CategoryBudgetEntity(monthYear = currentMonth, categoryName = "Alimentação", budgetLimit = 1500.0),
                    CategoryBudgetEntity(monthYear = currentMonth, categoryName = "Moradia", budgetLimit = 2200.0),
                    CategoryBudgetEntity(monthYear = currentMonth, categoryName = "Transporte", budgetLimit = 600.0),
                    CategoryBudgetEntity(monthYear = currentMonth, categoryName = "Lazer", budgetLimit = 500.0),
                    CategoryBudgetEntity(monthYear = currentMonth, categoryName = "Saúde", budgetLimit = 400.0),
                    CategoryBudgetEntity(monthYear = currentMonth, categoryName = "Cartão de Crédito", budgetLimit = 1200.0),
                    CategoryBudgetEntity(monthYear = currentMonth, categoryName = "Outros", budgetLimit = 500.0)
                )
                db.categoryBudgetDao().insertAll(defaultBudgets)

                // Default Recurring Expenses
                val recurring = listOf(
                    RecurringExpenseEntity(title = "Aluguel Residencial", estimatedAmount = 1800.0, category = "Moradia", dueDayOfMonth = 5),
                    RecurringExpenseEntity(title = "Condomínio", estimatedAmount = 400.0, category = "Moradia", dueDayOfMonth = 10),
                    RecurringExpenseEntity(title = "Internet Fibra", estimatedAmount = 120.0, category = "Moradia", dueDayOfMonth = 15),
                    RecurringExpenseEntity(title = "Plano de Saúde", estimatedAmount = 350.0, category = "Saúde", dueDayOfMonth = 20),
                    RecurringExpenseEntity(title = "Assinatura Netflix & Spotify", estimatedAmount = 75.0, category = "Lazer", dueDayOfMonth = 25),
                    RecurringExpenseEntity(title = "Fatura Cartão Nubank", estimatedAmount = 950.0, category = "Cartão de Crédito", dueDayOfMonth = 28)
                )
                recurring.forEach { db.recurringExpenseDao().insertRecurringExpense(it) }

                // Default Sample Transactions for Current Month
                val sampleTransactions = listOf(
                    TransactionEntity(
                        title = "Salário Mensal",
                        amount = 6500.0,
                        monthYear = currentMonth,
                        type = TransactionType.INCOME,
                        category = "Salário",
                        notes = "Depósito via PIX empresa"
                    ),
                    TransactionEntity(
                        title = "Supermercado Carrefour",
                        amount = 458.90,
                        monthYear = currentMonth,
                        type = TransactionType.EXPENSE,
                        category = "Alimentação",
                        bankName = "Nubank",
                        isAutoCaptured = true,
                        sourceNotification = "Compra de R$ 458,90 aprovada no Carrefour via Nubank"
                    ),
                    TransactionEntity(
                        title = "Posto Shell - Combustível",
                        amount = 180.00,
                        monthYear = currentMonth,
                        type = TransactionType.EXPENSE,
                        category = "Transporte",
                        bankName = "Itaú",
                        isAutoCaptured = true,
                        sourceNotification = "Compra aprovada de R$ 180,00 no Cartão Itaú final 4912"
                    ),
                    TransactionEntity(
                        title = "Aluguel Mensal",
                        amount = 1800.00,
                        monthYear = currentMonth,
                        type = TransactionType.EXPENSE,
                        category = "Moradia",
                        isRecurring = true
                    ),
                    TransactionEntity(
                        title = "Drogaria SP",
                        amount = 64.50,
                        monthYear = currentMonth,
                        type = TransactionType.EXPENSE,
                        category = "Saúde",
                        bankName = "Bradesco",
                        isAutoCaptured = true,
                        sourceNotification = "Pix enviado de R$ 64,50 para Drogaria SP"
                    ),
                    TransactionEntity(
                        title = "Cinema e Jantar",
                        amount = 145.00,
                        monthYear = currentMonth,
                        type = TransactionType.EXPENSE,
                        category = "Lazer",
                        bankName = "Nubank"
                    )
                )
                sampleTransactions.forEach { db.transactionDao().insertTransaction(it) }

                // Default Notification Logs
                val sampleLogs = listOf(
                    NotificationLogEntity(
                        packageName = "com.nu.production",
                        bankName = "Nubank",
                        rawContent = "Compra de R$ 458,90 em Carrefour aprovada.",
                        extractedAmount = 458.90,
                        extractedTitle = "Carrefour"
                    ),
                    NotificationLogEntity(
                        packageName = "com.itau",
                        bankName = "Itaú",
                        rawContent = "Compra aprovada de R$ 180,00 no Cartão Itaú final 4912.",
                        extractedAmount = 180.00,
                        extractedTitle = "Posto Shell"
                    )
                )
                sampleLogs.forEach { db.notificationLogDao().insertLog(it) }
            }
        }
    }
}
