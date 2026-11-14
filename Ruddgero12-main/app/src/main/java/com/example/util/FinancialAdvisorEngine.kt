package com.example.util

import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.text.NumberFormat
import java.util.*

data class FinancialInsight(
    val title: String,
    val description: String,
    val type: InsightType,
    val iconEmoji: String
)

enum class InsightType {
    ALERT, TIP, SUCCESS, INFO
}

object FinancialAdvisorEngine {

    fun generateInsights(
        income: Double,
        expenses: Double,
        transactions: List<TransactionEntity>,
        budgets: List<CategoryBudgetEntity>,
        recurring: List<RecurringExpenseEntity>
    ): List<FinancialInsight> {
        val insights = mutableListOf<FinancialInsight>()
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        // 1. Balanço e Poupança
        if (income > 0) {
            val savingsRate = ((income - expenses) / income) * 100
            if (savingsRate >= 20.0) {
                insights.add(
                    FinancialInsight(
                        title = "Excelente taxa de poupança!",
                        description = "Você está economizando %.1f%% da sua renda este mês. Continue assim para atingir suas metas mais rápido.".format(savingsRate),
                        type = InsightType.SUCCESS,
                        iconEmoji = "🎉"
                    )
                )
            } else if (savingsRate < 0) {
                val deficit = expenses - income
                insights.add(
                    FinancialInsight(
                        title = "Alerta de Orçamento Negativo",
                        description = "Suas despesas ultrapassaram suas receitas em ${currencyFormat.format(deficit)}. Revise gastos variáveis e evite juros do cartão.",
                        type = InsightType.ALERT,
                        iconEmoji = "⚠️"
                    )
                )
            }
        }

        // 2. Alertas de Limite por Categoria
        val categoryExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        budgets.forEach { budget ->
            val spent = categoryExpenses[budget.categoryName] ?: 0.0
            if (budget.budgetLimit > 0) {
                val percent = (spent / budget.budgetLimit) * 100
                if (percent >= 100.0) {
                    insights.add(
                        FinancialInsight(
                            title = "Limite estourado: ${budget.categoryName}",
                            description = "Você já gastou ${currencyFormat.format(spent)} dos ${currencyFormat.format(budget.budgetLimit)} planejados (%.0f%%).".format(percent),
                            type = InsightType.ALERT,
                            iconEmoji = "🚨"
                        )
                    )
                } else if (percent >= 80.0) {
                    insights.add(
                        FinancialInsight(
                            title = "Atenção em ${budget.categoryName}",
                            description = "Você atingiu %.0f%% do teto estipulado. Restam apenas ${currencyFormat.format(budget.budgetLimit - spent)} para o restante do mês.".format(percent),
                            type = InsightType.TIP,
                            iconEmoji = "⚡"
                        )
                    )
                }
            }
        }

        // 3. Impacto de Gastos Recorrentes Fixos
        val totalRecurring = recurring.filter { it.isActive }.sumOf { it.estimatedAmount }
        if (income > 0 && totalRecurring > 0) {
            val fixedRate = (totalRecurring / income) * 100
            if (fixedRate > 50.0) {
                insights.add(
                    FinancialInsight(
                        title = "Comprometimento com Custos Fixos",
                        description = "%.0f%% da sua renda já está comprometida com contas fixas (${currencyFormat.format(totalRecurring)}). Tente negociar assinaturas ou planos de internet.".format(fixedRate),
                        type = InsightType.INFO,
                        iconEmoji = "📌"
                    )
                )
            }
        }

        // 4. Insight Padrão de Boas Práticas se tiver poucos dados
        if (insights.isEmpty()) {
            insights.add(
                FinancialInsight(
                    title = "Regra 50-30-20 Recomendada",
                    description = "Procure direcionar 50% da renda para necessidades essenciais, 30% para estilo de vida e 20% para investimentos e reserva de emergência.",
                    type = InsightType.TIP,
                    iconEmoji = "💡"
                )
            )
        }

        return insights
    }
}

