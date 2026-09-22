package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.AppBrandLogo
import com.example.data.model.*
import com.example.ui.components.PosSectionHeader
import com.example.ui.components.formatCurrency
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.viewmodel.PosViewModel

enum class LedgerTab {
    CUSTOMER_FACTORIES,
    PAINT,
    RAW_MATERIAL,
    SCRAP,
    LABOUR,
    CUSTOM_FACTORY
}

@Composable
fun LedgersScreen(
    viewModel: PosViewModel,
    initialTab: LedgerTab = LedgerTab.CUSTOMER_FACTORIES,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isUr = currentLanguage == "ur"
    var selectedTab by remember { mutableStateOf(initialTab) }

    val factories by viewModel.factories.collectAsState()
    val customerEntries by viewModel.customerLedgerEntries.collectAsState()
    val paintEntries by viewModel.paintEntries.collectAsState()
    val rawMaterialEntries by viewModel.rawMaterialEntries.collectAsState()
    val scrapEntries by viewModel.scrapEntries.collectAsState()
    val customEntries by viewModel.customLedgerEntries.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val labourEntries by viewModel.labourEntries.collectAsState()

    var showAddEntryDialog by remember { mutableStateOf(false) }
    var selectedPartyForDetail by remember { mutableStateOf<String?>(null) }
    var showAddFactoryDialog by remember { mutableStateOf(false) }
    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var showBulkAttendanceDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        LedgerTab.CUSTOMER_FACTORIES to (if (isUr) "فیکٹریاں (کسٹمرز)" else "Factories (Customers)"),
        LedgerTab.PAINT to (if (isUr) "پینٹ لیجر" else "Paint Ledger"),
        LedgerTab.RAW_MATERIAL to (if (isUr) "خام مال لیجر" else "Raw Material"),
        LedgerTab.SCRAP to (if (isUr) "سکریپ لیجر" else "Scrap Ledger"),
        LedgerTab.LABOUR to (if (isUr) "لیبر لیجر" else "Labour Ledger"),
        LedgerTab.CUSTOM_FACTORY to (if (isUr) "کسٹم فیکٹری لیجر" else "Custom Ledgers")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Tab Selector Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tabs) { (tab, title) ->
                val isSelected = tab == selectedTab
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedTab = tab
                        selectedPartyForDetail = null
                    },
                    label = { Text(title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color(0xFF1C1F22),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        // Content Area depending on Tab
        when (selectedTab) {
            LedgerTab.CUSTOMER_FACTORIES -> {
                CustomerFactoriesTab(
                    factories = factories,
                    entries = customerEntries,
                    selectedParty = selectedPartyForDetail,
                    isUrdu = isUr,
                    onSelectParty = { selectedPartyForDetail = it },
                    onAddFactory = { showAddFactoryDialog = true },
                    onAddEntry = { showAddEntryDialog = true },
                    onDeleteEntry = { viewModel.deleteLedgerEntry(it) },
                    onUpdateCheque = { id, st -> viewModel.updateChequeStatus(id, st) }
                )
            }
            LedgerTab.PAINT -> {
                GenericLedgerTab(
                    ledgerType = "PAINT",
                    title = if (isUr) "پینٹر کھاتہ جات" else "Painter Accounts",
                    parties = listOf("Rashid Painter", "Umar Finishing", "Asghar Coating"),
                    entries = paintEntries,
                    selectedParty = selectedPartyForDetail,
                    isUrdu = isUr,
                    onSelectParty = { selectedPartyForDetail = it },
                    onAddEntry = { showAddEntryDialog = true },
                    onDeleteEntry = { viewModel.deleteLedgerEntry(it) },
                    onUpdateCheque = { id, st -> viewModel.updateChequeStatus(id, st) }
                )
            }
            LedgerTab.RAW_MATERIAL -> {
                GenericLedgerTab(
                    ledgerType = "RAW_MATERIAL",
                    title = if (isUr) "خام مال سپلائرز" else "Raw Material Suppliers",
                    parties = listOf("Steel Wire Supplier (Gujranwala)", "Chutki & Tala Vendor", "Karri & Patri Works"),
                    entries = rawMaterialEntries,
                    selectedParty = selectedPartyForDetail,
                    isUrdu = isUr,
                    onSelectParty = { selectedPartyForDetail = it },
                    onAddEntry = { showAddEntryDialog = true },
                    onDeleteEntry = { viewModel.deleteLedgerEntry(it) },
                    onUpdateCheque = { id, st -> viewModel.updateChequeStatus(id, st) }
                )
            }
            LedgerTab.SCRAP -> {
                GenericLedgerTab(
                    ledgerType = "SCRAP",
                    title = if (isUr) "سکریپ خریدار" else "Scrap Buyers",
                    parties = listOf("Rafiq Scrap Dealer", "Gujrat Metal Recycling"),
                    entries = scrapEntries,
                    selectedParty = selectedPartyForDetail,
                    isUrdu = isUr,
                    onSelectParty = { selectedPartyForDetail = it },
                    onAddEntry = { showAddEntryDialog = true },
                    onDeleteEntry = { viewModel.deleteLedgerEntry(it) },
                    onUpdateCheque = { id, st -> viewModel.updateChequeStatus(id, st) }
                )
            }
            LedgerTab.LABOUR -> {
                LabourLedgerTab(
                    workers = workers,
                    entries = labourEntries,
                    selectedWorker = selectedPartyForDetail,
                    isUrdu = isUr,
                    onSelectWorker = { selectedPartyForDetail = it },
                    onAddWorker = { showAddWorkerDialog = true },
                    onBulkAttendance = { showBulkAttendanceDialog = true },
                    onMarkAttendance = { name, st, units, note -> viewModel.markWorkerAttendance(name, st, units, note) },
                    onRecordPayment = { name, amt, kind, meth, note -> viewModel.recordWorkerPayment(name, amt, kind, meth, note) },
                    onDeleteEntry = { viewModel.deleteLabourEntry(it) }
                )
            }
            LedgerTab.CUSTOM_FACTORY -> {
                GenericLedgerTab(
                    ledgerType = "CUSTOM_FACTORY",
                    title = if (isUr) "فیکٹری لیبر جاب ورک" else "Factory per Labour (Job Work)",
                    parties = listOf("Alhmad Fan Job Work", "Hazma Solar Specialized", "Royal Fan Stamping"),
                    entries = customEntries,
                    selectedParty = selectedPartyForDetail,
                    isUrdu = isUr,
                    onSelectParty = { selectedPartyForDetail = it },
                    onAddEntry = { showAddEntryDialog = true },
                    onDeleteEntry = { viewModel.deleteLedgerEntry(it) },
                    onUpdateCheque = { id, st -> viewModel.updateChequeStatus(id, st) }
                )
            }
        }
    }

    // Add Factory Dialog
    if (showAddFactoryDialog) {
        AddFactoryDialog(
            isUrdu = isUr,
            onDismiss = { showAddFactoryDialog = false },
            onSave = { name, loc, phone ->
                viewModel.saveFactory(name, loc, phone)
                showAddFactoryDialog = false
            }
        )
    }

    // Add Worker Dialog
    if (showAddWorkerDialog) {
        AddWorkerDialog(
            isUrdu = isUr,
            onDismiss = { showAddWorkerDialog = false },
            onSave = { name, workType, rateType, rate ->
                viewModel.addWorker(name, workType, rateType, rate)
                showAddWorkerDialog = false
            }
        )
    }

    // Bulk Attendance Dialog
    if (showBulkAttendanceDialog) {
        BulkAttendanceDialog(
            workers = workers,
            isUrdu = isUr,
            onDismiss = { showBulkAttendanceDialog = false },
            onSaveAttendance = { results ->
                results.forEach { (name, status) ->
                    viewModel.markWorkerAttendance(name, status)
                }
                showBulkAttendanceDialog = false
            }
        )
    }

    // Add Ledger Entry Dialog
    if (showAddEntryDialog) {
        val ledgerTypeStr = when (selectedTab) {
            LedgerTab.CUSTOMER_FACTORIES -> "CUSTOMER"
            LedgerTab.PAINT -> "PAINT"
            LedgerTab.RAW_MATERIAL -> "RAW_MATERIAL"
            LedgerTab.SCRAP -> "SCRAP"
            LedgerTab.CUSTOM_FACTORY -> "CUSTOM_FACTORY"
            else -> "CUSTOMER"
        }
        val partyName = selectedPartyForDetail ?: "General Party"

        AddLedgerEntryDialog(
            ledgerType = ledgerTypeStr,
            partyName = partyName,
            isUrdu = isUr,
            onDismiss = { showAddEntryDialog = false },
            onSave = { entry ->
                viewModel.addLedgerEntry(entry)
                showAddEntryDialog = false
            }
        )
    }
}

// ---------------- SUB TABS ----------------

@Composable
fun CustomerFactoriesTab(
    factories: List<FactoryEntity>,
    entries: List<LedgerEntryEntity>,
    selectedParty: String?,
    isUrdu: Boolean = false,
    onSelectParty: (String?) -> Unit,
    onAddFactory: () -> Unit,
    onAddEntry: () -> Unit,
    onDeleteEntry: (String) -> Unit,
    onUpdateCheque: (String, String) -> Unit
) {
    if (selectedParty == null) {
        // Factory Cards Grid
        PosSectionHeader(
            title = if (isUrdu) "کسٹمر فیکٹریاں" else "Customer Factories",
            subtitle = if (isUrdu) "کل بیلنس اور رابطے (کھاتہ کھولنے کے لیے ٹیپ کریں)" else "Tap a factory to open detailed customer ledger",
            actionButton = {
                Button(
                    onClick = onAddFactory,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color(0xFF1C1F22)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isUrdu) "فیکٹری شامل کریں" else "Add Factory")
                }
            }
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(factories) { f ->
                val fEntries = entries.filter { it.partyName == f.name }
                val balance = fEntries.sumOf { it.debit - it.credit }
                val isOwed = balance > 0

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectParty(f.name) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = f.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "${f.location} · ${f.contact}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatCurrency(Math.abs(balance), isUrdu),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isOwed) PosRed else PosGreen
                                )
                            )
                            val statusLabel = if (balance == 0.0) {
                                if (isUrdu) "بے باق" else "Settled"
                            } else if (isOwed) {
                                if (isUrdu) "واجب الادا" else "Owed (Due)"
                            } else {
                                if (isUrdu) "پیشگی" else "Advance"
                            }
                            Text(
                                text = statusLabel,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Detailed Party Ledger
        PartyLedgerDetail(
            partyName = selectedParty,
            entries = entries.filter { it.partyName == selectedParty },
            isUrdu = isUrdu,
            onBack = { onSelectParty(null) },
            onAddEntry = onAddEntry,
            onDeleteEntry = onDeleteEntry,
            onUpdateCheque = onUpdateCheque
        )
    }
}

@Composable
fun GenericLedgerTab(
    ledgerType: String,
    title: String,
    parties: List<String>,
    entries: List<LedgerEntryEntity>,
    selectedParty: String?,
    isUrdu: Boolean = false,
    onSelectParty: (String?) -> Unit,
    onAddEntry: () -> Unit,
    onDeleteEntry: (String) -> Unit,
    onUpdateCheque: (String, String) -> Unit
) {
    if (selectedParty == null) {
        PosSectionHeader(
            title = title,
            subtitle = if (isUrdu) "کھاتہ اور تفصیلات دیکھنے کے لیے منتخب کریں" else "Tap an account to inspect statement & entries",
            actionButton = {
                Button(
                    onClick = onAddEntry,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color(0xFF1C1F22)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isUrdu) "اندراج شامل کریں" else "Add Entry")
                }
            }
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(parties) { name ->
                val partyEntries = entries.filter { it.partyName == name }
                val debitSum = partyEntries.sumOf { it.debit }
                val creditSum = partyEntries.sumOf { it.credit }
                val net = if (ledgerType == "PAINT" || ledgerType == "RAW_MATERIAL") creditSum - debitSum else debitSum - creditSum

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectParty(name) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = if (isUrdu) "${partyEntries.size} لیجر اندراجات" else "${partyEntries.size} ledger entries",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatCurrency(Math.abs(net), isUrdu),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (net > 0) PosGreen else PosAmber
                                )
                            )
                            val balType = if (isUrdu) (if (net >= 0) "واجب الادا" else "پیشگی") else (if (net >= 0) "Payable" else "Advance")
                            Text(
                                text = if (isUrdu) "بیلنس: $balType" else "Balance: $balType",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    } else {
        PartyLedgerDetail(
            partyName = selectedParty,
            entries = entries.filter { it.partyName == selectedParty },
            isUrdu = isUrdu,
            onBack = { onSelectParty(null) },
            onAddEntry = onAddEntry,
            onDeleteEntry = onDeleteEntry,
            onUpdateCheque = onUpdateCheque
        )
    }
}

@Composable
fun PartyLedgerDetail(
    partyName: String,
    entries: List<LedgerEntryEntity>,
    isUrdu: Boolean = false,
    onBack: () -> Unit,
    onAddEntry: () -> Unit,
    onDeleteEntry: (String) -> Unit,
    onUpdateCheque: (String, String) -> Unit
) {
    var showStatementDialog by remember { mutableStateOf(false) }
    val totalDebit = entries.sumOf { it.debit }
    val totalCredit = entries.sumOf { it.credit }
    val balance = totalDebit - totalCredit

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Back Navigation Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = partyName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { showStatementDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = "Print Statement", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isUrdu) "کھاتہ پرنٹ" else "Statement", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAddEntry,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color(0xFF1C1F22)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isUrdu) "نیا اندراج" else "Entry")
                }
            }
        }

        // Summary KPI Banner
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(if (isUrdu) "کل بل (ڈیبٹ)" else "Total Billed (Debit)", fontSize = 10.sp, color = Color.Gray)
                    Text(formatCurrency(totalDebit, isUrdu), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column {
                    Text(if (isUrdu) "کل وصول (کریڈٹ)" else "Total Received (Credit)", fontSize = 10.sp, color = Color.Gray)
                    Text(formatCurrency(totalCredit, isUrdu), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PosGreen)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (isUrdu) "خالص بیلنس" else "Net Balance", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        formatCurrency(Math.abs(balance), isUrdu),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = if (balance > 0) PosRed else PosGreen
                    )
                }
            }
        }

        // Entries List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries) { entry ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(entry.date, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                            Text(
                                if (entry.debit > 0) "+${formatCurrency(entry.debit, isUrdu)}" else "-${formatCurrency(entry.credit, isUrdu)}",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (entry.debit > 0) PosAmber else PosGreen,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(entry.desc, style = MaterialTheme.typography.bodyMedium)

                        if (entry.method.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val methodDisplay = when (entry.method) {
                                    "Cash" -> if (isUrdu) "نقد" else "Cash"
                                    "Online" -> if (isUrdu) "آن لائن" else "Online"
                                    "Bank" -> if (isUrdu) "بینک" else "Bank"
                                    "Cheque" -> if (isUrdu) "چیک" else "Cheque"
                                    else -> entry.method
                                }
                                Text(
                                    "$methodDisplay ${if (entry.detail.isNotBlank()) "· " + entry.detail else ""}",
                                    fontSize = 10.5.sp,
                                    color = Color.Gray
                                )

                                // Cheque status actions if applicable
                                if (entry.method == "Cheque" && entry.chequeStatus.isNotBlank()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TextButton(onClick = { onUpdateCheque(entry.id, "cleared") }) {
                                            Text(if (isUrdu) "کلیئر" else "Clear", fontSize = 10.sp, color = PosGreen)
                                        }
                                        TextButton(onClick = { onUpdateCheque(entry.id, "bounced") }) {
                                            Text(if (isUrdu) "باؤنس" else "Bounce", fontSize = 10.sp, color = PosRed)
                                        }
                                    }
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { onDeleteEntry(entry.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PosRed, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStatementDialog) {
        PartyStatementExportDialog(
            partyName = partyName,
            entries = entries,
            totalDebit = totalDebit,
            totalCredit = totalCredit,
            balance = balance,
            isUrdu = isUrdu,
            onDismiss = { showStatementDialog = false }
        )
    }
}

@Composable
fun LabourLedgerTab(
    workers: List<WorkerEntity>,
    entries: List<LabourEntryEntity>,
    selectedWorker: String?,
    isUrdu: Boolean = false,
    onSelectWorker: (String?) -> Unit,
    onAddWorker: () -> Unit,
    onBulkAttendance: () -> Unit,
    onMarkAttendance: (name: String, status: String, units: Double, note: String) -> Unit,
    onRecordPayment: (name: String, amount: Double, kind: String, method: String, note: String) -> Unit,
    onDeleteEntry: (String) -> Unit
) {
    if (selectedWorker == null) {
        PosSectionHeader(
            title = if (isUrdu) "مزدور فہرست اور حاضری" else "Labour Workers & Attendance",
            subtitle = if (isUrdu) "روزانہ اجرت، پیس ریٹ اور حاضری کا انتظام" else "Manage worker wages, attendance, and advances",
            actionButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = onBulkAttendance, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(if (isUrdu) "حاضری لگائیں" else "Bulk Attendance", fontSize = 11.sp)
                    }
                    Button(
                        onClick = onAddWorker,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color(0xFF1C1F22)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(if (isUrdu) "مزدور شامل کریں" else "+ Worker", fontSize = 11.sp)
                    }
                }
            }
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(workers) { w ->
                val wEntries = entries.filter { it.workerName == w.name }
                val earned = wEntries.sumOf { it.credit }
                val paid = wEntries.sumOf { it.debit }
                val dues = earned - paid

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectWorker(w.name) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = w.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "${w.workType} · ${formatCurrency(w.rate, isUrdu)} / ${if (isUrdu && w.rateType == "daily") "روزانہ" else w.rateType}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatCurrency(Math.abs(dues), isUrdu),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (dues > 0) PosAmber else PosGreen
                                )
                            )
                            val dueLabel = if (isUrdu) (if (dues >= 0) "واجب الادا" else "بے باق") else (if (dues >= 0) "Payable" else "Settled")
                            Text(text = if (isUrdu) "واجبات: $dueLabel" else "Dues: $dueLabel", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    } else {
        // Individual Worker Detail
        val worker = workers.find { it.name == selectedWorker }
        val wEntries = entries.filter { it.workerName == selectedWorker }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onSelectWorker(null) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(text = selectedWorker, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { onMarkAttendance(selectedWorker, "present", 0.0, "") },
                        colors = ButtonDefaults.buttonColors(containerColor = PosGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(if (isUrdu) "حاضر" else "Present", fontSize = 10.5.sp)
                    }
                }
            }

            // Entries List
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(wEntries) { entry ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(entry.date, fontSize = 11.sp, color = Color.Gray)
                                val kindDisplay = when (entry.kind.lowercase()) {
                                    "attendance" -> if (isUrdu) "حاضری" else "Attendance"
                                    "wage" -> if (isUrdu) "اجرت" else "Wage"
                                    "advance" -> if (isUrdu) "پیشگی" else "Advance"
                                    else -> entry.kind.uppercase()
                                }
                                Text(
                                    text = "$kindDisplay: ${entry.status} ${if (entry.note.isNotBlank()) "· " + entry.note else ""}",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                if (entry.credit > 0) "+${formatCurrency(entry.credit, isUrdu)}" else "-${formatCurrency(entry.debit, isUrdu)}",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (entry.credit > 0) PosGreen else PosAmber
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- DIALOGS ----------------

@Composable
fun AddFactoryDialog(
    isUrdu: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (name: String, location: String, contact: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrdu) "نئی فیکٹری شامل کریں" else "Add New Factory") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(if (isUrdu) "فیکٹری کا نام" else "Factory Name") })
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text(if (isUrdu) "مقام / پتہ" else "Location") })
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text(if (isUrdu) "رابطہ فون نمبر" else "Phone Contact") })
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onSave(name, location, contact) }) {
                Text(AppStrings.save(isUrdu))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.cancel(isUrdu)) }
        }
    )
}

@Composable
fun AddWorkerDialog(
    isUrdu: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (name: String, workType: String, rateType: String, rate: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var workType by remember { mutableStateOf("Welding") }
    var rateType by remember { mutableStateOf("daily") }
    var rateStr by remember { mutableStateOf("1200") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrdu) "مزدور شامل کریں" else "Add Labour Worker") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(if (isUrdu) "مزدور کا نام" else "Worker Name") })
                OutlinedTextField(value = workType, onValueChange = { workType = it }, label = { Text(if (isUrdu) "کام کی قسم (جیسے ویلڈنگ)" else "Work Type") })
                OutlinedTextField(value = rateStr, onValueChange = { rateStr = it }, label = { Text(if (isUrdu) "اجرت / ریٹ (روپے)" else "Rate (Rs)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val rate = rateStr.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) onSave(name, workType, rateType, rate)
            }) { Text(AppStrings.save(isUrdu)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(AppStrings.cancel(isUrdu)) } }
    )
}

@Composable
fun BulkAttendanceDialog(
    workers: List<WorkerEntity>,
    isUrdu: Boolean = false,
    onDismiss: () -> Unit,
    onSaveAttendance: (Map<String, String>) -> Unit
) {
    val statusMap = remember { mutableStateMapOf<String, String>().apply { workers.forEach { put(it.name, "present") } } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrdu) "روزانہ حاضری لگائیں" else "Mark Daily Attendance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 300.dp)) {
                workers.forEach { w ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(w.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val statuses = if (isUrdu) listOf("present" to "حاضر", "half" to "آدھا", "absent" to "غیر حاضر")
                            else listOf("present" to "P", "half" to "½", "absent" to "A")
                            statuses.forEach { (st, label) ->
                                val sel = statusMap[w.name] == st
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (sel) PosGreen else Color.DarkGray,
                                    modifier = Modifier.clickable { statusMap[w.name] = st }
                                ) {
                                    Text(label, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSaveAttendance(statusMap.toMap()) }) {
                Text(if (isUrdu) "حاضری محفوظ کریں" else "Save Attendance")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(AppStrings.cancel(isUrdu)) } }
    )
}

@Composable
fun AddLedgerEntryDialog(
    ledgerType: String,
    partyName: String,
    isUrdu: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (LedgerEntryEntity) -> Unit
) {
    // Mode: "CALCULATOR" (Auto Solve Math) vs "MANUAL" (Direct Debit/Credit)
    var isCalculatorMode by remember { mutableStateOf(true) }

    // Transaction Type: "BILL" (Goods In / Service Done) vs "PAYMENT" (Money Settlement)
    var transactionType by remember {
        mutableStateOf(if (ledgerType == "SCRAP") "SALE" else "BILL")
    }

    // Common Math Fields
    var quantityStr by remember { mutableStateOf("") }
    var rateStr by remember { mutableStateOf("") }
    var deductionStr by remember { mutableStateOf("0") }
    var paidOrReceivedStr by remember { mutableStateOf("0") }
    var noteStr by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("Cash") }
    var detail by remember { mutableStateOf("") }

    // Direct Manual Override Fields
    var manualDesc by remember { mutableStateOf("") }
    var manualDebitStr by remember { mutableStateOf("") }
    var manualCreditStr by remember { mutableStateOf("") }

    // Mathematical Auto Calculations
    val qty = quantityStr.toDoubleOrNull() ?: 0.0
    val rate = rateStr.toDoubleOrNull() ?: 0.0
    val deduction = deductionStr.toDoubleOrNull() ?: 0.0
    val paidOrReceived = paidOrReceivedStr.toDoubleOrNull() ?: 0.0

    // Effective Quantity after Tare/Wastage/Defects
    val effectiveQty = (qty - deduction).coerceAtLeast(0.0)
    // Gross Valuation = Effective Qty × Rate
    val grossValuation = effectiveQty * rate
    // Balance Outstanding = Gross Valuation - Paid/Received
    val balanceOutstanding = (grossValuation - paidOrReceived).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isUrdu) "اندراج شامل کریں — $partyName (خودکار حسابی فارمولا)"
                else "Add Entry — $partyName (Auto-Solve Math)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mode Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isUrdu) "ریاضیاتی فارمولا کیلکولیٹر" else "Mathematical Auto-Calculator",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(checked = isCalculatorMode, onCheckedChange = { isCalculatorMode = it })
                }

                if (isCalculatorMode) {
                    // Type Selector Tabs (Bill vs Payment)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isBill = transactionType in listOf("BILL", "SALE")
                        val billLabel = when (ledgerType) {
                            "RAW_MATERIAL" -> if (isUrdu) "خام مال بل / وصولی" else "Material Received (Bill)"
                            "PAINT" -> if (isUrdu) "رنگ شدہ فین راڈز" else "Painted Rods (Bill)"
                            "SCRAP" -> if (isUrdu) "سکریپ فروخت" else "Scrap Sale (Billed)"
                            else -> if (isUrdu) "مال سپلائی / بل" else "Goods Delivered (Bill)"
                        }
                        val paymentLabel = when (ledgerType) {
                            "SCRAP" -> if (isUrdu) "سکریپ کی رقم وصولی" else "Payment Received"
                            "CUSTOMER" -> if (isUrdu) "گاہک سے وصولی" else "Customer Payment"
                            else -> if (isUrdu) "ادائیگی رقم" else "Payment Sent"
                        }

                        FilterChip(
                            selected = isBill,
                            onClick = { transactionType = if (ledgerType == "SCRAP") "SALE" else "BILL" },
                            label = { Text(billLabel, fontSize = 10.5.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isBill,
                            onClick = { transactionType = "PAYMENT" },
                            label = { Text(paymentLabel, fontSize = 10.5.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (transactionType in listOf("BILL", "SALE")) {
                        // Dynamic Units/Labels depending on Ledger Type
                        val qtyLabel = when (ledgerType) {
                            "RAW_MATERIAL" -> if (isUrdu) "کل وزن (کلوگرام)" else "Gross Weight (kg)"
                            "PAINT" -> if (isUrdu) "راڈز کی تعداد" else "Rods Count (pcs)"
                            "SCRAP" -> if (isUrdu) "سکریپ وزن (کلو)" else "Scrap Weight (kg)"
                            else -> if (isUrdu) "تعداد / یونٹس" else "Quantity (pcs)"
                        }
                        val rateLabel = when (ledgerType) {
                            "RAW_MATERIAL" -> if (isUrdu) "ریٹ فی کلو (روپے)" else "Rate/kg (Rs)"
                            "PAINT" -> if (isUrdu) "ریٹ فی راڈ (روپے)" else "Rate/Rod (Rs)"
                            "SCRAP" -> if (isUrdu) "ریٹ فی کلو (روپے)" else "Rate/kg (Rs)"
                            else -> if (isUrdu) "ریٹ فی پیس (روپے)" else "Rate/pc (Rs)"
                        }
                        val dedLabel = when (ledgerType) {
                            "RAW_MATERIAL" -> if (isUrdu) "کٹوتی / باردانہ وزن (کلو)" else "Tare / Wastage (kg)"
                            "PAINT" -> if (isUrdu) "نقص / ریجیکٹڈ کٹوتی (روپے)" else "Defect Deduction (Rs)"
                            "SCRAP" -> if (isUrdu) "کٹوتی / بوری وزن (کلو)" else "Tare / Bag (kg)"
                            else -> if (isUrdu) "رعایت / ڈسکاؤنٹ (روپے)" else "Discount (Rs)"
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = quantityStr,
                                onValueChange = { quantityStr = it },
                                label = { Text(qtyLabel, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = rateStr,
                                onValueChange = { rateStr = it },
                                label = { Text(rateLabel, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = deductionStr,
                                onValueChange = { deductionStr = it },
                                label = { Text(dedLabel, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = paidOrReceivedStr,
                                onValueChange = { paidOrReceivedStr = it },
                                label = {
                                    Text(
                                        if (ledgerType in listOf("SCRAP", "CUSTOMER"))
                                            (if (isUrdu) "موقع پر وصول نقد" else "Cash Received Now")
                                        else
                                            (if (isUrdu) "موقع پر ادا نقد" else "Cash Paid Now"),
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Real-time Mathematical Equation Card
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    if (isUrdu) "خودکار حسابی تفصیل:" else "Live Auto-Calculation:",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = if (deduction > 0)
                                        "($quantityStr - $deductionStr) × Rs $rateStr = Rs ${grossValuation.toInt()}"
                                    else
                                        "$quantityStr × Rs $rateStr = Rs ${grossValuation.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (paidOrReceived > 0) {
                                    Text(
                                        text = "Less Paid/Received Rs ${paidOrReceived.toInt()} => Remaining Rs ${balanceOutstanding.toInt()}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = PosGreen
                                    )
                                }
                            }
                        }
                    } else {
                        // Payment Entry
                        OutlinedTextField(
                            value = paidOrReceivedStr,
                            onValueChange = { paidOrReceivedStr = it },
                            label = { Text(if (isUrdu) "ادا شدہ / وصول شدہ رقم (روپے)" else "Amount Paid / Received (Rs)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Payment Methods
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val methods = listOf("Cash", "Online", "Bank", "Cheque")
                            methods.forEach { m ->
                                val sel = m == method
                                FilterChip(
                                    selected = sel,
                                    onClick = { method = m },
                                    label = { Text(m, fontSize = 11.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = detail,
                            onValueChange = { detail = it },
                            label = { Text(if (isUrdu) "چیک نمبر / بینک / رسید تفصیل" else "Cheque # / Bank / Receipt Detail") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = noteStr,
                        onValueChange = { noteStr = it },
                        label = { Text(if (isUrdu) "اضافی نوٹ / بل نمبر" else "Extra Note / Bill #") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Manual Fallback
                    OutlinedTextField(
                        value = manualDesc,
                        onValueChange = { manualDesc = it },
                        label = { Text(if (isUrdu) "تفصیل / وجہ" else "Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = manualDebitStr,
                            onValueChange = { manualDebitStr = it },
                            label = { Text(if (isUrdu) "ڈیبٹ (بل / ادا شدہ)" else "Debit") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = manualCreditStr,
                            onValueChange = { manualCreditStr = it },
                            label = { Text(if (isUrdu) "کریڈٹ (وصول شدہ)" else "Credit") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isCalculatorMode) {
                        val isBill = transactionType in listOf("BILL", "SALE")
                        val finalDebit: Double
                        val finalCredit: Double
                        val finalDesc: String
                        val weightInVal = if (ledgerType == "RAW_MATERIAL") effectiveQty else null
                        val itemsInVal = if (ledgerType == "PAINT" || ledgerType == "CUSTOMER") effectiveQty else null

                        if (isBill) {
                            when (ledgerType) {
                                "RAW_MATERIAL" -> {
                                    // Supplier Credit (We owe supplier the gross valuation, minus whatever paid at gate)
                                    finalCredit = grossValuation
                                    finalDebit = paidOrReceived
                                    finalDesc = "${if (isUrdu) "خام مال وصول" else "Raw Material In"}: $effectiveQty kg @ Rs $rate/kg" +
                                            if (noteStr.isNotBlank()) " ($noteStr)" else ""
                                }
                                "PAINT" -> {
                                    // Painter Credit (We owe painter for painting work)
                                    finalCredit = grossValuation
                                    finalDebit = paidOrReceived
                                    finalDesc = "${if (isUrdu) "رنگ شدہ راڈز" else "Painted Fan Rods"}: $effectiveQty pcs @ Rs $rate/pc" +
                                            if (noteStr.isNotBlank()) " ($noteStr)" else ""
                                }
                                "SCRAP" -> {
                                    // Scrap Dealer Debit (Dealer owes us for scrap taken)
                                    finalDebit = grossValuation
                                    finalCredit = paidOrReceived
                                    finalDesc = "${if (isUrdu) "سکریپ فروخت" else "Scrap Sold"}: $effectiveQty kg @ Rs $rate/kg" +
                                            if (noteStr.isNotBlank()) " ($noteStr)" else ""
                                }
                                else -> {
                                    // Customer Debit (Customer owes us for goods)
                                    finalDebit = grossValuation
                                    finalCredit = paidOrReceived
                                    finalDesc = "${if (isUrdu) "سامان ترسیل" else "Goods Delivered"}: $effectiveQty pcs @ Rs $rate" +
                                            if (noteStr.isNotBlank()) " ($noteStr)" else ""
                                }
                            }
                        } else {
                            // Payment Mode
                            when (ledgerType) {
                                "SCRAP", "CUSTOMER" -> {
                                    // Received from Scrap Dealer or Customer (Credit to their ledger)
                                    finalDebit = 0.0
                                    finalCredit = paidOrReceived
                                    finalDesc = "${if (isUrdu) "وصولی بذریعہ" else "Payment received via"} $method" +
                                            if (noteStr.isNotBlank()) " ($noteStr)" else ""
                                }
                                else -> {
                                    // Paid to Raw Material supplier or Painter (Debit to their ledger)
                                    finalDebit = paidOrReceived
                                    finalCredit = 0.0
                                    finalDesc = "${if (isUrdu) "ادائیگی بذریعہ" else "Payment made via"} $method" +
                                            if (noteStr.isNotBlank()) " ($noteStr)" else ""
                                }
                            }
                        }

                        if (finalDebit > 0 || finalCredit > 0) {
                            val entry = LedgerEntryEntity(
                                id = "${ledgerType.lowercase()}_${System.currentTimeMillis()}",
                                ledgerType = ledgerType,
                                partyName = partyName,
                                date = PosViewModel.getCurrentDateString(),
                                time = PosViewModel.getCurrentTimeString(),
                                desc = finalDesc,
                                debit = finalDebit,
                                credit = finalCredit,
                                method = method,
                                detail = detail,
                                weightIn = weightInVal,
                                itemsIn = itemsInVal
                            )
                            onSave(entry)
                        }
                    } else {
                        // Manual Mode Save
                        val debit = manualDebitStr.toDoubleOrNull() ?: 0.0
                        val credit = manualCreditStr.toDoubleOrNull() ?: 0.0
                        if (manualDesc.isNotBlank() && (debit > 0 || credit > 0)) {
                            val entry = LedgerEntryEntity(
                                id = "${ledgerType.lowercase()}_${System.currentTimeMillis()}",
                                ledgerType = ledgerType,
                                partyName = partyName,
                                date = PosViewModel.getCurrentDateString(),
                                time = PosViewModel.getCurrentTimeString(),
                                desc = manualDesc,
                                debit = debit,
                                credit = credit,
                                method = "Cash",
                                detail = ""
                            )
                            onSave(entry)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color(0xFF1C1F22)
                )
            ) {
                Text(AppStrings.save(isUrdu))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.cancel(isUrdu)) }
        }
    )
}

@Composable
fun PartyStatementExportDialog(
    partyName: String,
    entries: List<LedgerEntryEntity>,
    totalDebit: Double,
    totalCredit: Double,
    balance: Double,
    isUrdu: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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
                    text = if (isUrdu) "آفیشل کھاتہ اسٹیٹمنٹ" else "OFFICIAL ACCOUNT STATEMENT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Party Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (isUrdu) "کھاتے دار کا نام:" else "Account Name:", fontSize = 11.sp, color = Color.Gray)
                        Text(partyName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (isUrdu) "کل اندراجات:" else "Total Entries:", fontSize = 11.sp, color = Color.Gray)
                        Text("${entries.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // Balance summary card
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(if (isUrdu) "کل بل (ڈیبٹ)" else "Debit (+)", fontSize = 10.sp, color = Color.Gray)
                            Text(formatCurrency(totalDebit, isUrdu), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(if (isUrdu) "کل وصول (کریڈٹ)" else "Credit (-)", fontSize = 10.sp, color = Color.Gray)
                            Text(formatCurrency(totalCredit, isUrdu), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PosGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (isUrdu) "خالص بیلنس" else "Net Due", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                formatCurrency(Math.abs(balance), isUrdu),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = if (balance > 0) PosRed else PosGreen
                            )
                        }
                    }
                }

                // Recent entries list
                Text(
                    if (isUrdu) "حالیہ ٹرانزیکشنز:" else "Recent Transactions:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(entries.take(8)) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.date, fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                Text(entry.desc, fontSize = 11.sp, maxLines = 1)
                            }
                            Text(
                                text = if (entry.debit > 0) "+${formatCurrency(entry.debit, isUrdu)}" else "-${formatCurrency(entry.credit, isUrdu)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (entry.debit > 0) PosAmber else PosGreen
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entriesFormatted = entries.take(15).joinToString("\n") { e ->
                        val amt = if (e.debit > 0) "+Rs ${e.debit.toInt()}" else "-Rs ${e.credit.toInt()}"
                        "• ${e.date}: ${e.desc} -> $amt"
                    }
                    val exportText = """
                        *FALCON ROD MAKER*
                        Ceiling Fan Accessories · Gujrat, Pakistan
                        --------------------------------
                        *ACCOUNT STATEMENT*
                        *Party:* $partyName
                        --------------------------------
                        *Total Billed (Debit):* Rs ${totalDebit.toInt()}
                        *Total Paid (Credit):* Rs ${totalCredit.toInt()}
                        *Net Balance Due:* Rs ${balance.toInt()}
                        --------------------------------
                        *Statement Entries:*
                        $entriesFormatted
                        --------------------------------
                        Generated via Falcon Rod Maker POS
                    """.trimIndent()
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, exportText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share / Print Statement"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color(0xFF1C1F22))
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isUrdu) "کھاتہ شیئر / پرنٹ کریں" else "Share / Print Statement")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.close(isUrdu))
            }
        }
    )
}
