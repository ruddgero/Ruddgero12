package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.io.File
import java.text.NumberFormat
import java.util.*

object EmailReportUtil {

    fun sendReportByEmail(
        context: Context,
        recipientEmail: String,
        monthYear: String,
        displayMonthName: String,
        pdfFile: File,
        transactions: List<TransactionEntity>
    ) {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val subject = "Relatório Financeiro Mensal - $displayMonthName ($monthYear)"
        val bodyText = """
            Olá!
            
            Segue em anexo o seu Relatório Financeiro referente a $displayMonthName.
            
            📊 RESUMO DO MÊS:
            • Receita Total: ${currencyFormat.format(totalIncome)}
            • Despesas Totais: ${currencyFormat.format(totalExpense)}
            • Saldo do Período: ${currencyFormat.format(netBalance)}
            • Total de Transações: ${transactions.size}
            
            🔒 Segurança: Este relatório foi gerado automaticamente pelo aplicativo Controle Financeiro de forma 100% local e segura no seu dispositivo.
            
            Atenciosamente,
            Equipe Controle Financeiro
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            if (recipientEmail.isNotBlank()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            }
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, bodyText)
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Enviar Relatório de Orçamento por E-mail")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
