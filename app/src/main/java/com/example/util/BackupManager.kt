package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object BackupManager {

    suspend fun exportBackup(
        context: Context,
        uri: Uri,
        repository: FinanceRepository
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val transactions = repository.getAllTransactionsSync()
            val budgets = repository.getAllBudgetsSync()
            val recurring = repository.getAllRecurringExpensesSync()

            val rootJson = JSONObject().apply {
                put("version", 1)
                put("exportedAt", System.currentTimeMillis())

                // Transactions
                val txArray = JSONArray()
                transactions.forEach { tx ->
                    txArray.put(JSONObject().apply {
                        put("id", tx.id)
                        put("title", tx.title)
                        put("amount", tx.amount)
                        put("dateMillis", tx.dateMillis)
                        put("monthYear", tx.monthYear)
                        put("type", tx.type.name)
                        put("category", tx.category)
                        put("notes", tx.notes ?: "")
                        put("bankName", tx.bankName ?: "")
                        put("isAutoCaptured", tx.isAutoCaptured)
                        put("isRecurring", tx.isRecurring)
                    })
                }
                put("transactions", txArray)

                // Budgets
                val budgetArray = JSONArray()
                budgets.forEach { b ->
                    budgetArray.put(JSONObject().apply {
                        put("id", b.id)
                        put("monthYear", b.monthYear)
                        put("categoryName", b.categoryName)
                        put("budgetLimit", b.budgetLimit)
                    })
                }
                put("budgets", budgetArray)

                // Recurring Expenses
                val recArray = JSONArray()
                recurring.forEach { r ->
                    recArray.put(JSONObject().apply {
                        put("id", r.id)
                        put("title", r.title)
                        put("estimatedAmount", r.estimatedAmount)
                        put("category", r.category)
                        put("dueDayOfMonth", r.dueDayOfMonth)
                        put("isActive", r.isActive)
                    })
                }
                put("recurringExpenses", recArray)
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(rootJson.toString(2))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importBackup(
        context: Context,
        uri: Uri,
        repository: FinanceRepository
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure(Exception("Não foi possível ler o arquivo."))

            val rootJson = JSONObject(jsonString)
            val txArray = rootJson.optJSONArray("transactions") ?: JSONArray()
            val budgetArray = rootJson.optJSONArray("budgets") ?: JSONArray()
            val recArray = rootJson.optJSONArray("recurringExpenses") ?: JSONArray()

            val transactions = mutableListOf<TransactionEntity>()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                transactions.add(
                    TransactionEntity(
                        id = 0, // Auto-generate IDs
                        title = obj.getString("title"),
                        amount = obj.getDouble("amount"),
                        dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                        monthYear = obj.getString("monthYear"),
                        type = TransactionType.valueOf(obj.optString("type", "EXPENSE")),
                        category = obj.getString("category"),
                        notes = obj.optString("notes").ifBlank { null },
                        bankName = obj.optString("bankName").ifBlank { null },
                        isAutoCaptured = obj.optBoolean("isAutoCaptured", false),
                        isRecurring = obj.optBoolean("isRecurring", false)
                    )
                )
            }

            val budgets = mutableListOf<CategoryBudgetEntity>()
            for (i in 0 until budgetArray.length()) {
                val obj = budgetArray.getJSONObject(i)
                budgets.add(
                    CategoryBudgetEntity(
                        id = 0,
                        monthYear = obj.getString("monthYear"),
                        categoryName = obj.getString("categoryName"),
                        budgetLimit = obj.getDouble("budgetLimit")
                    )
                )
            }

            val recurring = mutableListOf<RecurringExpenseEntity>()
            for (i in 0 until recArray.length()) {
                val obj = recArray.getJSONObject(i)
                recurring.add(
                    RecurringExpenseEntity(
                        id = 0,
                        title = obj.getString("title"),
                        estimatedAmount = obj.getDouble("estimatedAmount"),
                        category = obj.getString("category"),
                        dueDayOfMonth = obj.optInt("dueDayOfMonth", 1),
                        isActive = obj.optBoolean("isActive", true)
                    )
                )
            }

            repository.restoreAllData(transactions, budgets, recurring)
            Result.success(transactions.size)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

