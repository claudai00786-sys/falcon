package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductReturnEntity
import com.example.ui.components.PosSectionHeader
import com.example.ui.components.PosStatCard
import com.example.ui.theme.PosGreen
import com.example.ui.theme.PosOrange
import com.example.ui.theme.PosRed
import com.example.ui.util.AppStrings
import com.example.viewmodel.PosViewModel

@Composable
fun ProductReturnsScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isUrdu = currentLanguage == "ur"
    val returns by viewModel.productReturns.collectAsState()
    val factories by viewModel.factories.collectAsState()
    val products by viewModel.products.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val totalItemsReturned = remember(returns) { returns.sumOf { it.quantity.toInt() } }
    val scrapCount = remember(returns) { returns.count { it.resolution.contains("scrap", ignoreCase = true) } }
    val reworkCount = remember(returns) { returns.count { it.resolution.contains("rework", ignoreCase = true) } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // KPI Stat Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PosStatCard(
                label = if (isUrdu) "کل واپس شدہ آئٹمز" else "Total Units Returned",
                value = "$totalItemsReturned ${if (isUrdu) "عدد" else "pcs"}",
                subValue = if (isUrdu) "${returns.size} واقعات" else "${returns.size} incidents",
                modifier = Modifier.weight(1f)
            )
            PosStatCard(
                label = if (isUrdu) "مرمت بنام کباڑ" else "Rework vs Scrap",
                value = "$reworkCount / $scrapCount",
                accentColor = if (scrapCount > 0) PosOrange else PosGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Section Title & Add Return Button
        PosSectionHeader(
            title = if (isUrdu) "خراب و واپس شدہ مال" else "Damaged & Returned Goods",
            subtitle = if (isUrdu) "گاہک کی طرف سے واپس آنے والے نقائص کا اندراج اور دوبارہ مرمت یا کباڑ میں منتقلی" else "Log defective customer batches and route to rework or scrap",
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
                    Text(if (isUrdu) "واپسی درج کریں" else "Log Return")
                }
            }
        )

        // Returns List
        if (returns.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isUrdu) "کوئی واپسی لاگ نہیں ہے — بہترین کوالٹی کنٹرول!" else "No return incidents recorded — great quality control!",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(returns) { item ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.product} (${item.quantity.toInt()} ${if (isUrdu) "عدد" else "pcs"})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                val isScrap = item.resolution.contains("scrap", ignoreCase = true)
                                val actionColor = if (isScrap) PosRed else PosGreen
                                val resolutionText = when {
                                    item.resolution.contains("rework", ignoreCase = true) -> if (isUrdu) "مرمت" else "REWORK"
                                    item.resolution.contains("scrap", ignoreCase = true) -> if (isUrdu) "کباڑ / سکریپ" else "SCRAP"
                                    item.resolution.contains("credit", ignoreCase = true) -> if (isUrdu) "کریڈٹ نوٹ" else "CREDIT NOTE"
                                    item.resolution.isNotBlank() -> item.resolution.uppercase()
                                    else -> item.status.uppercase()
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = actionColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = resolutionText,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = actionColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (isUrdu) "فیکٹری" else "Factory"}: ${item.factory} · ${if (isUrdu) "تاریخ" else "Date"}: ${item.date}",
                                fontSize = 11.5.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (isUrdu) "وجہ / نقص" else "Reason"}: ${item.reason}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { viewModel.deleteProductReturn(item.id) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PosRed, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Return Dialog
    if (showAddDialog) {
        AddReturnDialog(
            factories = factories.map { it.name },
            products = products.map { it.name },
            isUrdu = isUrdu,
            onDismiss = { showAddDialog = false },
            onSave = { customer, product, qty, reason, action ->
                viewModel.addProductReturn(customer, product, qty.toDouble(), reason, action)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddReturnDialog(
    factories: List<String>,
    products: List<String>,
    isUrdu: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (customer: String, product: String, qty: Int, reason: String, action: String) -> Unit
) {
    var customer by remember { mutableStateOf(factories.firstOrNull() ?: if (isUrdu) "واک ان گاہک" else "Walk-in") }
    var product by remember { mutableStateOf(products.firstOrNull() ?: if (isUrdu) "پنکھا پرزہ جات" else "Fan Accessory") }
    var qtyStr by remember { mutableStateOf("1") }
    var reason by remember { mutableStateOf(if (isUrdu) "ویلڈنگ پوائنٹ ٹوٹا ہوا" else "Broken weld point") }
    var action by remember { mutableStateOf("Rework") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrdu) "واپس شدہ مال درج کریں" else "Log Product Return") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = customer,
                    onValueChange = { customer = it },
                    label = { Text(if (isUrdu) "کسٹمر / فیکٹری" else "Customer / Factory") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = product,
                    onValueChange = { product = it },
                    label = { Text(if (isUrdu) "پروڈکٹ کا نام" else "Product Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = { qtyStr = it },
                    label = { Text(if (isUrdu) "تعداد (عدد)" else "Quantity (pcs)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(if (isUrdu) "خرابی کی وجہ" else "Defect Reason") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(if (isUrdu) "کارروائی / حل:" else "Resolution Action:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val actions = listOf(
                        "Rework" to if (isUrdu) "مرمت" else "Rework",
                        "Scrap" to if (isUrdu) "سکریپ" else "Scrap",
                        "Credit Note" to if (isUrdu) "کریڈٹ نوٹ" else "Credit Note"
                    )
                    actions.forEach { (act, label) ->
                        val sel = act == action
                        FilterChip(
                            selected = sel,
                            onClick = { action = act },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val q = qtyStr.toIntOrNull() ?: 1
                if (customer.isNotBlank() && product.isNotBlank()) {
                    onSave(customer, product, q, reason, action)
                }
            }) {
                Text(if (isUrdu) "محفوظ کریں" else "Log Return")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.cancel(isUrdu)) }
        }
    )
}
