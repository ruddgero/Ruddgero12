package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun EditBudgetDialog(
    categoryName: String,
    currentLimit: Double,
    onDismiss: () -> Unit,
    onConfirm: (limit: Double) -> Unit
) {
    var limitText by remember { mutableStateOf(if (currentLimit > 0) currentLimit.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Limite para $categoryName",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Defina o orçamento limite máximo de gastos mensais para a categoria $categoryName:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Orçamento Limite (R$)") },
                    placeholder = { Text("Ex: 1500.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newLimit = limitText.replace(",", ".").toDoubleOrNull() ?: 0.0
                    onConfirm(newLimit)
                }
            ) {
                Text("Salvar Limite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
