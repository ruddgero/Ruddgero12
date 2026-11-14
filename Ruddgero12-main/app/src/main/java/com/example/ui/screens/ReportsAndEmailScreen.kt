package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.ui.components.MonthSelectorBar
import com.example.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun ReportsAndEmailScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedMonthYear by viewModel.selectedMonthYear.collectAsState()
    val displayMonthName = viewModel.getDisplayMonthName(selectedMonthYear)

    val userEmail by viewModel.userEmail.collectAsState()
    var emailInput by remember { mutableStateOf(userEmail) }
    var isAutoEmailEnabled by remember { mutableStateOf(viewModel.securityPrefs.isAutoEmailEnabled) }

    val transactions by viewModel.transactionsForMonth.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val netBalance = totalIncome - totalExpense
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Month Selector Bar
        item {
            MonthSelectorBar(
                displayMonthName = displayMonthName,
                onPreviousMonth = { viewModel.setPreviousMonth() },
                onNextMonth = { viewModel.setNextMonth() }
            )
        }

        // Title Header
        item {
            Text(
                text = "Relatórios em PDF & Envio por E-mail",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gere relatórios completos de orçamento em formato PDF e envie automaticamente no encerramento de cada mês.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // PDF Preview Mock Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFD32F2F).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Relatório_$selectedMonthYear.pdf",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "$displayMonthName • ${transactions.size} transações",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "PDF A4 Native",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    HorizontalDivider()

                    // PDF Content Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Receitas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = currencyFormat.format(totalIncome), style = MaterialTheme.typography.titleSmall, color = Color(0xFF2E7D32))
                        }
                        Column {
                            Text(text = "Despesas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = currencyFormat.format(totalExpense), style = MaterialTheme.typography.titleSmall, color = Color(0xFFC62828))
                        }
                        Column {
                            Text(text = "Saldo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = currencyFormat.format(netBalance), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Action Buttons for PDF
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.generatePdfReport(context) { pdfFile ->
                            if (pdfFile != null) {
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        pdfFile
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Abrir Relatório PDF"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "PDF gerado com sucesso em: ${pdfFile.name}", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "Erro ao gerar PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gerar & Abrir PDF")
                }

                Button(
                    onClick = {
                        viewModel.sendEmailReport(context)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enviar por E-mail")
                }
            }
        }

        // Email Configuration Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Configuração do Envio Automático por E-mail",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            viewModel.updateUserEmail(it)
                        },
                        label = { Text("E-mail do Destinatário") },
                        placeholder = { Text("seu-email@exemplo.com") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Envio Automático ao Final do Mês",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Prepara o e-mail automaticamente no encerramento do período mensal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isAutoEmailEnabled,
                            onCheckedChange = {
                                isAutoEmailEnabled = it
                                viewModel.securityPrefs.isAutoEmailEnabled = it
                            }
                        )
                    }
                }
            }
        }
    }
}
