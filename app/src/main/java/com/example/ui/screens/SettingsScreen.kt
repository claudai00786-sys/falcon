package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sync.SyncState
import com.example.ui.components.AppBrandLogo
import com.example.ui.components.OfflineSyncStatusCard
import com.example.ui.components.PosSectionHeader
import com.example.ui.components.PosStatCard
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.viewmodel.PosViewModel

@Composable
fun SettingsScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isUrdu = currentLanguage == "ur"
    val activePreset by viewModel.currentThemePreset.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val totalPendingCount by viewModel.totalPendingCount.collectAsState()
    val pendingSyncQueue by viewModel.pendingSyncQueue.collectAsState()
    val lastSyncedAt by viewModel.lastSyncedAt.collectAsState()

    var showChangePinDialog by remember { mutableStateOf(false) }
    var showSecurityQuestionDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        // Section: End-to-End Encryption & Security
        item {
            PosSectionHeader(
                title = if (isUrdu) "سیکیورٹی اور لاک" else "Security & End-to-End Encryption",
                subtitle = if (isUrdu) "حساس ریکارڈز کے لیے ہارڈویئر محفوظ AES-256 خفیہ کاری" else "Hardware-backed AES-256-GCM cipher protection for sensitive records"
            )

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PosGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(if (isUrdu) "AES-256-GCM خفیہ کاری" else "AES-256-GCM Encryption", fontWeight = FontWeight.Bold)
                                Text(if (isUrdu) "تمام ٹرانزیکشن ریکارڈز خفیہ کاری کے تحت محفوظ ہیں" else "All transaction payload records encrypted at rest", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = PosGreen.copy(alpha = 0.15f)) {
                            Text(if (isUrdu) "فعال" else "ACTIVE", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = PosGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isUrdu) "پی او ایس لاک پن" else "POS Lock PIN", fontWeight = FontWeight.SemiBold)
                            Text(if (isUrdu) "کیشیئر انلاک کے لیے 4 ہندسوں کا سیکیورٹی کوڈ" else "4-digit security code for cashier unlock", fontSize = 11.sp, color = Color.Gray)
                        }
                        Button(
                            onClick = { showChangePinDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (isUrdu) "پن تبدیل کریں" else "Change PIN", fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isUrdu) "سیکیورٹی سوال سے بحالی" else "Security Question Recovery", fontWeight = FontWeight.SemiBold)
                            Text(if (isUrdu) "پن بھول جانے پر کلاؤڈ کے بغیر ری سیٹ کریں" else "Reset PIN if forgotten without cloud access", fontSize = 11.sp, color = Color.Gray)
                        }
                        OutlinedButton(
                            onClick = { showSecurityQuestionDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (isUrdu) "سوال بدلیں" else "Edit Question", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Section: Cloud Data Synchronization
        item {
            PosSectionHeader(
                title = if (isUrdu) "مرکزی کلاؤڈ سنک" else "Central Cloud Database Sync",
                subtitle = if (isUrdu) "انٹرنیٹ آنے پر خودکار سنک کی سہولت" else "Offline-first sync engine with auto-reconnect trigger"
            )

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isUrdu) "مرکزی کلاؤڈ سنک" else "Central Cloud Sync", fontWeight = FontWeight.Bold)
                            Text(if (isUrdu) "انٹرنیٹ کنکشن بحال ہوتے ہی ٹرانزیکشنز خودکار سنک ہو جاتی ہیں" else "Auto-syncs offline transactions once connection is active", fontSize = 11.sp, color = Color.Gray)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (isUrdu) "فعال" else "ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    val (syncLabel, syncTime, syncColor) = when (val s = syncState) {
                        is SyncState.Syncing -> Triple(
                            if (isUrdu) "سنک ہو رہا ہے…" else "Syncing…",
                            if (isUrdu) "اپ لوڈ جاری ہے" else "Uploading payload",
                            PosAmber
                        )
                        is SyncState.Synced -> Triple(
                            if (isUrdu) "اپ ٹو ڈیٹ" else "Up to Date",
                            s.timeString,
                            PosGreen
                        )
                        is SyncState.LocalSaved -> Triple(
                            if (isUrdu) "مقامی طور پر محفوظ" else "Saved Locally",
                            s.message,
                            PosSkyBlue
                        )
                        is SyncState.Error -> Triple(
                            if (isUrdu) "آف لائن موڈ" else "Offline Mode",
                            s.message,
                            PosOrange
                        )
                        SyncState.Idle -> Triple(
                            if (isUrdu) "تیار" else "Ready",
                            if (isUrdu) "اسٹینڈ بائی" else "Standby",
                            PosGreen
                        )
                    }

                    // Detailed Offline Sync Status Component
                    OfflineSyncStatusCard(
                        syncState = syncState,
                        isOnline = isOnline,
                        pendingCount = totalPendingCount,
                        pendingItems = pendingSyncQueue,
                        lastSyncedTime = lastSyncedAt.ifEmpty { null },
                        currentLanguage = currentLanguage,
                        onSyncClick = { viewModel.manualSync() }
                    )
                }
            }
        }

        // Section: Appearance & Themes
        item {
            PosSectionHeader(
                title = if (isUrdu) "تھیم اور رنگ" else "Color Theme Presets",
                subtitle = if (isUrdu) "پی او ایس انٹرفیس کے رنگ اپنی مرضی کے مطابق منتخب کریں" else "Customize the look and feel of the POS interface"
            )

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val presets: List<Pair<ColorThemePreset, String>> = listOf(
                        ColorThemePreset.AMBER to (if (isUrdu) "فالکن امبر (گولڈ)" else "Falcon Amber (Gold)"),
                        ColorThemePreset.EMERALD to (if (isUrdu) "زمرد انڈسٹریل (سبز)" else "Emerald Industrial"),
                        ColorThemePreset.OCEAN to (if (isUrdu) "اوشین اسکائی (نیلا)" else "Ocean Sky Cyan"),
                        ColorThemePreset.STEEL to (if (isUrdu) "مونوکروم اسٹیل" else "Monochrome Steel")
                    )

                    presets.forEach { (preset, label) ->
                        val isSelected = preset == activePreset
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.setThemePreset(preset) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(preset.primary)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                            RadioButton(selected = isSelected, onClick = { viewModel.setThemePreset(preset) })
                        }
                    }
                }
            }
        }

        // Section: Language
        item {
            PosSectionHeader(
                title = if (isUrdu) "زبان کا انتخاب" else "Language & Localization",
                subtitle = if (isUrdu) "اردو اور انگریزی سپورٹ" else "Urdu and English support"
            )

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.setLanguage("en") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentLanguage == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (currentLanguage == "en") Color(0xFF1C1F22) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("English", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.setLanguage("ur") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentLanguage == "ur") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (currentLanguage == "ur") Color(0xFF1C1F22) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("اردو (Urdu)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: App Metadata
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AppBrandLogo(modifier = Modifier.size(52.dp), showBorder = true)
                    Column {
                        Text(if (isUrdu) "فالکن راڈ میکر پی او ایس" else "Falcon Rod Maker POS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(if (isUrdu) "پنکھا پرزہ جات · گجرات، پاکستان" else "Fan Accessories · Gujrat, Pakistan", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isUrdu) "آف لائن کام، خفیہ ٹرانزیکشنز اور محفوظ روم لیجر ڈیٹا بیس کی سہولت کے ساتھ بنایا گیا ہے۔"
                            else "Built for offline-first resilience, E2E encrypted transactions, and local Room ledger storage.",
                            fontSize = 10.5.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var currentPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text(if (isUrdu) "سیکیورٹی پن تبدیل کریں" else "Change Security PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = currentPin, onValueChange = { if (it.length <= 4) currentPin = it }, label = { Text(if (isUrdu) "موجودہ 4 ہندسوں کا پن" else "Current 4-digit PIN") })
                    OutlinedTextField(value = newPin, onValueChange = { if (it.length <= 4) newPin = it }, label = { Text(if (isUrdu) "نیا 4 ہندسوں کا پن" else "New 4-digit PIN") })
                    OutlinedTextField(value = confirmPin, onValueChange = { if (it.length <= 4) confirmPin = it }, label = { Text(if (isUrdu) "نئے پن کی تصدیق کریں" else "Confirm New PIN") })
                    if (errorMsg != null) {
                        Text(errorMsg!!, color = PosRed, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newPin.length == 4 && newPin == confirmPin) {
                        val ok = viewModel.changePin(currentPin, newPin)
                        if (ok) {
                            showChangePinDialog = false
                        } else {
                            errorMsg = if (isUrdu) "موجودہ پن غلط ہے۔" else "Current PIN is incorrect."
                        }
                    } else {
                        errorMsg = if (isUrdu) "نیا پن 4 ہندسوں کا ہونا چاہیے اور دونوں مماثل ہوں۔" else "New PIN must be 4 digits and match."
                    }
                }) { Text(if (isUrdu) "پن تبدیل کریں" else "Update PIN") }
            },
            dismissButton = { TextButton(onClick = { showChangePinDialog = false }) { Text(AppStrings.cancel(isUrdu)) } }
        )
    }

    // Security Question Dialog
    if (showSecurityQuestionDialog) {
        var question by remember { mutableStateOf(if (isUrdu) "دکان کے مالک کا نام کیا ہے؟" else "What is the shop owner's name?") }
        var answer by remember { mutableStateOf("amir") }

        AlertDialog(
            onDismissRequest = { showSecurityQuestionDialog = false },
            title = { Text(if (isUrdu) "سیکیورٹی ریکوری سوال" else "Security Recovery Question") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text(if (isUrdu) "سیکیورٹی سوال" else "Security Question") })
                    OutlinedTextField(value = answer, onValueChange = { answer = it }, label = { Text(if (isUrdu) "جواب" else "Answer") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setSecurityQuestionAnswer(answer)
                    showSecurityQuestionDialog = false
                }) { Text(AppStrings.save(isUrdu)) }
            },
            dismissButton = { TextButton(onClick = { showSecurityQuestionDialog = false }) { Text(AppStrings.cancel(isUrdu)) } }
        )
    }
}
