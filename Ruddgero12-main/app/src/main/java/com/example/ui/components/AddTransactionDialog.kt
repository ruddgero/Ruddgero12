package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.TransactionType

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, type: TransactionType, category: String, notes: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf("Alimentação") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("Alimentação", "Moradia", "Transporte", "Lazer", "Saúde", "Cartão de Crédito", "Salário", "Outros")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nova Transação",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selector Type (Despesa / Receita)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = { type = TransactionType.EXPENSE },
                        label = { Text("Despesa (Saída)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = { type = TransactionType.INCOME },
                        label = { Text("Receita (Entrada)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Descrição / Nome") },
                    placeholder = { Text("Ex: Supermercado Carrefour") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Valor (R$)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selection
                Text(
                    text = "Categoria",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                var expandedCategoryDropdown by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedCategoryDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(category)
                    }

                    DropdownMenu(
                        expanded = expandedCategoryDropdown,
                        onDismissRequest = { expandedCategoryDropdown = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expandedCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0) {
                        onConfirm(title, amount, type, category, notes.ifBlank { null })
                    }
                },
                enabled = title.isNotBlank() && (amountText.replace(",", ".").toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
