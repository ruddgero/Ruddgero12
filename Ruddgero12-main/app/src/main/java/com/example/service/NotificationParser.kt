package com.example.service

import com.example.data.model.TransactionType
import java.text.NumberFormat
import java.util.*
import java.util.regex.Pattern

data class ExtractedTransaction(
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val bankName: String,
    val rawText: String
)

object NotificationParser {

    private val BANK_PACKAGES = mapOf(
        "com.nu.production" to "Nubank",
        "com.itau" to "Itaú",
        "com.itau.personnalite" to "Itaú Personnalité",
        "br.com.bradesco" to "Bradesco",
        "br.com.bradesco.next" to "Next",
        "com.santander.app" to "Santander",
        "com.santander.app.way" to "Santander Way",
        "br.com.intermedium" to "Banco Inter",
        "com.c6bank.app" to "C6 Bank",
        "br.gov.caixa.tem" to "Caixa Tem",
        "com.picpay" to "PicPay",
        "com.btg.pactual" to "BTG Pactual",
        "com.mercadopago.wallet" to "Mercado Pago",
        "br.com.uol.ps.myaccount" to "PagBank",
        "br.com.neon" to "Neon",
        "com.nomadfinancial.nomad" to "Nomad"
    )

    fun getBankNameFromPackageOrText(packageName: String, text: String): String {
        BANK_PACKAGES[packageName]?.let { return it }

        val textLower = text.lowercase(Locale.getDefault())
        return when {
            textLower.contains("nubank") -> "Nubank"
            textLower.contains("itaú") || textLower.contains("itau") -> "Itaú"
            textLower.contains("bradesco") -> "Bradesco"
            textLower.contains("santander") -> "Santander"
            textLower.contains("inter") -> "Banco Inter"
            textLower.contains("c6") -> "C6 Bank"
            textLower.contains("caixa") -> "Caixa"
            textLower.contains("picpay") -> "PicPay"
            textLower.contains("btg") -> "BTG Pactual"
            textLower.contains("mercado pago") -> "Mercado Pago"
            textLower.contains("pagbank") || textLower.contains("pagseguro") -> "PagBank"
            textLower.contains("neon") -> "Neon"
            textLower.contains("nomad") -> "Nomad"
            else -> "Notificação Bancária"
        }
    }

    fun parseNotification(packageName: String, title: String, text: String): ExtractedTransaction? {
        val fullText = "$title $text"
        val fullTextLower = fullText.lowercase(Locale.getDefault())

        // Check if it's a financial transaction notification
        val isFinancial = fullTextLower.contains("r$") ||
                fullTextLower.contains("compra") ||
                fullTextLower.contains("pix") ||
                fullTextLower.contains("transferência") ||
                fullTextLower.contains("pagamento") ||
                fullTextLower.contains("recebido") ||
                fullTextLower.contains("estorno") ||
                fullTextLower.contains("reembolso") ||
                fullTextLower.contains("cashback")

        if (!isFinancial) return null

        val bankName = getBankNameFromPackageOrText(packageName, fullText)
        val amount = extractAmount(fullText) ?: return null

        // Determine type (Income vs Expense)
        val type = if (fullTextLower.contains("recebido") ||
            fullTextLower.contains("crédito de") ||
            fullTextLower.contains("depósito") ||
            fullTextLower.contains("você recebeu") ||
            fullTextLower.contains("estorno") ||
            fullTextLower.contains("reembolso") ||
            fullTextLower.contains("cashback")
        ) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }

        var extractedTitle = extractTitle(title, text, bankName)
        
        // Detect installments (ex: "em 3x", "parcelado em 5x")
        val installmentRegex = Pattern.compile("""(?:em|parcelado em)\s+(\d{1,2})x""", Pattern.CASE_INSENSITIVE)
        val installmentMatcher = installmentRegex.matcher(fullText)
        if (installmentMatcher.find()) {
            val numParcels = installmentMatcher.group(1)
            extractedTitle = "$extractedTitle (${numParcels}x)"
        }

        val category = if (fullTextLower.contains("cashback") || fullTextLower.contains("estorno")) {
            "Outros"
        } else {
            autoCategorize(extractedTitle, fullTextLower)
        }

        return ExtractedTransaction(
            title = extractedTitle,
            amount = amount,
            type = type,
            category = category,
            bankName = bankName,
            rawText = fullText
        )
    }

    private fun extractAmount(text: String): Double? {
        // RegEx for Brazilian currency R$ 1.250,50 or 1250,50
        val regex = Pattern.compile("""(?:R\$\s*|BRL\s*)([\d\.\,]+)""", Pattern.CASE_INSENSITIVE)
        val matcher = regex.matcher(text)

        if (matcher.find()) {
            val amountStr = matcher.group(1) ?: return null
            return try {
                // Remove thousand separators (.) and replace decimal separator (,) with (.)
                val cleanStr = if (amountStr.contains(",") && amountStr.contains(".")) {
                    amountStr.replace(".", "").replace(",", ".")
                } else if (amountStr.contains(",")) {
                    amountStr.replace(",", ".")
                } else {
                    amountStr
                }
                cleanStr.toDoubleOrNull()
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    private fun extractTitle(title: String, text: String, bankName: String): String {
        // Look for merchant names after "em", "no", "para"
        val full = "$title $text"
        val regex = Pattern.compile("""(?:em|no|na|para|estabelecimento)\s+([A-Z0-9\sáàâãéèêíóòôõúçÁÀÂÃÉÈÍÓÒÔÕÚÇ]{3,25})""", Pattern.CASE_INSENSITIVE)
        val matcher = regex.matcher(full)

        if (matcher.find()) {
            val merchant = matcher.group(1)?.trim()
            if (!merchant.isNullOrEmpty() && !merchant.lowercase(Locale.getDefault()).contains("cartão")) {
                return merchant.split(" ").take(3).joinToString(" ").replaceFirstChar { it.uppercase() }
            }
        }

        if (title.isNotBlank() && title != bankName) {
            return title.take(30)
        }

        return "Transação $bankName"
    }

    private fun autoCategorize(merchantTitle: String, textLower: String): String {
        val combined = "$merchantTitle $textLower"

        return when {
            combined.contains("supermercado") || combined.contains("carrefour") || combined.contains("pão de açúcar") ||
                    combined.contains("assai") || combined.contains("atacadão") || combined.contains("extra") ||
                    combined.contains("hortifruti") || combined.contains("ifood") || combined.contains("rappi") ||
                    combined.contains("mcdonald") || combined.contains("restaurante") || combined.contains("padaria") ||
                    combined.contains("feira") -> "Alimentação"

            combined.contains("posto") || combined.contains("shell") || combined.contains("ipiranga") ||
                    combined.contains("uber") || combined.contains("99") || combined.contains("estacionamento") ||
                    combined.contains("pedágio") || combined.contains("gasolina") || combined.contains("combustivel") -> "Transporte"

            combined.contains("drogaria") || combined.contains("farmácia") || combined.contains("drogasil") ||
                    combined.contains("raia") || combined.contains("hospital") || combined.contains("médico") ||
                    combined.contains("consultório") || combined.contains("dentista") -> "Saúde"

            combined.contains("netflix") || combined.contains("spotify") || combined.contains("steam") ||
                    combined.contains("playstation") || combined.contains("cinema") || combined.contains("sympla") ||
                    combined.contains("bar") || combined.contains("show") -> "Lazer"

            combined.contains("aluguel") || combined.contains("condomínio") || combined.contains("enel") ||
                    combined.contains("sabesp") || combined.contains("claro") || combined.contains("vivo") ||
                    combined.contains("tim") || combined.contains("luz") || combined.contains("água") -> "Moradia"

            combined.contains("cartão") || combined.contains("fatura") || combined.contains("crédito") -> "Cartão de Crédito"

            else -> "Outros"
        }
    }
}
