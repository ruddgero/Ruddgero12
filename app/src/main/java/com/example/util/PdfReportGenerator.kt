package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    fun generateMonthlyPdfReport(
        context: Context,
        monthYear: String,
        displayMonthName: String,
        transactions: List<TransactionEntity>,
        budgets: List<CategoryBudgetEntity>
    ): File? {
        val pdfDocument = PdfDocument()

        // Page info: A4 size (595 x 842 points)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Calculate Totals
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        // Paints
        val paintHeaderBg = Paint().apply { color = Color.parseColor("#0D47A1"); style = Paint.Style.FILL }
        val paintHeaderTitle = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintHeaderSub = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            textSize = 12f
        }
        val paintTitleSection = Paint().apply {
            color = Color.parseColor("#0D47A1")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintTextBold = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintTextRegular = Paint().apply {
            color = Color.parseColor("#333333")
            textSize = 9f
        }
        val paintGreen = Paint().apply {
            color = Color.parseColor("#2E7D32")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintRed = Paint().apply {
            color = Color.parseColor("#C62828")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintLine = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 1f
        }
        val paintCardBg = Paint().apply {
            color = Color.parseColor("#F5F5F5")
            style = Paint.Style.FILL
        }

        var yPos = 0f

        // Draw Header Banner
        canvas.drawRect(0f, 0f, 595f, 90f, paintHeaderBg)
        canvas.drawText("Controle Financeiro - Relatório Mensal", 30f, 40f, paintHeaderTitle)
        canvas.drawText("Período de Referência: $displayMonthName ($monthYear)", 30f, 65f, paintHeaderSub)

        // Draw KPI Cards
        yPos = 110f
        canvas.drawRoundRect(30f, yPos, 190f, yPos + 60f, 8f, 8f, paintCardBg)
        canvas.drawText("RECEITA TOTAL", 40f, yPos + 22f, paintTextRegular)
        canvas.drawText(currencyFormat.format(totalIncome), 40f, yPos + 45f, paintGreen)

        canvas.drawRoundRect(205f, yPos, 365f, yPos + 60f, 8f, 8f, paintCardBg)
        canvas.drawText("DESPESAS TOTAIS", 215f, yPos + 22f, paintTextRegular)
        canvas.drawText(currencyFormat.format(totalExpense), 215f, yPos + 45f, paintRed)

        canvas.drawRoundRect(380f, yPos, 565f, yPos + 60f, 8f, 8f, paintCardBg)
        canvas.drawText("SALDO LÍQUIDO", 390f, yPos + 22f, paintTextRegular)
        val balancePaint = if (netBalance >= 0) paintGreen else paintRed
        canvas.drawText(currencyFormat.format(netBalance), 390f, yPos + 45f, balancePaint)

        // Resumo por Categoria
        yPos += 85f
        canvas.drawText("1. RESUMO DE ORÇAMENTO POR CATEGORIA", 30f, yPos, paintTitleSection)
        yPos += 15f

        // Category Table Header
        canvas.drawRect(30f, yPos, 565f, yPos + 20f, paintCardBg)
        canvas.drawText("Categoria", 40f, yPos + 14f, paintTextBold)
        canvas.drawText("Orçamento", 200f, yPos + 14f, paintTextBold)
        canvas.drawText("Gasto Real", 320f, yPos + 14f, paintTextBold)
        canvas.drawText("Status", 450f, yPos + 14f, paintTextBold)

        yPos += 20f

        val categorySpentMap = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val allCategories = (budgets.map { it.categoryName } + categorySpentMap.keys).distinct()

        allCategories.take(7).forEach { catName ->
            val limit = budgets.find { it.categoryName == catName }?.budgetLimit ?: 0.0
            val spent = categorySpentMap[catName] ?: 0.0
            val status = if (limit > 0 && spent > limit) "Acima do limite" else "Dentro da meta"
            val statusPaint = if (limit > 0 && spent > limit) paintRed else paintGreen

            yPos += 18f
            canvas.drawText(catName, 40f, yPos, paintTextRegular)
            canvas.drawText(if (limit > 0) currencyFormat.format(limit) else "-", 200f, yPos, paintTextRegular)
            canvas.drawText(currencyFormat.format(spent), 320f, yPos, paintTextRegular)
            canvas.drawText(status, 450f, yPos, statusPaint)
            canvas.drawLine(30f, yPos + 5f, 565f, yPos + 5f, paintLine)
        }

        // Detailed Transactions
        yPos += 35f
        canvas.drawText("2. DETALHAMENTO DE TRANSAÇÕES DO MÊS", 30f, yPos, paintTitleSection)
        yPos += 15f

        // Table Header
        canvas.drawRect(30f, yPos, 565f, yPos + 20f, paintCardBg)
        canvas.drawText("Data", 35f, yPos + 14f, paintTextBold)
        canvas.drawText("Descrição", 100f, yPos + 14f, paintTextBold)
        canvas.drawText("Banco/Origem", 250f, yPos + 14f, paintTextBold)
        canvas.drawText("Categoria", 370f, yPos + 14f, paintTextBold)
        canvas.drawText("Valor (R$)", 480f, yPos + 14f, paintTextBold)

        yPos += 20f

        val displayTransactions = transactions.take(18)
        displayTransactions.forEach { tx ->
            if (yPos > 790) return@forEach // Prevent canvas overflow

            yPos += 18f
            canvas.drawText(dateFormat.format(Date(tx.dateMillis)), 35f, yPos, paintTextRegular)
            canvas.drawText(tx.title.take(22), 100f, yPos, paintTextRegular)
            canvas.drawText((tx.bankName ?: "Manual").take(16), 250f, yPos, paintTextRegular)
            canvas.drawText(tx.category.take(16), 370f, yPos, paintTextRegular)

            val valStr = (if (tx.type == TransactionType.INCOME) "+ " else "- ") + currencyFormat.format(tx.amount)
            val valPaint = if (tx.type == TransactionType.INCOME) paintGreen else paintRed
            canvas.drawText(valStr, 480f, yPos, valPaint)
            canvas.drawLine(30f, yPos + 5f, 565f, yPos + 5f, paintLine)
        }

        // Footer Security Stamp
        canvas.drawRect(0f, 810f, 595f, 842f, paintCardBg)
        canvas.drawText(
            "🔒 Documento gerado localmente pelo Controle Financeiro. Dados bancários 100% protegidos no dispositivo.",
            30f, 828f, paintTextRegular
        )

        pdfDocument.finishPage(page)

        // Save PDF File
        val pdfDir = File(context.cacheDir, "reports")
        if (!pdfDir.exists()) pdfDir.mkdirs()

        val pdfFile = File(pdfDir, "Relatorio_Financeiro_$monthYear.pdf")

        return try {
            val fileOutputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()
            fileOutputStream.close()
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
