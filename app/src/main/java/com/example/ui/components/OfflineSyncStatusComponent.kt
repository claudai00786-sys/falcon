package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SyncQueueEntity
import com.example.sync.SyncState
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Primary UI component displaying real-time offline sync status,
 * indicating when data is pending transmission to the remote server.
 */
@Composable
fun OfflineSyncStatusBanner(
    syncState: SyncState,
    isOnline: Boolean,
    pendingCount: Int,
    modifier: Modifier = Modifier,
    pendingItems: List<SyncQueueEntity> = emptyList(),
    lastSyncedTime: String? = null,
    currentLanguage: String = "en",
    onSyncClick: () -> Unit = {},
    onTestQueueItem: (() -> Unit)? = null,
    onClearQueue: (() -> Unit)? = null
) {
    val isUr = currentLanguage == "ur"
    var isExpanded by remember { mutableStateOf(false) }
    var showDetailsModal by remember { mutableStateOf(false) }

    // Pulsing animation for active sync or warning
    val infiniteTransition = rememberInfiniteTransition(label = "syncPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val isSyncing = syncState is SyncState.Syncing
    val isError = syncState is SyncState.Error
    val hasPending = pendingCount > 0

    // Determine banner color theme and message
    val (bannerBg, bannerBorder, accentColor, iconVector) = when {
        isSyncing -> {
            Quad(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.primary,
                Icons.Default.Sync
            )
        }
        isError -> {
            Quad(
                PosRedLight,
                PosRed.copy(alpha = 0.6f),
                PosRed,
                Icons.Default.SyncProblem
            )
        }
        !isOnline && hasPending -> {
            Quad(
                Color(0x2EE8590C),
                PosOrange.copy(alpha = 0.7f),
                PosOrange,
                Icons.Default.CloudOff
            )
        }
        !isOnline -> {
            Quad(
                Color(0x22F5B700),
                PosAmber.copy(alpha = 0.6f),
                PosAmber,
                Icons.Default.WifiOff
            )
        }
        hasPending -> {
            Quad(
                Color(0x26F5B700),
                PosAmber.copy(alpha = 0.8f),
                PosAmber,
                Icons.Default.CloudUpload
            )
        }
        else -> {
            Quad(
                PosGreenLight,
                PosGreen.copy(alpha = 0.3f),
                PosGreen,
                Icons.Default.CloudDone
            )
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bannerBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, bannerBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_sync_status_banner")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            // Main Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Icon + Status Text
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = if (isSyncing) pulseAlpha * 0.3f else 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = accentColor,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = "Sync Status",
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = when {
                                    isSyncing -> if (isUr) "ریموٹ سرور پر ترسیل جاری ہے…" else "Transmitting to Remote Server…"
                                    isError -> if (isUr) "ترسیل میں خرابی ہوئی" else "Transmission Error"
                                    !isOnline && hasPending -> if (isUr) "آف لائن موڈ — ترسیل کا منتظر" else "Offline Mode · Data Pending"
                                    !isOnline -> if (isUr) "آف لائن موڈ" else "Offline Mode"
                                    hasPending -> if (isUr) "ڈیٹا سرور ترسیل کا منتظر ہے" else "Data Pending Transmission"
                                    else -> if (isUr) "تمام ڈیٹا سرور سے ہم وقت ہے" else "All Data Synchronized"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            // Pending Transmission Badge
                            if (hasPending) {
                                Surface(
                                    shape = RoundedCornerShape(99.dp),
                                    color = accentColor,
                                    modifier = Modifier.testTag("pending_transmission_badge")
                                ) {
                                    Text(
                                        text = if (isUr) "$pendingCount منتظر" else "$pendingCount Pending",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.Black
                                        ),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Subtitle / explanation
                        Text(
                            text = when {
                                isSyncing -> if (isUr) "لوکل روم ڈیٹا بیس سے ریموٹ کلاؤڈ پر منتقل کیا جا رہا ہے…" else "Uploading local Room database records to cloud…"
                                isError -> (syncState as? SyncState.Error)?.message ?: if (isUr) "کنکشن کی جانچ کر کے دوبارہ کوشش کریں" else "Check connection and retry"
                                !isOnline && hasPending -> if (isUr) "ڈیٹا محفوظ ہے۔ انٹرنیٹ بحال ہونے پر خودکار ترسیل ہوگی۔" else "Data is safely saved locally. Will transmit automatically when reconnected."
                                !isOnline -> if (isUr) "انٹرنیٹ منسلک نہیں ہے، تمام اندراجات فون میں محفوظ ہوں گے" else "No internet. All local transactions are safely preserved."
                                hasPending -> if (isUr) "انٹرنیٹ دستیاب ہے، ترسیل کے لیے 'Sync Now' دبائیں۔" else "Ready to upload pending changes to remote server."
                                else -> if (isUr) "آخری بار ہم وقت: ${lastSyncedTime ?: "ابھی"}" else "Last synced: ${lastSyncedTime ?: "Just now"}"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }
                }

                // Right: Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Sync Now Action Button
                    Button(
                        onClick = onSyncClick,
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = if (accentColor == PosAmber || accentColor == PosOrange) Color.Black else Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("banner_sync_now_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSyncing) (if (isUr) "جاری…" else "Syncing…") else (if (isUr) "ابھی سنک کریں" else "Sync Now"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Expand / Details Button
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                            .testTag("banner_expand_details_button")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle Queue Details",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expandable Tray: Detailed Queue & Diagnostics
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(
                        color = bannerBorder.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Meta status chips row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Connection State Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) PosGreen else PosOrange)
                            )
                            Text(
                                text = if (isOnline) (if (isUr) "انٹرنیٹ فعال" else "Internet Connected") else (if (isUr) "انٹرنیٹ منقطع" else "Device Offline"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isOnline) PosGreen else PosOrange
                                )
                            )
                        }

                        // Remote Server Endpoint Chip
                        Text(
                            text = if (isUr) "ریموٹ سرور: گوگل ایپس اسکرپٹ (فعال)" else "Remote: Google Apps Script API",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pending Items List / Notice
                    if (pendingItems.isNotEmpty()) {
                        Text(
                            text = if (isUr) "سرور پر ترسیل کے منتظر ریکارڈز:" else "Pending Transmission Queue (${pendingItems.size}):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // Preview of up to 4 items in the banner
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            pendingItems.take(4).forEach { item ->
                                PendingItemRow(item = item, isUrdu = isUr)
                            }

                            if (pendingItems.size > 4) {
                                Text(
                                    text = if (isUr) "+ مزید ${pendingItems.size - 4} ریکارڈز قطار میں ہیں…" else "+ ${pendingItems.size - 4} more records in queue…",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    } else if (hasPending) {
                        // Pending count reported but queue detail empty
                        Text(
                            text = if (isUr) "$pendingCount آرڈرز لوکل ڈیٹا بیس میں محفوظ ہیں اور ترسیل کا انتظار کر رہے ہیں۔" else "$pendingCount transactions saved locally in Room DB, awaiting cloud sync.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    } else {
                        Text(
                            text = if (isUr) "کوئی ریکارڈ ترسیل کا منتظر نہیں ہے۔ ڈیٹا بالکل ہم وقت ہے۔" else "No pending items. All sales, payments, and stock records are up to date on remote server.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    // Test/Simulation Tool Strip
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onTestQueueItem != null) {
                            TextButton(
                                onClick = onTestQueueItem,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isUr) "ٹیسٹ قطار شامل کریں" else "+ Enqueue Test Item",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                )
                            }
                        }

                        if (onClearQueue != null && hasPending) {
                            TextButton(
                                onClick = onClearQueue,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = if (isUr) "قطار صاف کریں" else "Clear Queue",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = PosRed)
                                )
                            }
                        }

                        TextButton(
                            onClick = { showDetailsModal = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (isUr) "مکمل سرور رپورٹ" else "Full Server Report →",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialog when user requests full report
    if (showDetailsModal) {
        OfflineSyncDetailsDialog(
            isOnline = isOnline,
            syncState = syncState,
            pendingCount = pendingCount,
            pendingItems = pendingItems,
            lastSyncedTime = lastSyncedTime,
            isUrdu = isUr,
            onDismiss = { showDetailsModal = false },
            onSyncClick = {
                onSyncClick()
                showDetailsModal = false
            }
        )
    }
}

/**
 * Individual row displaying a queued sync item pending transmission.
 */
@Composable
fun PendingItemRow(
    item: SyncQueueEntity,
    isUrdu: Boolean = false,
    modifier: Modifier = Modifier
) {
    val timeStr = remember(item.timestamp) {
        val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    val typeColor = when (item.entityType.uppercase()) {
        "ORDER" -> PosAmber
        "PAYMENT" -> PosGreen
        "EXPENSE" -> PosRed
        "STOCK", "INVENTORY" -> PosSkyBlue
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Type badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = typeColor.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, typeColor)
                ) {
                    Text(
                        text = item.entityType,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = typeColor
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                Text(
                    text = "#${item.entityId}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Text(
                    text = item.action,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                )

                // Transmit pending indicator
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(PosOrange)
                )
            }
        }
    }
}

/**
 * Dedicated Card variant for embedding in specific screens like Settings or Transactions.
 */
@Composable
fun OfflineSyncStatusCard(
    syncState: SyncState,
    isOnline: Boolean,
    pendingCount: Int,
    modifier: Modifier = Modifier,
    pendingItems: List<SyncQueueEntity> = emptyList(),
    lastSyncedTime: String? = null,
    currentLanguage: String = "en",
    onSyncClick: () -> Unit = {}
) {
    val isUr = currentLanguage == "ur"
    val isSyncing = syncState is SyncState.Syncing

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (pendingCount > 0) PosOrange.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSyncing -> MaterialTheme.colorScheme.primary
                                    !isOnline -> PosOrange
                                    pendingCount > 0 -> PosAmber
                                    else -> PosGreen
                                }
                            )
                    )
                    Text(
                        text = if (isUr) "ریموٹ کلاؤڈ سنکرونائزیشن اسٹیٹس" else "Remote Cloud Sync Status",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = if (isOnline) PosGreenLight else PosRedLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isOnline) PosGreen else PosRed)
                ) {
                    Text(
                        text = if (isOnline) (if (isUr) "آن لائن" else "ONLINE") else (if (isUr) "آف لائن" else "OFFLINE"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (isOnline) PosGreen else PosRed
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stat Columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pending Transmission Box
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (pendingCount > 0) Color(0x22E8590C) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (pendingCount > 0) PosOrange else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isUr) "ترسیل کے منتظر" else "Pending Upload",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "$pendingCount ${if (isUr) "ریکارڈز" else "records"}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (pendingCount > 0) PosOrange else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                // Last Sync Box
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isUr) "آخری بار ہم وقت" else "Last Synced",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = lastSyncedTime ?: (if (isUr) "ابھی تک نہیں" else "Not yet"),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            Button(
                onClick = onSyncClick,
                enabled = !isSyncing,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth().testTag("sync_card_transmit_button")
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isUr) "سرور پر ترسیل ہو رہی ہے…" else "Transmitting to Server…")
                } else {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (pendingCount > 0) {
                            if (isUr) "منتظر ڈیٹا ریموٹ سرور پر منتقل کریں ($pendingCount)" else "Transmit Pending Data ($pendingCount)"
                        } else {
                            if (isUr) "ریموٹ سرور سے تصدیق کریں" else "Verify & Sync with Remote Server"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Detailed Dialog for inspecting remote server connection, transmission queue, and telemetry.
 */
@Composable
fun OfflineSyncDetailsDialog(
    isOnline: Boolean,
    syncState: SyncState,
    pendingCount: Int,
    pendingItems: List<SyncQueueEntity>,
    lastSyncedTime: String?,
    isUrdu: Boolean,
    onDismiss: () -> Unit,
    onSyncClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (isOnline) PosGreen else PosOrange
                )
                Text(
                    text = if (isUrdu) "آف لائن سنک اور سرور اسٹیٹس رپورٹ" else "Offline Sync & Server Status",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status Pill Summary
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isUrdu) "انٹرنیٹ رابطہ:" else "Network State:", fontSize = 12.sp)
                            Text(
                                text = if (isOnline) (if (isUrdu) "منسلک (آن لائن)" else "Connected (Online)") else (if (isUrdu) "غیر منسلک (آف لائن)" else "Disconnected (Offline)"),
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) PosGreen else PosOrange,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isUrdu) "ترسیل کے منتظر ریکارڈز:" else "Pending Transmission:", fontSize = 12.sp)
                            Text(
                                text = "$pendingCount ${if (isUrdu) "ریکارڈز" else "records"}",
                                fontWeight = FontWeight.Bold,
                                color = if (pendingCount > 0) PosOrange else PosGreen,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isUrdu) "آخری ہم وقتی:" else "Last Successful Sync:", fontSize = 12.sp)
                            Text(
                                text = lastSyncedTime ?: (if (isUrdu) "کوئی نہیں" else "Never"),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Text(
                    text = if (isUrdu) "ریموٹ سرور اینڈ پوائنٹ:" else "Remote Server Endpoint:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Google Apps Script Cloud Backend Engine\nscript.google.com/macros/s/AKfycbw.../exec",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                if (pendingItems.isNotEmpty()) {
                    Text(
                        text = if (isUrdu) "قطار میں موجود ریکارڈز:" else "Items Pending in Transmission Queue:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(pendingItems) { item ->
                            PendingItemRow(item = item, isUrdu = isUrdu)
                        }
                    }
                } else {
                    Text(
                        text = if (isUrdu) "✓ کوئی بھی ریکارڈ ریموٹ سرور پر جانے کے لیے باقی نہیں ہے۔ آپ کا فون مکمل طور پر اپ ٹو ڈیٹ ہے۔" else "✓ Local Room Database is fully synchronized with remote server. No records pending.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PosGreen,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSyncClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isUrdu) "ابھی سنک کریں" else "Sync Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isUrdu) "بند کریں" else "Close")
            }
        }
    )
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
