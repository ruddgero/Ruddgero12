package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import com.example.service.NotificationParser
import com.example.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val securityPrefs = SecurityPreferences(application)
    private val repository: FinanceRepository = run {
        val db = AppDatabase.getDatabase(application)
        FinanceRepository(
            transactionDao = db.transactionDao(),
            categoryBudgetDao = db.categoryBudgetDao(),
            recurringExpenseDao = db.recurringExpenseDao(),
            notificationLogDao = db.notificationLogDao()
        )
    }

    private val _selectedMonthYear = MutableStateFlow(
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    )
    val selectedMonthYear: StateFlow<String> = _selectedMonthYear.asStateFlow()

    private val _isPinUnlocked = MutableStateFlow(!securityPrefs.isPinEnabled)
    val isPinUnlocked: StateFlow<Boolean> = _isPinUnlocked.asStateFlow()

    private val _userEmail = MutableStateFlow(securityPrefs.userEmail)
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactionsForMonth: StateFlow<List<TransactionEntity>> = _selectedMonthYear
        .flatMapLatest { repository.getTransactionsForMonth(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryBudgets: StateFlow<List<CategoryBudgetEntity>> = _selectedMonthYear
        .flatMapLatest { repository.getBudgetsForMonth(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringExpenses: StateFlow<List<RecurringExpenseEntity>> = repository.getAllRecurringExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationLogs: StateFlow<List<NotificationLogEntity>> = repository.getRecentNotificationLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryTotals: StateFlow<Map<String, Double>> = _selectedMonthYear
        .flatMapLatest { repository.getCategoryTotalsForMonth(it) }
        .map { list -> list.associate { it.category to it.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalIncome: StateFlow<Double> = transactionsForMonth
        .map { list -> list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = transactionsForMonth
        .map { list -> list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val sixMonthProjections: StateFlow<List<MonthProjection>> = combine(
        totalIncome,
        recurringExpenses,
        transactionsForMonth
    ) { income, recurring, txs ->
        ProjectionEngine.calculate6MonthProjection(income, recurring, txs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectMonth(monthYear: String) {
        _selectedMonthYear.value = monthYear
    }

    fun setNextMonth() {
        changeMonth(1)
    }

    fun setPreviousMonth() {
        changeMonth(-1)
    }

    private fun changeMonth(delta: Int) {
        val format = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        try {
            val date = format.parse(_selectedMonthYear.value) ?: Date()
            val cal = Calendar.getInstance().apply { time = date }
            cal.add(Calendar.MONTH, delta)
            _selectedMonthYear.value = format.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                title = title,
                amount = amount,
                monthYear = _selectedMonthYear.value,
                type = type,
                category = category,
                notes = notes,
                dateMillis = System.currentTimeMillis()
            )
            repository.insertTransaction(tx)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun saveCategoryBudget(category: String, limit: Double) {
        viewModelScope.launch {
            val budget = CategoryBudgetEntity(
                monthYear = _selectedMonthYear.value,
                categoryName = category,
                budgetLimit = limit
            )
            repository.saveCategoryBudget(budget)
        }
    }

    fun addRecurringExpense(title: String, amount: Double, category: String, dueDay: Int) {
        viewModelScope.launch {
            val recurring = RecurringExpenseEntity(
                title = title,
                estimatedAmount = amount,
                category = category,
                dueDayOfMonth = dueDay,
                isActive = true
            )
            repository.insertRecurringExpense(recurring)
        }
    }

    fun deleteRecurringExpense(expense: RecurringExpenseEntity) {
        viewModelScope.launch {
            repository.deleteRecurringExpense(expense)
        }
    }

    fun simulateBankNotification(bankName: String, title: String, text: String) {
        viewModelScope.launch {
            val extracted = NotificationParser.parseNotification("com.nu.production", title, text)
            if (extracted != null) {
                val tx = TransactionEntity(
                    title = extracted.title,
                    amount = extracted.amount,
                    monthYear = _selectedMonthYear.value,
                    type = extracted.type,
                    category = extracted.category,
                    bankName = bankName,
                    sourceNotification = text,
                    isAutoCaptured = true
                )
                val txId = repository.insertTransaction(tx)
                val log = NotificationLogEntity(
                    packageName = "com.simulated.$bankName",
                    bankName = bankName,
                    rawContent = text,
                    extractedAmount = extracted.amount,
                    extractedTitle = extracted.title,
                    transactionId = txId
                )
                repository.insertNotificationLog(log)
            }
        }
    }

    fun updateUserEmail(email: String) {
        securityPrefs.userEmail = email
        _userEmail.value = email
    }

    fun togglePinProtection(enabled: Boolean, pin: String) {
        securityPrefs.isPinEnabled = enabled
        if (enabled && pin.isNotBlank()) {
            securityPrefs.pinCode = pin
            _isPinUnlocked.value = true
        } else if (!enabled) {
            _isPinUnlocked.value = true
        }
    }

    fun unlockPin(pinInput: String): Boolean {
        val success = securityPrefs.verifyPin(pinInput)
        if (success) {
            _isPinUnlocked.value = true
        }
        return success
    }

    fun lockApp() {
        if (securityPrefs.isPinEnabled) {
            _isPinUnlocked.value = false
        }
    }

    fun generatePdfReport(context: Context, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            val monthYear = _selectedMonthYear.value
            val displayMonth = getDisplayMonthName(monthYear)
            val txs = repository.getTransactionsForMonthSync(monthYear)
            val budgets = categoryBudgets.value

            val pdf = PdfReportGenerator.generateMonthlyPdfReport(
                context = context,
                monthYear = monthYear,
                displayMonthName = displayMonth,
                transactions = txs,
                budgets = budgets
            )
            onComplete(pdf)
        }
    }

    fun sendEmailReport(context: Context) {
        viewModelScope.launch {
            val monthYear = _selectedMonthYear.value
            val displayMonth = getDisplayMonthName(monthYear)
            val txs = repository.getTransactionsForMonthSync(monthYear)
            val budgets = categoryBudgets.value

            val pdf = PdfReportGenerator.generateMonthlyPdfReport(
                context = context,
                monthYear = monthYear,
                displayMonthName = displayMonth,
                transactions = txs,
                budgets = budgets
            )

            if (pdf != null) {
                EmailReportUtil.sendReportByEmail(
                    context = context,
                    recipientEmail = securityPrefs.userEmail,
                    monthYear = monthYear,
                    displayMonthName = displayMonth,
                    pdfFile = pdf,
                    transactions = txs
                )
            }
        }
    }

    fun getDisplayMonthName(monthYearStr: String = _selectedMonthYear.value): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val date = format.parse(monthYearStr) ?: Date()
            val outFormat = SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR"))
            outFormat.format(date).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            monthYearStr
        }
    }
}
