package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.PinLockOverlay
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel = viewModel()
) {
    val isPinUnlocked by viewModel.isPinUnlocked.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    if (!isPinUnlocked) {
        PinLockOverlay(
            onUnlock = { pinInput -> viewModel.unlockPin(pinInput) },
            onBiometricUnlock = { viewModel.unlockDirectly() }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Orçamento") },
                        label = { Text("Orçamento", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.NotificationsActive, contentDescription = "Notificações") },
                        label = { Text("Bancos", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = "Relatórios") },
                        label = { Text("Relatórios", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Projeção") },
                        label = { Text("Projeção 6M", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Security, contentDescription = "Segurança") },
                        label = { Text("Segurança", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }
        ) { innerPadding ->
            val screenModifier = Modifier.padding(innerPadding)
            when (selectedTab) {
                0 -> HomeDashboardScreen(viewModel = viewModel, modifier = screenModifier)
                1 -> NotificationReaderScreen(viewModel = viewModel, modifier = screenModifier)
                2 -> ReportsAndEmailScreen(viewModel = viewModel, modifier = screenModifier)
                3 -> SixMonthProjectionScreen(viewModel = viewModel, modifier = screenModifier)
                4 -> SecurityAndSettingsScreen(viewModel = viewModel, modifier = screenModifier)
            }
        }
    }
}
