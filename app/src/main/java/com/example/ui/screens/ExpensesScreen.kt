package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.data.model.WithdrawalEntity
import com.example.ui.components.PosSectionHeader
import com.example.ui.components.PosStatCard
import com.example.ui.components.formatCurrency
import com.example.ui.theme.PosGreen
import com.example.ui.theme.PosOrange
import com.example.ui.theme.PosRed
import com.example.ui.util.AppStrings
import com.example.viewmodel.PosViewModel

@Composable
fun ExpensesScreen(
    viewModel: PosViewModel,
    isWithdrawalMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isUrdu = currentLanguage == "ur"
    val expenses by viewModel.expenses.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()
    val cashInHand by viewModel.cashInHand.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }
    val totalWithdrawals = remember(withdrawals) { withdrawals.sumOf { it.amount } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Stat Card Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isWithdrawalMode) {
                PosStatCard(
                    label = if (isUrdu) "کل روزانہ اخراجات" else "Total Expenses",
                    value = formatCurrency(totalExpenses, isUrdu),
                    accentColor = PosOrange,
                    modifier = Modifier.weight(1f)
                )
                PosStatCard(
                    label = if (isUrdu) "کاؤنٹر نقد کیش" else "Shop Cash in Hand",
                    value = formatCurrency(cashInHand, isUrdu),
                    accentColor = PosGreen,
                    subValue = if (isUrdu) "خودکار حسابی بیلنس" else "Auto-computed balance",
                    modifier = Modifier.weight(1f)
                )
            } else {
                PosStatCard(
                    label = if (isUrdu) "کل نکاسی" else "Total Withdrawn",
                    value = formatCurrency(totalWithdrawals, isUrdu),
                    accentColor = PosRed,
                    subValue = if (isUrdu) "کاروباری خرچہ شمار نہیں ہوتا" else "Not counted as business expense",
                    modifier = Modifier.weight(1f)
                )
                PosStatCard(
                    label = if (isUrdu) "دستیاب کاؤنٹر کیش" else "Cash Available",
                    value = formatCurrency(cashInHand, isUrdu),
                    accentColor = PosGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section Title
        PosSectionHeader(
            title = if (!isWithdrawalMode)
                (if (isUrdu) "روزانہ کے اخراجات" else "Daily Expenses")
            else
                (if (isUrdu) "نکاسی لیجر (مالک)" else "Owner Withdrawals"),
            subtitle = if (!isWithdrawalMode)
                (if (isUrdu) "خام مال، بل، مرمت، دوپہر کا کھانا، رکشہ و ٹرانسپورٹ" else "Materials, utility bills, maintenance, lunch, and transport")
            else
                (if (isUrdu) "مالک کی ذاتی ضروریات کے لیے شاپ کے کیش سے رقم کی نکاسی" else "Personal owner drawings from shop cash-on-hand"),
            actionButton = {
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color(0xFF1C1F22)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (!isWithdrawalMode)
                            (if (isUrdu) "خرچہ شامل کریں" else "Add Expense")
                        else
                            (if (isUrdu) "نکاسی درج کریں" else "Log Withdrawal")
                    )
                }
            }
        )

        // List
        if (!isWithdrawalMode) {
            if (expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(if (isUrdu) "کوئی خرچہ درج نہیں ہوا" else "No expenses recorded yet")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(expenses) { exp ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(exp.desc, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("${exp.category} · ${exp.date} · ${exp.method}", fontSize = 11.sp, color = Color.Gray)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        formatCurrency(exp.amount, isUrdu),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = PosOrange,
                                        fontSize = 14.sp
                                    )
                                    IconButton(onClick = { viewModel.deleteExpense(exp.id) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PosRed, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (withdrawals.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(if (isUrdu) "کوئی نکاسی درج نہیں" else "No withdrawals recorded yet")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(withdrawals) { wd ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(wd.desc, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("${wd.date} · ${wd.method} ${if (wd.isReversal) (if (isUrdu) "· (واپسی)" else "· (Reversal)") else ""}", fontSize = 11.sp, color = Color.Gray)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        formatCurrency(wd.amount, isUrdu),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (wd.amount < 0) PosGreen else PosRed,
                                        fontSize = 14.sp
                                    )
                                    IconButton(onClick = { viewModel.deleteWithdrawal(wd.id) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PosRed, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        if (!isWithdrawalMode) {
            AddExpenseDialog(
                isUrdu = isUrdu,
                onDismiss = { showAddDialog = false },
                onSave = { desc, cat, amt, meth, det ->
                    viewModel.addExpense(desc, cat, amt, meth, det)
                    showAddDialog = false
                }
            )
        } else {
            AddWithdrawalDialog(
                cashInHand = cashInHand,
                isUrdu = isUrdu,
                onDismiss = { showAddDialog = false },
                onSave = { desc, amt, meth, det, rev ->
                    viewModel.addWithdrawal(desc, amt, meth, det, rev)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddExpenseDialog(
    isUrdu: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (desc: String, category: String, amount: Double, method: String, detail: String) -> Unit
) {
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Raw Material") }
    var method by remember { mutableStateOf("Cash") }
    var detail by remember { mutableStateOf("") }

    // Mathematical Formula Auto-Solve States
    var useFormula by remember { mutableStateOf(false) }
    var quantityStr by remember { mutableStateOf("1") }
    var unitRateStr by remember { mutableStateOf("") }
    var extraChargesStr by remember { mutableStateOf("0") }
    var discountStr by remember { mutableStateOf("0") }
    var directAmountStr by remember { mutableStateOf("") }

    val autoSolvedAmount = remember(quantityStr, unitRateStr, extraChargesStr, discountStr) {
        val q = quantityStr.toDoubleOrNull() ?: 0.0
        val r = unitRateStr.toDoubleOrNull() ?: 0.0
        val extra = extraChargesStr.toDoubleOrNull() ?: 0.0
        val disc = discountStr.toDoubleOrNull() ?: 0.0
        ((q * r) + extra - disc).coerceAtLeast(0.0)
    }

    val finalAmount = if (useFormula) autoSolvedAmount else (directAmountStr.toDoubleOrNull() ?: 0.0)

    val categories = listOf(
        "Raw Material" to if (isUrdu) "خام مال" else "Raw Material",
        "Labour" to if (isUrdu) "مزدوری" else "Labour",
        "Paint" to if (isUrdu) "رنگ / پینٹ" else "Paint",
        "Electricity" to if (isUrdu) "بجلی" else "Electricity",
        "Gas" to if (isUrdu) "گیس" else "Gas",
        "Sanitation" to if (isUrdu) "صفائی" else "Sanitation",
        "Rent" to if (isUrdu) "کرایہ" else "Rent",
        "Transport" to if (isUrdu) "ٹرانسپورٹ" else "Transport"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrdu) "خرچہ شامل کریں (خودکار حسابی فارمولا)" else "Add Expense (Auto-Solve Math)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(if (isUrdu) "تفصیل" else "Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Category dropdown / chips
                Text(if (isUrdu) "کیٹیگری:" else "Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { (key, label) ->
                        val sel = key == category
                        FilterChip(
                            selected = sel,
                            onClick = { category = key },
                            label = { Text(label, fontSize = 10.5.sp) }
                        )
                    }
                }

                // Toggle formula calculation mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isUrdu) "حسابی فارمولا (تعداد × ریٹ + کرایہ منہا چھوٹ)" else "Math Formula (Qty × Rate + Extra - Disc)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(checked = useFormula, onCheckedChange = { useFormula = it })
                }

                if (useFormula) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text(if (isUrdu) "تعداد / یونٹ" else "Qty / Units") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unitRateStr,
                            onValueChange = { unitRateStr = it },
                            label = { Text(if (isUrdu) "ریٹ فی یونٹ" else "Rate/Unit") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = extraChargesStr,
                            onValueChange = { extraChargesStr = it },
                            label = { Text(if (isUrdu) "+ رکشہ / اضافی" else "+ Transport") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discountStr,
                            onValueChange = { discountStr = it },
                            label = { Text(if (isUrdu) "- رعایت" else "- Discount") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Auto-solved Equation Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = if (isUrdu) "خودکار حسابی نتیجہ:" else "Auto-Solved Result:",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "($quantityStr × Rs $unitRateStr) + Rs $extraChargesStr - Rs $discountStr = Rs ${finalAmount.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = directAmountStr,
                        onValueChange = { directAmountStr = it },
                        label = { Text(if (isUrdu) "رقم (روپے)" else "Amount (Rs)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text(if (isUrdu) "بل نمبر / ادائیگی نوٹ" else "Payment Note / Bill #") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (desc.isNotBlank() && finalAmount > 0) {
                    val finalDesc = if (useFormula && (unitRateStr.isNotBlank())) {
                        "$desc ($quantityStr × Rs $unitRateStr${if (extraChargesStr != "0") " + Rs $extraChargesStr" else ""})"
                    } else desc
                    onSave(finalDesc, category, finalAmount, method, detail)
                }
            }) { Text(AppStrings.save(isUrdu)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(AppStrings.cancel(isUrdu)) } }
    )
}

@Composable
fun AddWithdrawalDialog(
    cashInHand: Double,
    isUrdu: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (desc: String, amount: Double, method: String, detail: String, isReversal: Boolean) -> Unit
) {
    var desc by remember { mutableStateOf(if (isUrdu) "مالک کی نقد نکاسی" else "Owner Cash Withdrawal") }
    var amountStr by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("Cash") }
    var isReversal by remember { mutableStateOf(false) }

    val withdrawalAmount = amountStr.toDoubleOrNull() ?: 0.0
    val newShopBalance = if (isReversal) (cashInHand + withdrawalAmount) else (cashInHand - withdrawalAmount).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrdu) "نکاسی درج کریں (کاؤنٹر کیش کٹوتی)" else "Log Withdrawal (Counter Math)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Live Cash In Counter Header
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isUrdu) "موجودہ کاؤنٹر کیش:" else "Current Counter Cash:", fontSize = 11.5.sp)
                            Text(formatCurrency(cashInHand, isUrdu), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, color = PosGreen)
                        }

                        if (withdrawalAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    if (isReversal) (if (isUrdu) "+ شاپ میں رقم واپسی جمع:" else "+ Shop Cash Deposit:")
                                    else (if (isUrdu) "- نکاسی کٹوتی:" else "- Withdrawal Deduction:"),
                                    fontSize = 11.5.sp,
                                    color = if (isReversal) PosGreen else PosRed
                                )
                                Text(
                                    "${if (isReversal) "+" else "-"}${formatCurrency(withdrawalAmount, isUrdu)}",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = if (isReversal) PosGreen else PosRed
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (isUrdu) "= باقی کاؤنٹر کیش:" else "= Remaining Counter Cash:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    formatCurrency(newShopBalance, isUrdu),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = if (newShopBalance < 1000) PosRed else PosGreen
                                )
                            }
                        }
                    }
                }

                // Quick chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isUrdu) "فوری انتخاب:" else "Quick Fill:", fontSize = 11.sp, color = Color.Gray)
                    FilterChip(
                        selected = withdrawalAmount == 1000.0,
                        onClick = { amountStr = "1000" },
                        label = { Text("1,000", fontSize = 10.5.sp) }
                    )
                    FilterChip(
                        selected = withdrawalAmount == 5000.0,
                        onClick = { amountStr = "5000" },
                        label = { Text("5,000", fontSize = 10.5.sp) }
                    )
                    FilterChip(
                        selected = withdrawalAmount == 10000.0,
                        onClick = { amountStr = "10000" },
                        label = { Text("10,000", fontSize = 10.5.sp) }
                    )
                    if (cashInHand > 0) {
                        FilterChip(
                            selected = withdrawalAmount == cashInHand,
                            onClick = { amountStr = cashInHand.toInt().toString() },
                            label = { Text(if (isUrdu) "تمام نقد" else "All", fontSize = 10.5.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(if (isUrdu) "تفصیل" else "Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(if (isUrdu) "رقم (روپے)" else "Amount (Rs)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isReversal, onCheckedChange = { isReversal = it })
                    Text(if (isUrdu) "یہ واپسی ہے (رقم شاپ میں واپس جمع ہوئی)" else "This is a Reversal (money returned to shop)", fontSize = 11.5.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (desc.isNotBlank() && withdrawalAmount > 0) {
                    onSave(desc, withdrawalAmount, method, "", isReversal)
                }
            }) { Text(AppStrings.save(isUrdu)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(AppStrings.cancel(isUrdu)) } }
    )
}
