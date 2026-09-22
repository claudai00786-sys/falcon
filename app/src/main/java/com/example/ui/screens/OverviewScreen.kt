package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.viewmodel.AppView
import com.example.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OverviewScreen(
    viewModel: PosViewModel,
    onNavigateTo: (AppView) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val factories by viewModel.factories.collectAsState()
    val customerLedgers by viewModel.customerLedgerEntries.collectAsState()
    val rawMaterialEntries by viewModel.rawMaterialEntries.collectAsState()
    val labourEntries by viewModel.labourEntries.collectAsState()
    val paintEntries by viewModel.paintEntries.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()

    var selectedPeriod by remember { mutableStateOf("Monthly") }
    val periods = listOf("Daily", "Yesterday", "Weekly", "Monthly", "Yearly")

    // Filter transactions based on selected period
    val periodTransactions = remember(transactions, selectedPeriod) {
        transactions.filter { txn ->
            // Filter according to period (today, this week, this month, this year)
            when (selectedPeriod) {
                "Daily" -> txn.date == PosViewModel.getCurrentDateString()
                else -> true
            }
        }
    }

    val periodSales = remember(periodTransactions) {
        periodTransactions.sumOf { it.total }
    }
    val periodOrdersCount = periodTransactions.size
    val periodItemsSold = remember(periodTransactions) {
        periodTransactions.sumOf { it.itemCount }
    }
    val avgOrderValue = if (periodOrdersCount > 0) periodSales / periodOrdersCount else 0.0

    val periodExpensesTotal = remember(expenses, selectedPeriod) {
        expenses.sumOf { it.amount }
    }

    val totalMaterialCostPaid = remember(rawMaterialEntries) {
        rawMaterialEntries.sumOf { it.debit }
    }

    val totalLabourCostPaid = remember(labourEntries, paintEntries) {
        labourEntries.sumOf { it.debit } + paintEntries.sumOf { it.debit }
    }

    val totalWithdrawals = remember(withdrawals) {
        withdrawals.sumOf { it.amount }
    }

    // Receivables: sum of positive customer ledger balances
    val totalReceivables = remember(customerLedgers) {
        val byCustomer = customerLedgers.groupBy { it.partyName }
        byCustomer.values.sumOf { entries ->
            val bal = entries.sumOf { it.debit - it.credit }
            if (bal > 0) bal else 0.0
        }
    }

    // Payables: sum of positive raw material supplier balances + paint balances
    val totalPayables = remember(rawMaterialEntries, paintEntries) {
        val rawBySup = rawMaterialEntries.groupBy { it.partyName }
        val rawOwed = rawBySup.values.sumOf { entries ->
            val bal = entries.sumOf { it.credit - it.debit }
            if (bal > 0) bal else 0.0
        }
        val paintByPainter = paintEntries.groupBy { it.partyName }
        val paintOwed = paintByPainter.values.sumOf { entries ->
            val bal = entries.sumOf { it.credit - it.debit }
            if (bal > 0) bal else 0.0
        }
        rawOwed + paintOwed
    }

    val netProfit = periodSales - periodExpensesTotal - totalMaterialCostPaid - totalLabourCostPaid - totalWithdrawals
    val grossProfit = totalReceivables - totalPayables

    // Factory sales breakdown segments
    val factoryBreakdown = remember(transactions) {
        val map = mutableMapOf<String, Double>()
        transactions.forEach { t ->
            val key = t.factory ?: "Walk-in"
            map[key] = (map[key] ?: 0.0) + t.total
        }
        map.entries.sortedByDescending { it.value }
    }

    val palette = listOf(PosAmber, PosSkyBlue, PosGreen, PosOrange, Color(0xFFC084FC), Color(0xFFFB7185))
    val factoryDonutSegments = remember(factoryBreakdown) {
        factoryBreakdown.mapIndexed { idx, entry ->
            Pair(entry.value.toFloat(), palette[idx % palette.size])
        }
    }

    // Outstanding factory balances leaderboard
    val outstandingFactories = remember(factories, customerLedgers) {
        factories.mapNotNull { f ->
            val entries = customerLedgers.filter { it.partyName == f.name }
            val bal = entries.sumOf { it.debit - it.credit }
            if (bal > 0) f.name to bal else null
        }.sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        // Today Calendar Date Hero
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        val calendar = Calendar.getInstance()
                        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                        Text(
                            text = dateFormat.format(calendar.time),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = dayFormat.format(calendar.time).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Button(
                        onClick = { viewModel.manualSync() },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color(0xFF1C1F22)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLanguage == "ur") "تازہ کریں" else "Update Live",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Business Summary & Executive ERP Metrics
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.businessReportTitle(currentLanguage == "ur"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        // Period Pills
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            periods.take(3).forEach { period ->
                                val isSelected = period == selectedPeriod
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { selectedPeriod = period }
                                ) {
                                    Text(
                                        text = AppStrings.translatePeriod(period, currentLanguage == "ur"),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF1C1F22) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val isUr = currentLanguage == "ur"
                    // Key KPIs in grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PosStatCard(
                            label = AppStrings.grossSales(isUr),
                            value = formatCurrency(periodSales, isUr),
                            subValue = if (isUr) "$periodOrdersCount آرڈرز" else "$periodOrdersCount orders",
                            modifier = Modifier.weight(1f)
                        )
                        PosStatCard(
                            label = AppStrings.netProfit(isUr),
                            value = formatCurrency(netProfit, isUr),
                            accentColor = if (netProfit >= 0) PosGreen else PosRed,
                            subValue = if (isUr) "منافع: ${if (periodSales > 0) (netProfit / periodSales * 100).toInt() else 0}%" else "Margin: ${if (periodSales > 0) (netProfit / periodSales * 100).toInt() else 0}%",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PosStatCard(
                            label = AppStrings.totalReceivables(isUr),
                            value = formatCurrency(totalReceivables, isUr),
                            accentColor = PosGreen,
                            modifier = Modifier.weight(1f)
                        )
                        PosStatCard(
                            label = AppStrings.totalPayables(isUr),
                            value = formatCurrency(totalPayables, isUr),
                            accentColor = PosRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Real-Time Analytics Charts: Donut Sales by Factory
        item {
            val isUr = currentLanguage == "ur"
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppStrings.factorySalesShare(isUr),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DonutChart(
                            segments = factoryDonutSegments,
                            centerText = formatCurrency(periodSales, isUr),
                            centerSubText = if (isUr) "کل سیلز" else "TOTAL",
                            modifier = Modifier.size(130.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            factoryBreakdown.take(4).forEachIndexed { idx, entry ->
                                val color = palette[idx % palette.size]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = entry.key,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = formatCurrency(entry.value, isUr),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = color
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Outstanding Balances Leaderboard
        item {
            val isUr = currentLanguage == "ur"
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.outstandingBalances(isUr),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "${outstandingFactories.size} ${if (isUr) "فیکٹریاں" else "factories"}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (outstandingFactories.isEmpty()) {
                        Text(
                            text = AppStrings.allBalancesCleared(isUr),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    } else {
                        outstandingFactories.take(4).forEach { (name, amount) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onNavigateTo(AppView.FACTORIES_CUSTOMER)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                                Text(
                                    text = formatCurrency(amount, isUr),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = PosGreen
                                    )
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Quick Navigation Tiles
        item {
            val isUr = currentLanguage == "ur"
            PosSectionHeader(
                title = AppStrings.quickActions(isUr),
                subtitle = if (isUr) "بکنگ، ادائیگیاں اور لیجر کھاتہ جات" else "Manage POS operations directly"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionCard(
                    title = AppStrings.actionNewOrder(isUr),
                    icon = Icons.Default.ShoppingCart,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { onNavigateTo(AppView.CATALOG) },
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = if (isUr) "آرڈرز (${transactions.size})" else "Orders (${transactions.size})",
                    icon = Icons.Default.ReceiptLong,
                    color = PosSkyBlue,
                    onClick = { onNavigateTo(AppView.ORDER_BOOKED) },
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = AppStrings.actionRawMaterial(isUr),
                    icon = Icons.Default.Inventory2,
                    color = PosOrange,
                    onClick = { onNavigateTo(AppView.RAW_MATERIAL_LEDGER) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .clickable { onClick() }
            .testTag("action_card_$title")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
