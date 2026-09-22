package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.OfflineSyncStatusBanner
import com.example.ui.components.PosTopBar
import com.example.ui.components.PosVerticalNavigationDrawerContent
import com.example.ui.screens.*
import com.example.ui.theme.FalconPosTheme
import com.example.ui.util.LocalStrings
import com.example.ui.util.ProvideLocalization
import com.example.viewmodel.AppView
import com.example.viewmodel.PosViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: PosViewModel = viewModel()
            val currentPreset by viewModel.currentThemePreset.collectAsState()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            FalconPosTheme(
                darkTheme = isDarkTheme,
                themePreset = currentPreset
            ) {
                PosMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosMainApp(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val enteredPin by viewModel.enteredPin.collectAsState()
    val pinError by viewModel.pinError.collectAsState()
    val currentView by viewModel.currentView.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val totalPendingCount by viewModel.totalPendingCount.collectAsState()
    val pendingSyncQueue by viewModel.pendingSyncQueue.collectAsState()
    val lastSyncedAt by viewModel.lastSyncedAt.collectAsState()

    ProvideLocalization(localizationManager = viewModel.localizationManager) {
        if (!isUnlocked) {
            LockScreen(
                enteredPin = enteredPin,
                pinError = pinError,
                onPinDigit = { viewModel.enterPinDigit(it) },
                onPinBackspace = { viewModel.deletePinDigit() },
                onPinClear = { viewModel.clearPin() },
                onResetPinWithAnswer = { answer, newPin -> viewModel.resetPinWithAnswer(answer, newPin) },
                currentLanguage = currentLanguage,
                onToggleLanguage = { viewModel.setLanguage(it) }
            )
        } else {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val coroutineScope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = isUnlocked,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        drawerTonalElevation = 6.dp,
                        modifier = Modifier
                            .width(320.dp)
                            .fillMaxHeight()
                    ) {
                        PosVerticalNavigationDrawerContent(
                            currentView = currentView,
                            currentLanguage = currentLanguage,
                            cartCount = cart.sumOf { it.quantity },
                            onNavigate = { targetView ->
                                viewModel.navigateTo(targetView)
                                coroutineScope.launch { drawerState.close() }
                            },
                            onCloseDrawer = {
                                coroutineScope.launch { drawerState.close() }
                            },
                            onToggleLanguage = { viewModel.setLanguage(it) },
                            onToggleTheme = { viewModel.toggleTheme() },
                            isDarkTheme = isDarkTheme,
                            onLockClick = {
                                coroutineScope.launch { drawerState.close() }
                                viewModel.lockApp()
                            }
                        )
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            PosTopBar(
                                syncState = syncState,
                                isDarkTheme = isDarkTheme,
                                currentLanguage = currentLanguage,
                                pendingCount = totalPendingCount,
                                onToggleTheme = { viewModel.toggleTheme() },
                                onToggleLanguage = { viewModel.setLanguage(it) },
                                onSyncClick = { viewModel.manualSync() },
                                onOpenSearch = { viewModel.navigateTo(AppView.CATALOG) },
                                onLockClick = { viewModel.lockApp() },
                                onMenuClick = {
                                    coroutineScope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                }
                            )

                            // Prominent Offline Sync Status Banner displaying pending transmission status
                            OfflineSyncStatusBanner(
                                syncState = syncState,
                                isOnline = isOnline,
                                pendingCount = totalPendingCount,
                                pendingItems = pendingSyncQueue,
                                lastSyncedTime = lastSyncedAt.ifEmpty { null },
                                currentLanguage = currentLanguage,
                                onSyncClick = { viewModel.manualSync() },
                                onTestQueueItem = { viewModel.enqueueTestSyncItem() },
                                onClearQueue = { viewModel.clearAllPendingQueue() },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing,
                    modifier = modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .imePadding()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        AnimatedContent(
                            targetState = currentView,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "view_transition"
                        ) { view ->
                            when (view) {
                                AppView.OVERVIEW -> OverviewScreen(
                                    viewModel = viewModel,
                                    onNavigateTo = { viewModel.navigateTo(it) }
                                )
                                AppView.CATALOG -> CatalogScreen(viewModel = viewModel)
                                AppView.ORDER_BOOKED -> TransactionsScreen(viewModel = viewModel)
                                AppView.FACTORIES_CUSTOMER -> LedgersScreen(viewModel = viewModel, initialTab = LedgerTab.CUSTOMER_FACTORIES)
                                AppView.PAINT_LEDGER -> LedgersScreen(viewModel = viewModel, initialTab = LedgerTab.PAINT)
                                AppView.RAW_MATERIAL_LEDGER -> LedgersScreen(viewModel = viewModel, initialTab = LedgerTab.RAW_MATERIAL)
                                AppView.SCRAP_LEDGER -> LedgersScreen(viewModel = viewModel, initialTab = LedgerTab.SCRAP)
                                AppView.LABOUR_LEDGER -> LedgersScreen(viewModel = viewModel, initialTab = LedgerTab.LABOUR)
                                AppView.CUSTOM_LEDGERS -> LedgersScreen(viewModel = viewModel, initialTab = LedgerTab.CUSTOM_FACTORY)
                                AppView.STOCK -> StockScreen(viewModel = viewModel)
                                AppView.EXPENSES -> ExpensesScreen(viewModel = viewModel, isWithdrawalMode = false)
                                AppView.WITHDRAWALS -> ExpensesScreen(viewModel = viewModel, isWithdrawalMode = true)
                                AppView.PRODUCT_RETURNS -> ProductReturnsScreen(viewModel = viewModel)
                                AppView.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}


