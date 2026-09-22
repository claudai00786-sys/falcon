package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.ProductEntity
import com.example.ui.components.PosSectionHeader
import com.example.ui.components.PosStatCard
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatQuantity
import com.example.ui.theme.PosGreen
import com.example.ui.theme.PosOrange
import com.example.ui.theme.PosRed
import com.example.ui.theme.PosRedLight
import com.example.viewmodel.PosViewModel
import org.json.JSONArray

@Composable
fun StockScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isUrdu = currentLanguage == "ur"
    val transactions by viewModel.transactions.collectAsState()
    val products by viewModel.products.collectAsState()
    val scrapEntries by viewModel.scrapEntries.collectAsState()
    val inventoryMovements by viewModel.inventoryMovements.collectAsState()

    var productToAdjust by remember { mutableStateOf<ProductEntity?>(null) }
    var filterLowStockOnly by remember { mutableStateOf(false) }

    // Stock Ready: confirmed orders not yet delivered
    val stockReadyOrders = remember(transactions) {
        transactions.filter { it.confirmed && !it.delivered }
    }
    val stockReadyTotalValue = remember(stockReadyOrders) {
        stockReadyOrders.sumOf { it.total }
    }

    // Computed Raw Material Stock Map
    val rawStockMap = remember(transactions, viewModel.rawMaterialEntries.collectAsState().value) {
        viewModel.computeRawMaterialStock()
    }
    val rawStockList = remember(rawStockMap) { rawStockMap.values.toList() }
    val lowRawStockCount = remember(rawStockList) { rawStockList.count { it.isLowStock } }

    // Filtered Products
    val displayedProducts = remember(products, filterLowStockOnly) {
        if (filterLowStockOnly) {
            products.filter { (it.stock ?: 0) <= (it.reorderLevel ?: 5) }
        } else {
            products
        }
    }

    // Scrap Totals
    val totalScrapWeight = remember(scrapEntries) {
        scrapEntries.sumOf { it.weightIn ?: it.credit }
    }
    val totalScrapPayments = remember(scrapEntries) {
        scrapEntries.sumOf { it.credit }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        // Section 1: Stock Ready (in Factory)
        item {
            PosSectionHeader(
                title = if (isUrdu) "اسٹاک تیار (فیکٹری میں)" else "Stock Ready (in Factory)",
                subtitle = if (isUrdu) "تصدیق شدہ آرڈرز جو فیکٹری میں تیار ہیں اور ڈیلیوری کے منتظر ہیں" else "Confirmed factory orders awaiting delivery gate receipt"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PosStatCard(
                    label = if (isUrdu) "تیار آرڈرز" else "Orders Ready",
                    value = "${stockReadyOrders.size}",
                    modifier = Modifier.weight(1f)
                )
                PosStatCard(
                    label = if (isUrdu) "تیار اسٹاک مالیت" else "Stock Ready Value",
                    value = formatCurrency(stockReadyTotalValue, isUrdu),
                    accentColor = PosOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (stockReadyOrders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isUrdu) "اس وقت فیکٹری میں کوئی تیار آرڈر باقی نہیں ہے — تمام ڈیلیور ہو چکے ہیں۔" else "No stock ready at factory right now — all confirmed orders delivered.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(stockReadyOrders) { order ->
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
                            Text("#${order.id} · ${order.factory ?: (if (isUrdu) "ورکشاپ" else "Workshop")}", fontWeight = FontWeight.Bold)
                            Text(order.itemsSummary.replace("\n", ", "), fontSize = 11.5.sp, color = Color.Gray)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatCurrency(order.total, isUrdu), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { viewModel.markOrderDelivered(order.id) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(if (isUrdu) "ڈیلیور" else "Deliver", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Raw Material Stock
        item {
            PosSectionHeader(
                title = if (isUrdu) "خام مال اسٹاک" else "Raw Material Stock",
                subtitle = if (isUrdu) "موصولہ وزن بنام آرڈرز کے نسخے کے مطابق استعمال شدہ وزن" else "Weight In minus Weight Used by all booked order recipes"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PosStatCard(
                    label = if (isUrdu) "ٹریک شدہ میٹریل" else "Materials Tracked",
                    value = "${rawStockList.size}",
                    modifier = Modifier.weight(1f)
                )
                PosStatCard(
                    label = if (isUrdu) "کم اسٹاک وارننگ" else "Low Stock Alerts",
                    value = "$lowRawStockCount",
                    accentColor = if (lowRawStockCount > 0) PosRed else PosGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (rawStockList.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isUrdu) "خام مال کا کوئی اندراج موجود نہیں۔" else "No raw material stock entries yet.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(rawStockList) { stock ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (stock.isLowStock) PosRed else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stock.materialName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (stock.isLowStock) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PosRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isUrdu) "کم اسٹاک" else "LOW STOCK",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PosRed,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(if (isUrdu) "موصولہ وزن" else "Received In", fontSize = 10.sp, color = Color.Gray)
                                Text("${formatQuantity(stock.weightReceived)} ${if (isUrdu) "کلو" else "kg"}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                            Column {
                                Text(if (isUrdu) "استعمال شدہ" else "Used in Orders", fontSize = 10.sp, color = Color.Gray)
                                Text("${formatQuantity(stock.weightUsed)} ${if (isUrdu) "کلو" else "kg"}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(if (isUrdu) "باقی اسٹاک" else "Remaining", fontSize = 10.sp, color = Color.Gray)
                                Text(
                                    "${formatQuantity(stock.weightRemaining)} ${if (isUrdu) "کلو" else "kg"}",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = if (stock.isLowStock) PosRed else PosGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Product Stock on Hand & Can Still Build
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PosSectionHeader(
                    title = if (isUrdu) "پروڈکٹ انوینٹری و اسٹاک (مقامی روم ڈیٹا بیس)" else "Product Inventory & On-Hand Stock (Room DB)",
                    subtitle = if (isUrdu) "ڈیوائس پر محفوظ اسٹاک — انٹرنیٹ کے بغیر درستگی و ری اسٹاک ممکن ہے" else "Device-persisted stock with offline physical count adjustment & restock"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filterLowStockOnly,
                        onClick = { filterLowStockOnly = !filterLowStockOnly },
                        label = {
                            Text(
                                text = if (isUrdu) "صرف کم اسٹاک دکھائیں" else "Low Stock Only",
                                fontSize = 11.sp
                            )
                        },
                        leadingIcon = if (filterLowStockOnly) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            Text(
                                text = if (isUrdu) "روم ڈی بی (آف لائن تیار)" else "Room DB (Offline Ready)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        items(displayedProducts) { p ->
            // Calculate how many can still be built based on remaining raw materials
            val canBuild = remember(p, rawStockMap) {
                if (p.recipeJson.isNullOrBlank()) null
                else {
                    try {
                        val arr = JSONArray(p.recipeJson)
                        var maxUnits = Int.MAX_VALUE
                        for (i in 0 until arr.length()) {
                            val item = arr.getJSONObject(i)
                            val mat = item.getString("material")
                            val wUnit = if (item.has("weightPerUnit") && !item.isNull("weightPerUnit")) item.getDouble("weightPerUnit") else 0.0
                            if (wUnit > 0) {
                                val remaining = rawStockMap[mat]?.weightRemaining ?: 0.0
                                val possible = (remaining / wUnit).toInt()
                                if (possible < maxUnits) maxUnits = possible
                            }
                        }
                        if (maxUnits == Int.MAX_VALUE) null else maxUnits.coerceAtLeast(0)
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            val currentStock = p.stock ?: 0
            val isLowStock = currentStock <= (p.reorderLevel ?: 5)

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isLowStock) PosRed else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                if (isLowStock) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = PosRedLight
                                    ) {
                                        Text(
                                            text = if (isUrdu) "کم اسٹاک" else "LOW",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PosRed,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text("${p.cat} · ${p.size ?: ""} · ${formatCurrency(p.price, isUrdu)}", fontSize = 11.sp, color = Color.Gray)
                        }

                        // Adjust / Restock Button
                        OutlinedButton(
                            onClick = { productToAdjust = p },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isUrdu) "اسٹاک درست کریں" else "Adjust/Restock", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isUrdu) "موجودہ انوینٹری" else "On-Hand Stock", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "$currentStock ${if (isUrdu) "عدد" else "units"}",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = if (isLowStock) PosRed else PosGreen
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (isUrdu) "مزید بن سکتے ہیں" else "Can Build", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                if (canBuild != null) "$canBuild ${if (isUrdu) "عدد" else "units"}" else "—",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = if (canBuild != null && canBuild > 0) PosGreen else PosOrange
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Inventory Movements History (Room DB Audit Log)
        item {
            PosSectionHeader(
                title = if (isUrdu) "اسٹاک تاریخچہ و حرکات (آڈٹ لاگ)" else "Inventory Movements & Audit Trail",
                subtitle = if (isUrdu) "روم ڈیٹا بیس میں تمام فروخت، ری اسٹاک اور تصحیحات کا مکمل محفوظ ریکارڈ" else "Complete Room database log of all sales, restocks, and manual count adjustments"
            )
        }

        if (inventoryMovements.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isUrdu) "ابھی تک کوئی اسٹاک موومنٹ ریکارڈ نہیں ہوئی۔" else "No inventory movement logs found in Room database.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(inventoryMovements.take(30)) { movement ->
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val (badgeColor, badgeText) = when (movement.movementType) {
                                    "RESTOCK" -> PosGreen to (if (isUrdu) "ری اسٹاک" else "RESTOCK")
                                    "SALE" -> PosOrange to (if (isUrdu) "فروخت" else "SALE")
                                    "INITIAL_STOCK" -> MaterialTheme.colorScheme.primary to (if (isUrdu) "ابتدائی اسٹاک" else "INIT")
                                    "ADJUSTMENT" -> Color(0xFF9C27B0) to (if (isUrdu) "تصحیح" else "ADJUST")
                                    else -> Color.Gray to movement.movementType
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = badgeColor.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = badgeText,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = movement.productName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${movement.date} ${movement.time} · ${movement.referenceId ?: ""} ${if (!movement.note.isNullOrBlank()) "(${movement.note})" else ""}",
                                fontSize = 10.5.sp,
                                color = Color.Gray
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val changeText = if (movement.quantityChange > 0) "+${movement.quantityChange}" else "${movement.quantityChange}"
                            val changeColor = if (movement.quantityChange >= 0) PosGreen else PosOrange

                            Text(
                                text = changeText,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = changeColor
                            )
                            Text(
                                text = "${movement.previousStock} → ${movement.newStock}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Section 5: Scrap Stock Rollup
        item {
            PosSectionHeader(
                title = if (isUrdu) "سکریپ اسٹاک سمری" else "Scrap Stock Summary",
                subtitle = if (isUrdu) "تمام خریداروں کو فروخت شدہ سکریپ وزن اور وصول شدہ رقم" else "Total scrap weight sold & payment received across all buyers"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PosStatCard(
                    label = if (isUrdu) "کل فروخت شدہ سکریپ" else "Total Scrap Sold",
                    value = "${formatQuantity(totalScrapWeight)} ${if (isUrdu) "کلو" else "kg"}",
                    modifier = Modifier.weight(1f)
                )
                PosStatCard(
                    label = if (isUrdu) "سکریپ کی ادائیگیاں" else "Scrap Payments",
                    value = formatCurrency(totalScrapPayments, isUrdu),
                    accentColor = PosGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Adjust / Restock Dialog
    if (productToAdjust != null) {
        AdjustStockDialog(
            product = productToAdjust!!,
            isUrdu = isUrdu,
            onDismiss = { productToAdjust = null },
            onRestock = { addedQty, note ->
                viewModel.restockProduct(productToAdjust!!, addedQty, note)
                productToAdjust = null
            },
            onAdjust = { newCount, reason, note ->
                viewModel.adjustProductStock(
                    product = productToAdjust!!,
                    newStock = newCount,
                    movementType = "ADJUSTMENT",
                    reason = reason,
                    note = note
                )
                productToAdjust = null
            }
        )
    }
}

@Composable
fun AdjustStockDialog(
    product: ProductEntity,
    isUrdu: Boolean,
    onDismiss: () -> Unit,
    onRestock: (addedQty: Int, note: String) -> Unit,
    onAdjust: (newCount: Int, reason: String, note: String) -> Unit
) {
    var mode by remember { mutableStateOf("RESTOCK") } // "RESTOCK" or "ADJUST"
    var quantityInput by remember { mutableStateOf("") }
    var reasonInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    val currentStock = product.stock ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isUrdu) "اسٹاک اپ ڈیٹ — ${product.name}" else "Update Stock — ${product.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (isUrdu) "موجودہ اسٹاک: $currentStock عدد" else "Current On-Hand Stock: $currentStock units",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mode selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = mode == "RESTOCK",
                        onClick = { mode = "RESTOCK" },
                        label = { Text(if (isUrdu) "ری اسٹاک (+اضافہ)" else "Restock (+Add)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = mode == "ADJUST",
                        onClick = { mode = "ADJUST" },
                        label = { Text(if (isUrdu) "فزیکل کاؤنٹ (درستگی)" else "Physical Count", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it.filter { ch -> ch.isDigit() } },
                    label = {
                        Text(
                            if (mode == "RESTOCK") (if (isUrdu) "اضافی تعداد" else "Units to Add")
                            else (if (isUrdu) "نیا فزیکل اسٹاک" else "New Total Count")
                        )
                    },
                    placeholder = { Text(if (mode == "RESTOCK") "e.g. 20" else "e.g. 45") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (mode == "ADJUST") {
                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        label = { Text(if (isUrdu) "وجہ تصحیح" else "Reason for Adjustment") },
                        placeholder = { Text(if (isUrdu) "مثلاً: ہفتہ وار فزیکل گنتی" else "e.g. Physical Count Audit, Damage") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text(if (isUrdu) "نوٹ / تفصیل" else "Note / Reference") },
                    placeholder = { Text(if (isUrdu) "اختیاری" else "Optional reference note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = PosGreen, modifier = Modifier.size(16.dp))
                        Text(
                            text = if (isUrdu) "یہ تبدیلی مقامی روم ڈیٹا بیس میں محفوظ ہوگی اور آف لائن کام کرے گی۔" else "Directly saved to local Room DB. Functions 100% offline.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityInput.toIntOrNull() ?: 0
                    if (qty > 0 || (mode == "ADJUST" && quantityInput.isNotBlank())) {
                        if (mode == "RESTOCK") {
                            onRestock(qty, noteInput)
                        } else {
                            val r = if (reasonInput.isBlank()) "Physical Audit" else reasonInput
                            onAdjust(qty, r, noteInput)
                        }
                    }
                },
                enabled = quantityInput.isNotBlank()
            ) {
                Text(if (isUrdu) "محفوظ کریں" else "Save to Room DB")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isUrdu) "منسوخ" else "Cancel")
            }
        }
    )
}
