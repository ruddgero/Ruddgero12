package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.RecurringExpenseEntity
import com.example.ui.components.AddRecurringExpenseDialog
import com.example.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp

@Composable
fun SixMonthProjectionScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val projections by viewModel.sixMonthProjections.collectAsState()
    val recurringExpenses by viewModel.recurringExpenses.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    var showAddRecurringDialog by remember { mutableStateOf(false) }

    val totalCumulativeSavings = projections.lastOrNull()?.cumulativeSavings ?: 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Title Header
        item {
            Text(
                text = "Visão Geral - Próximos 6 Meses",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Projeção inteligente baseada nos seus custos fixos recorrentes e média de despesas variáveis.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 6-Month Projection Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Economia Acumulada Projetada (6 Meses)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currencyFormat.format(totalCumulativeSavings),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Interactive 6-Month Projection Line Chart (Exclusively Monthly Expenses)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Projeção de Despesas Mensais",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Tendência de custos totais (Fixos + Variáveis)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Gráfico de Linhas",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (projections.isNotEmpty()) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val strokeColor = MaterialTheme.colorScheme.primary
                        val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        val textColor = MaterialTheme.colorScheme.onSurface
                        val textColorArgb = textColor.toArgb()
                        val density = LocalDensity.current

                        val valueTextPaint = remember(textColorArgb) {
                            android.graphics.Paint().apply {
                                color = textColorArgb
                                textSize = density.run { 10.sp.toPx() }
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                            }
                        }

                        val maxExpense = (projections.maxOfOrNull { it.totalProjectedExpense } ?: 5000.0).coerceAtLeast(100.0) * 1.2
                        val minExpense = ((projections.minOfOrNull { it.totalProjectedExpense } ?: 0.0) * 0.8).coerceAtLeast(0.0)

                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Canvas Area for Line Chart
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val width = size.width
                                    val height = size.height
                                    val paddingHorizontal = 28f
                                    val paddingTop = 50f
                                    val paddingBottom = 20f
                                    val chartHeight = height - paddingTop - paddingBottom
                                    val stepX = if (projections.size > 1) (width - 2 * paddingHorizontal) / (projections.size - 1) else 0f

                                    // Draw background dotted horizontal grid lines
                                    val gridLinesCount = 3
                                    for (i in 0..gridLinesCount) {
                                        val y = paddingTop + chartHeight * (i.toFloat() / gridLinesCount)
                                        drawLine(
                                            color = gridColor,
                                            start = Offset(paddingHorizontal, y),
                                            end = Offset(width - paddingHorizontal, y),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                        )
                                    }

                                    // Calculate data points (x, y)
                                    val points = projections.mapIndexed { index, proj ->
                                        val x = paddingHorizontal + index * stepX
                                        val ratio = ((proj.totalProjectedExpense - minExpense) / (maxExpense - minExpense)).toFloat().coerceIn(0f, 1f)
                                        val y = height - paddingBottom - ratio * chartHeight
                                        Offset(x, y)
                                    }

                                    // Build Smooth Curve Path
                                    val linePath = Path()
                                    val fillPath = Path()

                                    if (points.isNotEmpty()) {
                                        linePath.moveTo(points[0].x, points[0].y)
                                        fillPath.moveTo(points[0].x, height - paddingBottom)
                                        fillPath.lineTo(points[0].x, points[0].y)

                                        for (i in 0 until points.size - 1) {
                                            val p1 = points[i]
                                            val p2 = points[i + 1]
                                            val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                                            val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)

                                            linePath.cubicTo(
                                                controlPoint1.x, controlPoint1.y,
                                                controlPoint2.x, controlPoint2.y,
                                                p2.x, p2.y
                                            )

                                            fillPath.cubicTo(
                                                controlPoint1.x, controlPoint1.y,
                                                controlPoint2.x, controlPoint2.y,
                                                p2.x, p2.y
                                            )
                                        }

                                        fillPath.lineTo(points.last().x, height - paddingBottom)
                                        fillPath.close()

                                        // Draw Gradient Fill under line
                                        drawPath(
                                            path = fillPath,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    primaryColor.copy(alpha = 0.35f),
                                                    primaryColor.copy(alpha = 0.02f)
                                                )
                                            )
                                        )

                                        // Draw Smooth Main Line
                                        drawPath(
                                            path = linePath,
                                            color = strokeColor,
                                            style = Stroke(width = 3.5.dp.toPx())
                                        )

                                        // Draw Points & Values above line
                                        points.forEachIndexed { index, pt ->
                                            drawCircle(
                                                color = strokeColor.copy(alpha = 0.2f),
                                                radius = 9.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = strokeColor,
                                                radius = 5.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = Color.White,
                                                radius = 2.5.dp.toPx(),
                                                center = pt
                                            )

                                            // Draw value text above the point
                                            val valText = currencyFormat.format(projections[index].totalProjectedExpense)
                                            drawContext.canvas.nativeCanvas.drawText(
                                                valText,
                                                pt.x,
                                                pt.y - 12.dp.toPx(),
                                                valueTextPaint
                                            )
                                        }
                                    }
                                }
                            }

                            // Month X-Axis Labels below the line chart
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                projections.forEach { proj ->
                                    val abbrevLabel = try {
                                        val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(proj.monthYear)
                                        if (date != null) {
                                            val m = SimpleDateFormat("MMM", Locale("pt", "BR")).format(date).replace(".", "").lowercase()
                                            val y = SimpleDateFormat("yy", Locale.getDefault()).format(date)
                                            "$m$y"
                                        } else {
                                            proj.monthDisplayName
                                        }
                                    } catch (e: Exception) {
                                        proj.monthDisplayName
                                    }

                                    Text(
                                        text = abbrevLabel,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Legend Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Despesa Mensal Projetada (R$)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Detailed Breakdown of Upcoming Months
        item {
            Text(
                text = "Detalhamento Mês a Mês",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(projections, key = { it.monthYear }) { proj ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = proj.monthDisplayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Fixos: ${currencyFormat.format(proj.fixedExpenses)} • Var: ${currencyFormat.format(proj.estimatedVariableExpenses)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Despesa: ${currencyFormat.format(proj.totalProjectedExpense)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFC62828)
                        )
                        Text(
                            text = "Saldo: ${currencyFormat.format(proj.projectedBalance)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (proj.projectedBalance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }
        }

        // Fixed Costs Manager Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custos Fixos Recorrentes",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                IconButton(onClick = { showAddRecurringDialog = true }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Adicionar Custo Fixo", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Fixed Costs List
        items(recurringExpenses, key = { it.id }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = item.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(text = "${item.category} • Vence dia ${item.dueDayOfMonth}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = currencyFormat.format(item.estimatedAmount), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { viewModel.deleteRecurringExpense(item) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddRecurringDialog) {
        AddRecurringExpenseDialog(
            onDismiss = { showAddRecurringDialog = false },
            onConfirm = { title, amount, category, day ->
                viewModel.addRecurringExpense(title, amount, category, day)
                showAddRecurringDialog = false
            }
        )
    }
}
