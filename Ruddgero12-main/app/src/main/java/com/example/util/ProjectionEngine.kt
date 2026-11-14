package com.example.util

import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

data class MonthProjection(
    val monthYear: String, // "YYYY-MM"
    val monthDisplayName: String, // "Agosto 2026"
    val projectedIncome: Double,
    val fixedExpenses: Double,
    val estimatedVariableExpenses: Double,
    val totalProjectedExpense: Double,
    val projectedBalance: Double,
    val cumulativeSavings: Double
)

object ProjectionEngine {

    fun calculate6MonthProjection(
        currentIncome: Double,
        recurringExpenses: List<RecurringExpenseEntity>,
        recentTransactions: List<TransactionEntity>
    ): List<MonthProjection> {
        val calendar = Calendar.getInstance()
        val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val displayFormat = SimpleDateFormat("MMM/yyyy", Locale("pt", "BR"))

        val fixedTotal = recurringExpenses.filter { it.isActive }.sumOf { it.estimatedAmount }

        // Estimate average variable expenses per category from history or fallback defaults
        val historicalExpenseTotal = recentTransactions
            .filter { it.type == TransactionType.EXPENSE && !it.isRecurring }
            .sumOf { it.amount }
        
        val estimatedVariable = if (historicalExpenseTotal > 0) {
            (historicalExpenseTotal / 2.0).coerceAtLeast(800.0)
        } else {
            1200.0
        }

        val baseIncome = if (currentIncome > 0) currentIncome else 6500.0
        val projections = mutableListOf<MonthProjection>()
        var runningSavings = 0.0

        for (i in 1..6) {
            calendar.add(Calendar.MONTH, 1)
            val monthYear = monthYearFormat.format(calendar.time)
            val monthDisplayName = displayFormat.format(calendar.time).replaceFirstChar { it.uppercase() }

            // Slight seasonal adjustments (e.g. December bonus / expenses)
            val monthInt = calendar.get(Calendar.MONTH)
            val seasonalFactor = if (monthInt == Calendar.DECEMBER) 1.25 else 1.0
            val monthIncome = if (monthInt == Calendar.DECEMBER) baseIncome * 1.3 else baseIncome

            val monthVariable = estimatedVariable * seasonalFactor
            val monthTotalExpense = fixedTotal + monthVariable
            val monthBalance = monthIncome - monthTotalExpense

            runningSavings += monthBalance

            projections.add(
                MonthProjection(
                    monthYear = monthYear,
                    monthDisplayName = monthDisplayName,
                    projectedIncome = monthIncome,
                    fixedExpenses = fixedTotal,
                    estimatedVariableExpenses = monthVariable,
                    totalProjectedExpense = monthTotalExpense,
                    projectedBalance = monthBalance,
                    cumulativeSavings = runningSavings
                )
            )
        }

        return projections
    }
}
