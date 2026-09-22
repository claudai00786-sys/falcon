package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sync.SyncState
import com.example.ui.theme.*
import com.example.ui.util.LocalStrings
import com.example.ui.util.LocalizationManager
import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double, isUrdu: Boolean = false): String {
    return LocalizationManager.getBundle(isUrdu).formatCurrency(amount)
}

fun formatQuantity(amount: Double, isUrdu: Boolean = false): String {
    return LocalizationManager.getBundle(isUrdu).formatPcs(amount)
}

/**
 * Official Brand Logo Component displaying the Falcon and Industrial Downrod emblem.
 * Replaces letter badges with high-resolution brand asset.
 */
@Composable
fun AppBrandLogo(
    modifier: Modifier = Modifier,
    contentDescription: String? = "Falcon Rod Maker Logo",
    showBorder: Boolean = true,
    backgroundColor: Color = Color.White
) {
    Surface(
        modifier = modifier.clip(CircleShape),
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = if (showBorder) 2.dp else 0.dp,
        border = if (showBorder) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_app_logo),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Top App Bar with Falcon Rod Maker branding, sync widget, theme toggle, and language switcher.
 */
@Composable
fun PosTopBar(
    syncState: SyncState,
    isDarkTheme: Boolean,
    currentLanguage: String,
    onToggleTheme: () -> Unit,
    onToggleLanguage: (String) -> Unit,
    onSyncClick: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
    pendingCount: Int = 0,
    onLockClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        val strings = LocalStrings.current
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Menu Toggle + Brand Name & Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onMenuClick != null) {
                        IconButton(
                            onClick = onMenuClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .testTag("topbar_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = strings.menuTitle,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    AppBrandLogo(
                        modifier = Modifier.size(38.dp),
                        showBorder = true
                    )

                    val strings = LocalStrings.current
                    Column {
                        Text(
                            text = strings.brandName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = strings.brandSubtitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }
                }

                // Controls: Sync indicator, Search, Theme toggle, Language switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sync Widget with animated pulsing dot
                    SyncStatusWidget(
                        syncState = syncState,
                        currentLanguage = currentLanguage,
                        pendingCount = pendingCount,
                        onClick = onSyncClick
                    )

                    // Global Search Button
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("topbar_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Everything",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Quick Lock Button
                    if (onLockClick != null) {
                        IconButton(
                            onClick = onLockClick,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("topbar_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock POS",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Theme Toggle
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("topbar_theme_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Language Toggle (EN / اردو)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentLanguage == "en") Color(0xFF1C1F22) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (currentLanguage == "en") MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onToggleLanguage("en") }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("lang_en_button")
                        )
                        Text(
                            text = "اردو",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentLanguage == "ur") Color(0xFF1C1F22) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (currentLanguage == "ur") MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onToggleLanguage("ur") }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("lang_ur_button")
                        )
                    }
                }
            }

            // Hazard decorative accent bar from HTML
            HazardBar()
        }
    }
}

/**
 * Visual signature hazard bar (diagonal black & gold stripes).
 */
@Composable
fun HazardBar(modifier: Modifier = Modifier) {
    val yellow = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
    ) {
        val stripeWidth = 14.dp.toPx()
        val totalWidth = size.width
        var x = 0f
        var isYellow = true
        while (x < totalWidth) {
            val endX = (x + stripeWidth).coerceAtMost(totalWidth)
            drawRect(
                color = if (isYellow) yellow else Color(0xFF1C1F22),
                topLeft = Offset(x, 0f),
                size = Size(endX - x, size.height)
            )
            x += stripeWidth
            isYellow = !isYellow
        }
    }
}

/**
 * Cloud Synchronization Pill Widget with glowing pulsing dot indicator.
 */
@Composable
fun SyncStatusWidget(
    syncState: SyncState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentLanguage: String = "en",
    pendingCount: Int = 0
) {
    val isUr = currentLanguage == "ur"
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val (dotColor, labelText) = when {
        syncState is SyncState.Syncing -> MaterialTheme.colorScheme.primary to (if (isUr) "سنک جاری ہے…" else "Syncing…")
        syncState is SyncState.Error -> PosRed to (if (isUr) "سنک خرابی" else "Sync error")
        pendingCount > 0 -> PosOrange to (if (isUr) "$pendingCount منتظر" else "$pendingCount Pending")
        syncState is SyncState.Synced -> PosGreen to (if (isUr) "ہم وقت ہے" else "Synced")
        syncState is SyncState.LocalSaved -> MaterialTheme.colorScheme.onSurfaceVariant to (if (isUr) "محفوظ شدہ" else syncState.message)
        else -> MaterialTheme.colorScheme.onSurfaceVariant to (if (isUr) "ڈیوائس پر محفوظ" else "Saved on device")
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .height(32.dp)
            .clickable { onClick() }
            .testTag("sync_status_widget")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(
                        if (syncState is SyncState.Syncing) dotColor.copy(alpha = alpha) else dotColor
                    )
            )
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

/**
 * High contrast metric Stat Card.
 */
@Composable
fun PosStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subValue: String? = null,
    accentColor: Color? = null
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    color = accentColor ?: MaterialTheme.colorScheme.primary
                )
            )
            if (!subValue.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

/**
 * Donut / Ring chart canvas component for visual analytics.
 */
@Composable
fun DonutChart(
    segments: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier,
    centerText: String? = null,
    centerSubText: String? = null
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val total = segments.sumOf { it.first.toDouble() }.toFloat()
            if (total <= 0f) {
                drawCircle(
                    color = Color(0x33888888),
                    radius = size.minDimension / 2f * 0.85f,
                    style = Stroke(width = 18.dp.toPx())
                )
                return@Canvas
            }

            var startAngle = -90f
            val strokeWidth = 20.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f

            segments.forEach { (value, color) ->
                if (value > 0f) {
                    val sweepAngle = (value / total) * 360f
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                        size = Size(radius * 2f, radius * 2f),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        if (centerText != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (centerSubText != null) {
                    Text(
                        text = centerSubText.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Text(
                    text = centerText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

/**
 * Section Header with title, subtitle and optional action button.
 */
@Composable
fun PosSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    actionButton: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
        if (actionButton != null) {
            Box(modifier = Modifier.padding(start = 10.dp)) {
                actionButton()
            }
        }
    }
}
