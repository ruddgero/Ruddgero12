package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.model.NotificationLogEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BankNotificationService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: FinanceRepository

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getDatabase(applicationContext)
        repository = FinanceRepository(
            transactionDao = db.transactionDao(),
            categoryBudgetDao = db.categoryBudgetDao(),
            recurringExpenseDao = db.recurringExpenseDao(),
            notificationLogDao = db.notificationLogDao()
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val packageName = sbn.packageName ?: return
        val extras = sbn.notification?.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        serviceScope.launch {
            val extracted = NotificationParser.parseNotification(packageName, title, text)

            if (extracted != null) {
                // Previne inserção duplicada de notificações repetidas ou atualizadas pelo banco
                val isDuplicated = repository.isRecentDuplicate(
                    title = extracted.title,
                    amount = extracted.amount,
                    windowMillis = 60_000
                )

                if (isDuplicated) {
                    Log.d("BankNotificationService", "Notificação duplicada ignorada: ${extracted.title} - R$ ${extracted.amount}")
                    return@launch
                }

                val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

                val newTransaction = TransactionEntity(
                    title = extracted.title,
                    amount = extracted.amount,
                    dateMillis = System.currentTimeMillis(),
                    monthYear = currentMonth,
                    type = extracted.type,
                    category = extracted.category,
                    bankName = extracted.bankName,
                    sourceNotification = extracted.rawText,
                    isAutoCaptured = true
                )

                val txId = repository.insertTransaction(newTransaction)

                val log = NotificationLogEntity(
                    packageName = packageName,
                    bankName = extracted.bankName,
                    rawContent = extracted.rawText,
                    timestampMillis = System.currentTimeMillis(),
                    extractedAmount = extracted.amount,
                    extractedTitle = extracted.title,
                    isProcessed = true,
                    transactionId = txId
                )
                repository.insertNotificationLog(log)

                Log.d("BankNotificationService", "Capturada transação: ${extracted.title} - R$ ${extracted.amount}")
            }
        }
    }
}
