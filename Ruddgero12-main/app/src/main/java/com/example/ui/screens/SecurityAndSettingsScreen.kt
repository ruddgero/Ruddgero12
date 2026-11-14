package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SecurityAndSettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPinEnabled by remember { mutableStateOf(viewModel.securityPrefs.isPinEnabled) }
    var showSetPinDialog by remember { mutableStateOf(false) }

    // Launcher para Exportar Backup JSON
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(context, uri) { success ->
                val msg = if (success) "Backup exportado com sucesso!" else "Falha ao exportar backup."
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Launcher para Importar Backup JSON
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(context, uri) { result ->
                result.onSuccess { count ->
                    Toast.makeText(context, "Backup restaurado com sucesso! $count transações importadas.", Toast.LENGTH_LONG).show()
                }.onFailure { err ->
                    Toast.makeText(context, "Erro ao restaurar backup: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Header
        item {
            Text(
                text = "Segurança & Configurações",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gerencie a proteção por senha, privacidade dos seus dados e preferências do aplicativo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Privacy Shield Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D47A1))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Selo de Proteção Local-First",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Text(
                        text = "1. Todos os dados bancários, limites e transações ficam gravados exclusivamente no banco de dados SQLite local no seu celular.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "2. Não há envio de extratos ou senhas bancárias para servidores remotos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "3. O leitor de notificações analisa mensagens apenas dentro da memória RAM do dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // PIN Protection Toggle Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bloqueio de Tela por PIN",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isPinEnabled) "PIN ativado" else "PIN desativado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showSetPinDialog = true
                                } else {
                                    isPinEnabled = false
                                    viewModel.togglePinProtection(false, "")
                                    Toast.makeText(context, "Proteção por PIN desativada", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    if (isPinEnabled) {
                        OutlinedButton(
                            onClick = { showSetPinDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Alterar PIN de Segurança")
                        }
                    }
                }
            }
        }

        // Data Management Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Gestão de Dados & Backup",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Exporte todos os seus dados locais em arquivo JSON para guardar no seu Google Drive ou restaurar em outro dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val defaultName = "backup_financeiro_${System.currentTimeMillis()}.json"
                                exportBackupLauncher.launch(defaultName)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar")
                        }

                        OutlinedButton(
                            onClick = {
                                importBackupLauncher.launch(arrayOf("application/json", "text/*"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restaurar")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Button(
                        onClick = {
                            viewModel.lockApp()
                            Toast.makeText(context, "Aplicativo bloqueado", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bloquear App Agora")
                    }
                }
            }
        }
    }

    if (showSetPinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSetPinDialog = false },
            title = { Text("Configurar PIN (4 a 6 dígitos)") },
            text = {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6) newPin = it },
                    label = { Text("Digite o PIN desejado") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length >= 4) {
                            isPinEnabled = true
                            viewModel.togglePinProtection(true, newPin)
                            showSetPinDialog = false
                            Toast.makeText(context, "PIN salvo com sucesso!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = newPin.length >= 4
                ) {
                    Text("Salvar PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetPinDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
