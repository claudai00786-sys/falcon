package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.AppBrandLogo
import com.example.data.model.CustomerPaymentEntity
import com.example.data.model.OrderTransactionEntity
import com.example.data.model.ProductEntity
import com.example.ui.components.PosStatCard
import com.example.ui.components.formatCurrency
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.viewmodel.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val payments by viewModel.customerPayments.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var paymentDialogOrder by remember { mutableStateOf<OrderTransactionEntity?>(null) }
    var editDialogOrder by remember { mutableStateOf<OrderTransactionEntity?>(null) }
    var receiptDialogOrder by remember { mutableStateOf<OrderTransactionEntity?>(null) }

    val isUr = currentLanguage == "ur"

    val filteredTransactions = remember(transactions, searchQuery) {
        if (searchQuery.isBlank()) {
            transactions
        } else {
            val q = searchQuery.trim().lowercase().replace("#", "")
            transactions.filter {
                it.id.lowercase().contains(q) || (it.factory?.lowercase()?.contains(q) == true)
            }
        }
    }

    val totalUnpaid = remember(transactions, payments) {
        transactions.sumOf { txn ->
            val paid = payments.filter { it.txnId == txn.id }.sumOf { it.amount }
            if (txn.paid) 0.0 else (txn.total - paid).coerceAtLeast(0.0)
        }
    }

    val totalItemsSold = remember(transactions) {
        transactions.sumOf { it.itemCount }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(AppStrings.searchOrders(isUr)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // KPI Stat Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PosStatCard(
                label = AppStrings.totalUnpaid(isUr),
                value = formatCurrency(totalUnpaid, isUr),
                accentColor = if (totalUnpaid > 0) PosRed else PosGreen,
                modifier = Modifier.weight(1f)
            )
            PosStatCard(
                label = AppStrings.totalItemsSold(isUr),
                value = "$totalItemsSold ${if (isUr) "عدد" else "pcs"}",
                subValue = "${transactions.size} ${if (isUr) "آرڈرز" else "orders"}",
                modifier = Modifier.weight(1f)
            )
        }

        // List of Order Booked Cards
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = AppStrings.noOrdersFound(isUr),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { txn ->
                    val txnPayments = payments.filter { it.txnId == txn.id }
                    val totalPaid = txnPayments.sumOf { it.amount }
                    val dueAmount = (txn.total - totalPaid).coerceAtLeast(0.0)

                    TransactionCard(
                        transaction = txn,
                        dueAmount = dueAmount,
                        isUrdu = isUr,
                        onConfirm = { viewModel.markOrderConfirmed(txn.id) },
                        onDeliver = { viewModel.markOrderDelivered(txn.id) },
                        onRecordPayment = { paymentDialogOrder = txn },
                        onNil = { viewModel.nilOrder(txn.id) },
                        onDelete = { viewModel.deleteOrder(txn.id) },
                        onEdit = { editDialogOrder = txn },
                        onPrintReceipt = { receiptDialogOrder = txn }
                    )
                }
            }
        }
    }

    // Record Payment Dialog
    if (paymentDialogOrder != null) {
        val txn = paymentDialogOrder!!
        val txnPayments = payments.filter { it.txnId == txn.id }
        val totalPaid = txnPayments.sumOf { it.amount }
        val dueAmount = (txn.total - totalPaid).coerceAtLeast(0.0)

        RecordPaymentDialog(
            transaction = txn,
            dueAmount = dueAmount,
            history = txnPayments,
            isUrdu = isUr,
            onDismiss = { paymentDialogOrder = null },
            onConfirmPayment = { amount, method, detail, recBy, recIn ->
                viewModel.recordOrderPayment(txn.id, amount, method, detail, recBy, recIn)
                paymentDialogOrder = null
            },
            onNilFull = {
                viewModel.nilOrder(txn.id)
                paymentDialogOrder = null
            }
        )
    }

    // Edit Transaction Dialog
    if (editDialogOrder != null) {
        val txn = editDialogOrder!!
        EditTransactionDialog(
            transaction = txn,
            isUrdu = isUr,
            onDismiss = { editDialogOrder = null },
            onSave = { updated ->
                viewModel.saveProduct(ProductEntity(name = "", cat = "", price = 0.0)) // trigger refresh
                editDialogOrder = null
            }
        )
    }

    // Official Order Receipt & Invoice Print Dialog with App Brand Logo
    if (receiptDialogOrder != null) {
        val txn = receiptDialogOrder!!
        val txnPayments = payments.filter { it.txnId == txn.id }
        OrderReceiptPrintDialog(
            transaction = txn,
            payments = txnPayments,
            isUrdu = isUr,
            onDismiss = { receiptDialogOrder = null }
        )
    }
}

@Composable
fun TransactionCard(
    transaction: OrderTransactionEntity,
    dueAmount: Double,
    isUrdu: Boolean = false,
    onConfirm: () -> Unit,
    onDeliver: () -> Unit,
    onRecordPayment: () -> Unit,
    onNil: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onPrintReceipt: () -> Unit = {}
) {
    val isPaid = transaction.paid || dueAmount <= 0.0
    val isPartial = !isPaid && dueAmount < transaction.total

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (transaction.delivered) PosRedBright.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth().testTag("transaction_row_${transaction.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Order ID, Delivered Badge, Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Boxed ID Badge as in HTML
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "#${transaction.id}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Red Delivered Mark from HTML
                    if (transaction.delivered) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PosRedBright, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (isUrdu) "ڈیلیور ہو گیا" else "DELIVERED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = PosRedBright,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${transaction.date} · ${transaction.time}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (transaction.syncStatus == "pending") PosOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (transaction.syncStatus == "pending") androidx.compose.foundation.BorderStroke(0.5.dp, PosOrange) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (transaction.syncStatus == "pending") PosOrange else PosGreen)
                            )
                            Text(
                                text = if (transaction.syncStatus == "pending") {
                                    if (isUrdu) "ترسیل کا منتظر (آف لائن)" else "Pending Upload"
                                } else {
                                    if (isUrdu) "سرور سے ہم وقت" else "Synced"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (transaction.syncStatus == "pending") PosOrange else PosGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = transaction.itemsSummary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (transaction.delivered) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        if (!transaction.factory.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = transaction.factory,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Chips: Quantity, Sizes, Colors
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isUrdu) "تعداد: ${transaction.itemCount}" else "QTY: ${transaction.itemCount}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        if (!transaction.sizes.isNullOrBlank()) {
                            Text(
                                text = if (isUrdu) "سائز: ${transaction.sizes.replace("\n", ", ")}" else "SIZE: ${transaction.sizes.replace("\n", ", ")}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total Amount & Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatCurrency(transaction.total, isUrdu),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 17.sp
                    )
                )

                // Paid / Partial / Unpaid Status Tag
                val statusColor = when {
                    isPaid -> PosGreen
                    isPartial -> PosAmber
                    else -> PosRed
                }
                val statusText = when {
                    isPaid -> AppStrings.orderPaid(isUrdu)
                    isPartial -> AppStrings.orderPartial(isUrdu, formatCurrency(dueAmount, isUrdu))
                    else -> AppStrings.orderUnpaid(isUrdu)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Row: Confirm, Record Payment, Deliver, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!transaction.confirmed) {
                    OutlinedButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PosGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(AppStrings.confirm(isUrdu), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (!isPaid) {
                    Button(
                        onClick = onRecordPayment,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(AppStrings.recordPayment(isUrdu), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = onNil, contentPadding = PaddingValues(horizontal = 6.dp)) {
                        Text(AppStrings.nilDue(isUrdu), fontSize = 11.sp, color = PosGreen)
                    }
                }

                if (!transaction.delivered) {
                    OutlinedButton(
                        onClick = onDeliver,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PosRedBright),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(AppStrings.deliver(isUrdu), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onPrintReceipt, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Print, contentDescription = "Print / Export Receipt", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PosRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun RecordPaymentDialog(
    transaction: OrderTransactionEntity,
    dueAmount: Double,
    history: List<CustomerPaymentEntity>,
    isUrdu: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double, method: String, detail: String, recBy: String, recIn: String) -> Unit,
    onNilFull: () -> Unit
) {
    var amountStr by remember { mutableStateOf(dueAmount.toInt().toString()) }
    var method by remember { mutableStateOf("Cash") }
    var detail by remember { mutableStateOf("") }
    var receivedBy by remember { mutableStateOf("Amir") }
    var receivedIn by remember { mutableStateOf("Shop Counter") }

    val payingAmount = amountStr.toDoubleOrNull() ?: 0.0
    val newRemainingDue = (dueAmount - payingAmount).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = AppStrings.recordPayment(isUrdu),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            if (isUrdu) "آرڈر #${transaction.id} · کل بل: ${formatCurrency(transaction.total, true)}"
                            else "Order #${transaction.id} · Total Bill: ${formatCurrency(transaction.total, false)}",
                            fontSize = 11.5.sp
                        )
                        Text(
                            "${AppStrings.remainingDue(isUrdu)}: ${formatCurrency(dueAmount, isUrdu)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PosRed
                        )
                    }
                }

                // Quick Calculation Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isUrdu) "حسابی کٹوتی:" else "Quick Math:", fontSize = 11.sp, color = Color.Gray)
                    FilterChip(
                        selected = payingAmount == dueAmount,
                        onClick = { amountStr = dueAmount.toInt().toString() },
                        label = { Text(if (isUrdu) "مکمل (100%)" else "Full (100%)", fontSize = 10.5.sp) }
                    )
                    FilterChip(
                        selected = payingAmount == (dueAmount / 2),
                        onClick = { amountStr = (dueAmount / 2).toInt().toString() },
                        label = { Text(if (isUrdu) "نصف (50%)" else "50%", fontSize = 10.5.sp) }
                    )
                    FilterChip(
                        selected = payingAmount == (dueAmount * 0.25),
                        onClick = { amountStr = (dueAmount * 0.25).toInt().toString() },
                        label = { Text(if (isUrdu) "چوتھائی (25%)" else "25%", fontSize = 10.5.sp) }
                    )
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(AppStrings.amountRs(isUrdu)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Live Mathematical Auto-solve Formula Card
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                if (isUrdu) "حساب: واجب الادا منہا وصولی" else "Equation: Due - Received",
                                fontSize = 9.5.sp,
                                color = Color.Gray
                            )
                            Text(
                                "${formatCurrency(dueAmount, isUrdu)} - ${formatCurrency(payingAmount, isUrdu)}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                if (isUrdu) "نیا باقی واجب الادا:" else "New Due Balance:",
                                fontSize = 9.5.sp,
                                color = Color.Gray
                            )
                            Text(
                                formatCurrency(newRemainingDue, isUrdu),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.5.sp,
                                color = if (newRemainingDue == 0.0) PosGreen else PosRed
                            )
                        }
                    }
                }

                // Method Selector Row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val methods = listOf("Cash", "Online", "Bank", "Cheque")
                    methods.forEach { m ->
                        val sel = m == method
                        val displayMethod = when (m) {
                            "Cash" -> if (isUrdu) "نقد" else "Cash"
                            "Online" -> if (isUrdu) "آن لائن" else "Online"
                            "Bank" -> if (isUrdu) "بینک" else "Bank"
                            "Cheque" -> if (isUrdu) "چیک" else "Cheque"
                            else -> m
                        }
                        FilterChip(
                            selected = sel,
                            onClick = { method = m },
                            label = { Text(displayMethod, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color(0xFF1C1F22)
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text(if (method == "Cheque") (if (isUrdu) "چیک نمبر / بینک" else "Cheque # / Bank") else (if (isUrdu) "حوالہ / نوٹ" else "Reference / Note")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = receivedBy,
                        onValueChange = { receivedBy = it },
                        label = { Text(AppStrings.receivedBy(isUrdu)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = receivedIn,
                        onValueChange = { receivedIn = it },
                        label = { Text(AppStrings.receivedIn(isUrdu)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // History List if any
                if (history.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (isUrdu) "سابقہ ادائیگیاں:" else "Past Payments:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    history.forEach { p ->
                        Text("• ${p.date}: ${formatCurrency(p.amount, isUrdu)} (${p.method})", fontSize = 10.5.sp, color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirmPayment(amt, method, detail, receivedBy, receivedIn)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color(0xFF1C1F22))
            ) {
                Text(AppStrings.savePayment(isUrdu))
            }
        },
        dismissButton = {
            TextButton(onClick = onNilFull) {
                Text(AppStrings.nilFullBalance(isUrdu), color = PosGreen)
            }
        }
    )
}

@Composable
fun EditTransactionDialog(
    transaction: OrderTransactionEntity,
    isUrdu: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (OrderTransactionEntity) -> Unit
) {
    var summary by remember { mutableStateOf(transaction.itemsSummary) }
    var useFormula by remember { mutableStateOf(false) }
    var quantityStr by remember { mutableStateOf((transaction.itemCount ?: 1).toString()) }
    var unitRateStr by remember {
        val qty = (transaction.itemCount ?: 1).coerceAtLeast(1)
        mutableStateOf((transaction.total / qty).toInt().toString())
    }
    var discountStr by remember { mutableStateOf("0") }
    var directTotalStr by remember { mutableStateOf(transaction.total.toInt().toString()) }

    // Auto calculate from formula
    val autoTotal = remember(quantityStr, unitRateStr, discountStr) {
        val q = quantityStr.toDoubleOrNull() ?: 0.0
        val r = unitRateStr.toDoubleOrNull() ?: 0.0
        val d = discountStr.toDoubleOrNull() ?: 0.0
        ((q * r) - d).coerceAtLeast(0.0)
    }

    val finalTotal = if (useFormula) autoTotal else (directTotalStr.toDoubleOrNull() ?: transaction.total)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrdu) "آرڈر #${transaction.id} تبدیل کریں" else "Edit Transaction #${transaction.id}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text(if (isUrdu) "آئٹمز کی تفصیل" else "Items Summary") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Toggle formula mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isUrdu) "حسابی فارمولا (تعداد × ریٹ منہا رعایت)" else "Auto Solve Formula (Qty × Rate - Disc)", fontSize = 11.sp)
                    Switch(checked = useFormula, onCheckedChange = { useFormula = it })
                }

                if (useFormula) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text(if (isUrdu) "تعداد" else "Qty") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unitRateStr,
                            onValueChange = { unitRateStr = it },
                            label = { Text(if (isUrdu) "ریٹ" else "Rate") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discountStr,
                            onValueChange = { discountStr = it },
                            label = { Text(if (isUrdu) "رعایت" else "Disc") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${quantityStr} × Rs ${unitRateStr} - Rs ${discountStr} = Rs ${finalTotal.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = directTotalStr,
                        onValueChange = { directTotalStr = it },
                        label = { Text(if (isUrdu) "کل رقم (روپے)" else "Total Rs") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val q = quantityStr.toIntOrNull() ?: transaction.itemCount
                onSave(transaction.copy(itemsSummary = summary, total = finalTotal, itemCount = q))
            }) {
                Text(if (isUrdu) "محفوظ کریں" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isUrdu) "منسوخ" else "Cancel") }
        }
    )
}

@Composable
fun OrderReceiptPrintDialog(
    transaction: OrderTransactionEntity,
    payments: List<CustomerPaymentEntity>,
    isUrdu: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val totalPaid = payments.sumOf { it.amount }
    val dueAmount = (transaction.total - totalPaid).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Official Brand Logo
                AppBrandLogo(
                    modifier = Modifier.size(60.dp),
                    showBorder = true
                )

                Text(
                    text = if (isUrdu) "فالکن راڈ میکر" else "FALCON ROD MAKER",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = if (isUrdu) "پنکھا پرزہ جات · گجرات، پاکستان" else "Ceiling Fan Accessories · Gujrat, Pakistan",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isUrdu) "سیلز آرڈر / انوائس رسید" else "OFFICIAL SALES RECEIPT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Meta Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(if (isUrdu) "آرڈر نمبر: #${transaction.id}" else "Order #: #${transaction.id}", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Text("${transaction.date} · ${transaction.time}", fontSize = 11.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(transaction.factory ?: (if (isUrdu) "عام خریدار" else "Walk-in Customer"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text(if (transaction.confirmed) (if (isUrdu) "تصدیق شدہ" else "Confirmed") else (if (isUrdu) "غیر تصدیق شدہ" else "Unconfirmed"), fontSize = 10.sp, color = if (transaction.confirmed) PosGreen else PosAmber)
                    }
                }

                // Items summary box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isUrdu) "آئٹم تفصیل" else "Item Details", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(if (isUrdu) "تعداد" else "Qty", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(transaction.itemsSummary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text("${transaction.itemCount} pcs", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        if (!transaction.sizes.isNullOrBlank()) {
                            Text(
                                text = "${if (isUrdu) "سائز: " else "Sizes: "}${transaction.sizes.replace("\n", ", ")}",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Financial Summary
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isUrdu) "کل رقم:" else "Total Amount:", fontSize = 12.sp)
                        Text(formatCurrency(transaction.total, isUrdu), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isUrdu) "ادا شدہ رقم:" else "Paid Amount:", fontSize = 12.sp, color = PosGreen)
                        Text(formatCurrency(totalPaid, isUrdu), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PosGreen, fontFamily = FontFamily.Monospace)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isUrdu) "باقی واجب الادا:" else "Balance Due:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(formatCurrency(dueAmount, isUrdu), fontWeight = FontWeight.Black, fontSize = 14.sp, color = if (dueAmount > 0) PosRed else PosGreen, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shareText = """
                        *FALCON ROD MAKER*
                        Fan Accessories · Gujrat, Pakistan
                        --------------------------------
                        *Receipt #:* ${transaction.id}
                        *Date:* ${transaction.date} ${transaction.time}
                        *Party:* ${transaction.factory ?: "Walk-in"}
                        --------------------------------
                        *Items:* ${transaction.itemsSummary}
                        *Qty:* ${transaction.itemCount} pcs
                        *Sizes:* ${transaction.sizes ?: "Standard"}
                        --------------------------------
                        *Total:* Rs ${transaction.total.toInt()}
                        *Paid:* Rs ${totalPaid.toInt()}
                        *Balance Due:* Rs ${dueAmount.toInt()}
                        --------------------------------
                        Thank you for your business!
                    """.trimIndent()
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share / Print Receipt"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color(0xFF1C1F22))
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isUrdu) "رسید شیئر / پرنٹ کریں" else "Share / Print Receipt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.close(isUrdu))
            }
        }
    )
}
