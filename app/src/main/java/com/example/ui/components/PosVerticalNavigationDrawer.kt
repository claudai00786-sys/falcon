package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalStrings
import com.example.viewmodel.AppView

/**
 * Data item representing a vertically aligned main menu navigation tab.
 */
data class VerticalNavMenuItem(
    val view: AppView,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val badgeCount: Int = 0
)

/**
 * Modern, high-craft vertical navigation drawer content.
 * Replaces bottom navigation tabs with a structured, vertically aligned menu system
 * that opens and closes smoothly with each function.
 */
@Composable
fun PosVerticalNavigationDrawerContent(
    currentView: AppView,
    currentLanguage: String,
    cartCount: Int,
    onNavigate: (AppView) -> Unit,
    onCloseDrawer: () -> Unit,
    onToggleLanguage: (String) -> Unit,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onLockClick: (() -> Unit)? = null
) {
    val isUr = currentLanguage == "ur"
    val strings = LocalStrings.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // --- Drawer Header ---
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo and Brand Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppBrandLogo(
                            modifier = Modifier.size(42.dp),
                            showBorder = true
                        )

                        Column {
                            Text(
                                text = strings.brandName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = strings.brandSubtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    // Smooth Close Button
                    IconButton(
                        onClick = onCloseDrawer,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .testTag("drawer_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.closeMenu,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cashier info badge & Quick Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Cashier User Indicator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = strings.cashierUser,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Quick Language Toggle & Theme inside Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Language pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.clickable {
                                onToggleLanguage(if (isUr) "en" else "ur")
                            }
                        ) {
                            Text(
                                text = if (isUr) "English" else "اردو",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Theme toggle
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = strings.toggleTheme,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Vertically Aligned Menu Items List ---
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Section 1: Sales & Orders
            VerticalNavSectionHeader(title = strings.sectionOperations)

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.OVERVIEW,
                    icon = Icons.Default.Dashboard,
                    title = strings.navOverview,
                    subtitle = if (isUr) "کاروباری تجزیات اور منافع" else "Analytics, KPIs & Profit"
                ),
                isSelected = currentView == AppView.OVERVIEW,
                onSelect = { onNavigate(AppView.OVERVIEW) }
            )

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.CATALOG,
                    icon = Icons.Default.ShoppingCart,
                    title = strings.navCatalog,
                    subtitle = if (isUr) "فین راڈز کیٹلاگ اور آرڈر بکنگ" else "Products, Cart & Booking",
                    badgeCount = cartCount
                ),
                isSelected = currentView == AppView.CATALOG,
                onSelect = { onNavigate(AppView.CATALOG) }
            )

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.ORDER_BOOKED,
                    icon = Icons.Default.ReceiptLong,
                    title = strings.navOrders,
                    subtitle = if (isUr) "بُک شدہ آرڈرز اور ادائیگی وصولی" else "Booked Orders & Receipts"
                ),
                isSelected = currentView == AppView.ORDER_BOOKED,
                onSelect = { onNavigate(AppView.ORDER_BOOKED) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Section 2: Accounts & Ledgers
            VerticalNavSectionHeader(title = strings.sectionLedgers)

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.FACTORIES_CUSTOMER,
                    icon = Icons.Default.Business,
                    title = strings.tabCustomerFactories,
                    subtitle = if (isUr) "گاہک فیکٹریاں اور بقایا کھاتے" else "Debtor Balances & Invoices"
                ),
                isSelected = currentView == AppView.FACTORIES_CUSTOMER,
                onSelect = { onNavigate(AppView.FACTORIES_CUSTOMER) }
            )

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.PAINT_LEDGER,
                    icon = Icons.Default.ColorLens,
                    title = strings.tabPaint,
                    subtitle = if (isUr) "پینٹ سپلائرز اور پروسیسنگ" else "Paint Vendors & Bill Ledger"
                ),
                isSelected = currentView == AppView.PAINT_LEDGER,
                onSelect = { onNavigate(AppView.PAINT_LEDGER) }
            )

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.RAW_MATERIAL_LEDGER,
                    icon = Icons.Default.Layers,
                    title = strings.tabRawMaterial,
                    subtitle = if (isUr) "اسٹیل پائپ کوائلز اور وزن" else "Steel Pipe Coils & Suppliers"
                ),
                isSelected = currentView == AppView.RAW_MATERIAL_LEDGER,
                onSelect = { onNavigate(AppView.RAW_MATERIAL_LEDGER) }
            )

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.SCRAP_LEDGER,
                    icon = Icons.Default.DeleteSweep,
                    title = strings.tabScrap,
                    subtitle = if (isUr) "اسکریپ لوہا فروخت و وصولیاں" else "Metal Scrap Sales & Debtors"
                ),
                isSelected = currentView == AppView.SCRAP_LEDGER,
                onSelect = { onNavigate(AppView.SCRAP_LEDGER) }
            )

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.LABOUR_LEDGER,
                    icon = Icons.Default.Engineering,
                    title = strings.tabLabour,
                    subtitle = if (isUr) "کاریگروں کی یومیہ اجرت و حاضری" else "Attendance & Piece-Rate Wagers"
                ),
                isSelected = currentView == AppView.LABOUR_LEDGER,
                onSelect = { onNavigate(AppView.LABOUR_LEDGER) }
            )

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.CUSTOM_LEDGERS,
                    icon = Icons.Default.FolderShared,
                    title = strings.tabCustomFactory,
                    subtitle = if (isUr) "دیگر کسٹم پارٹی کھاتے" else "General & Third-Party Ledgers"
                ),
                isSelected = currentView == AppView.CUSTOM_LEDGERS,
                onSelect = { onNavigate(AppView.CUSTOM_LEDGERS) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Section 3: Inventory & Factory Operations
            VerticalNavSectionHeader(title = strings.sectionInventory)

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.STOCK,
                    icon = Icons.Default.Inventory2,
                    title = strings.navStock,
                    subtitle = if (isUr) "تیار مال اور گودام انوینٹری" else "Ready Stock & Factory Inventory"
                ),
                isSelected = currentView == AppView.STOCK,
                onSelect = { onNavigate(AppView.STOCK) }
            )

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.EXPENSES,
                    icon = Icons.Default.AttachMoney,
                    title = strings.navExpenses,
                    subtitle = if (isUr) "بجلی، کرایہ، ایندھن و متفرق" else "Operating & Factory Utilities"
                ),
                isSelected = currentView == AppView.EXPENSES,
                onSelect = { onNavigate(AppView.EXPENSES) }
            )

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.WITHDRAWALS,
                    icon = Icons.Default.AccountBalanceWallet,
                    title = strings.withdrawalsTitle,
                    subtitle = if (isUr) "مالک کی ذاتی رقم نکاسی" else "Owner Personal Drawings"
                ),
                isSelected = currentView == AppView.WITHDRAWALS,
                onSelect = { onNavigate(AppView.WITHDRAWALS) }
            )

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.PRODUCT_RETURNS,
                    icon = Icons.Default.AssignmentReturn,
                    title = strings.returnsTitle,
                    subtitle = if (isUr) "نقص والا مال و اسکریپ اندراج" else "Defective Batches & Rework"
                ),
                isSelected = currentView == AppView.PRODUCT_RETURNS,
                onSelect = { onNavigate(AppView.PRODUCT_RETURNS) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Section 4: System & Security
            VerticalNavSectionHeader(title = strings.sectionSystem)

            VerticalNavMenuItemRow(
                item = VerticalNavMenuItem(
                    view = AppView.SETTINGS,
                    icon = Icons.Default.Settings,
                    title = strings.navSettings,
                    subtitle = if (isUr) "پن، انکرپشن، کلاؤڈ سنک" else "Security PIN, Backup & Theme"
                ),
                isSelected = currentView == AppView.SETTINGS,
                onSelect = { onNavigate(AppView.SETTINGS) }
            )
        }

        // --- Drawer Bottom Action Bar ---
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Lock POS Quick Action
                if (onLockClick != null) {
                    Button(
                        onClick = onLockClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("drawer_lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = strings.lockApp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.lockApp,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Security & Version Tag
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "AES-256-GCM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "v2.4 Pro Build",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Section header divider inside the vertical navigation drawer.
 */
@Composable
private fun VerticalNavSectionHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

/**
 * Individual vertically aligned menu item row with icon, title, subtitle,
 * animated active highlight pill, badge count, and minimum 48dp touch target.
 */
@Composable
private fun VerticalNavMenuItemRow(
    item: VerticalNavMenuItem,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = tween(220),
        label = "nav_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(220),
        label = "nav_content"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(220),
        label = "nav_icon"
    )

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .testTag("menu_tab_${item.view.name.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Icon container box with soft active background
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = iconColor,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text column: Title and Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.5.sp,
                        color = contentColor
                    )
                )
                if (item.subtitle.isNotEmpty()) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1
                    )
                }
            }

            // Optional Cart count or status badge
            if (item.badgeCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Text(
                        text = "${item.badgeCount}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (isSelected) {
                // Active indicator dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
