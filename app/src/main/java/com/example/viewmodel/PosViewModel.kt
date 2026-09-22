package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.PosRepository
import com.example.sync.CloudSyncManager
import com.example.sync.SyncState
import com.example.ui.theme.ColorThemePreset
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalizationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class CartItem(
    val product: ProductEntity,
    val quantity: Int
) {
    val lineTotal: Double get() = product.price * quantity
}

data class PeriodRange(val from: Date, val to: Date, val label: String)

enum class AppView {
    OVERVIEW,
    CATALOG,
    ORDER_BOOKED,
    FACTORIES_CUSTOMER,
    PAINT_LEDGER,
    RAW_MATERIAL_LEDGER,
    SCRAP_LEDGER,
    LABOUR_LEDGER,
    CUSTOM_LEDGERS,
    EXPENSES,
    WITHDRAWALS,
    PRODUCT_RETURNS,
    STOCK,
    SETTINGS
}

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PosRepository
    val syncManager: CloudSyncManager

    // --- Navigation & State ---
    private val _currentView = MutableStateFlow(AppView.OVERVIEW)
    val currentView: StateFlow<AppView> = _currentView.asStateFlow()

    // --- Authentication & Lock Screen ---
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _enteredPin = MutableStateFlow("")
    val enteredPin: StateFlow<String> = _enteredPin.asStateFlow()

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError.asStateFlow()

    private val _currentPin = MutableStateFlow("321")
    val currentPin: StateFlow<String> = _currentPin.asStateFlow()

    private val _recoveryAnswer = MutableStateFlow("amir")
    val recoveryAnswer: StateFlow<String> = _recoveryAnswer.asStateFlow()

    // --- Localization & Theming ---
    val localizationManager = LocalizationManager.getInstance()
    private val _currentLanguage = MutableStateFlow("en") // "en" or "ur"
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _currentThemePreset = MutableStateFlow(ColorThemePreset.AMBER)
    val currentThemePreset: StateFlow<ColorThemePreset> = _currentThemePreset.asStateFlow()

    // --- Catalog & Cart ---
    private val _selectedCategory = MutableStateFlow("American Pedestal Fan")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _selectedOrderFactory = MutableStateFlow<String?>(null)
    val selectedOrderFactory: StateFlow<String?> = _selectedOrderFactory.asStateFlow()

    private val _orderDate = MutableStateFlow(getCurrentDateString())
    val orderDate: StateFlow<String> = _orderDate.asStateFlow()

    // --- Reporting & Filter Period ---
    private val _selectedPeriod = MutableStateFlow("Daily")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    // --- Data Flows from Room ---
    val products: StateFlow<List<ProductEntity>>
    val factories: StateFlow<List<FactoryEntity>>
    val transactions: StateFlow<List<OrderTransactionEntity>>
    val customerPayments: StateFlow<List<CustomerPaymentEntity>>
    val workers: StateFlow<List<WorkerEntity>>
    val labourEntries: StateFlow<List<LabourEntryEntity>>
    val expenses: StateFlow<List<ExpenseEntity>>
    val withdrawals: StateFlow<List<WithdrawalEntity>>
    val productReturns: StateFlow<List<ProductReturnEntity>>
    val lowStockProducts: StateFlow<List<ProductEntity>>
    val inventoryMovements: StateFlow<List<InventoryMovementEntity>>

    // Ledger filtered flows
    val paintEntries: StateFlow<List<LedgerEntryEntity>>
    val rawMaterialEntries: StateFlow<List<LedgerEntryEntity>>
    val scrapEntries: StateFlow<List<LedgerEntryEntity>>
    val customerLedgerEntries: StateFlow<List<LedgerEntryEntity>>
    val customLedgerEntries: StateFlow<List<LedgerEntryEntity>>
    val cashInHand: StateFlow<Double>

    val syncState: StateFlow<SyncState>
    val isOnline: StateFlow<Boolean>
    val pendingSyncQueue: StateFlow<List<SyncQueueEntity>>
    val pendingSyncCount: StateFlow<Int>
    val pendingTransactionsCount: StateFlow<Int>
    val totalPendingCount: StateFlow<Int>
    private val _lastSyncedAt = MutableStateFlow<String>("")
    val lastSyncedAt: StateFlow<String> = _lastSyncedAt.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = PosRepository(db.posDao())
        syncManager = CloudSyncManager(application, repository, viewModelScope)
        syncState = syncManager.syncState
        isOnline = syncManager.isNetworkAvailable

        pendingSyncQueue = repository.getAllPendingSyncQueue().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        pendingSyncCount = repository.getPendingSyncQueueCount().stateIn(viewModelScope, SharingStarted.Eagerly, 0)
        pendingTransactionsCount = repository.getPendingTransactionsCount().stateIn(viewModelScope, SharingStarted.Eagerly, 0)
        totalPendingCount = combine(pendingSyncCount, pendingTransactionsCount) { qCount, txnCount ->
            maxOf(qCount, txnCount)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

        products = repository.allProducts.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        lowStockProducts = repository.lowStockProducts.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        inventoryMovements = repository.allInventoryMovements.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        factories = repository.allFactories.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        transactions = repository.allTransactions.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        customerPayments = repository.allCustomerPayments.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        workers = repository.allWorkers.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        labourEntries = repository.allLabourEntries.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        expenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        withdrawals = repository.allWithdrawals.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        productReturns = repository.allProductReturns.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        paintEntries = repository.getLedgerEntriesByType("PAINT").stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        rawMaterialEntries = repository.getLedgerEntriesByType("RAW_MATERIAL").stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        scrapEntries = repository.getLedgerEntriesByType("SCRAP").stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        customerLedgerEntries = repository.getLedgerEntriesByType("CUSTOMER").stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        customLedgerEntries = repository.getLedgerEntriesByType("CUSTOM_FACTORY").stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        cashInHand = combine(
            customerPayments,
            expenses,
            withdrawals
        ) { payments, exps, wds ->
            val cashIn = payments.sumOf { it.amount } + wds.filter { it.isReversal }.sumOf { it.amount }
            val cashOut = exps.sumOf { it.amount } + wds.filter { !it.isReversal }.sumOf { it.amount }
            (cashIn - cashOut).coerceAtLeast(0.0)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

        // Load saved app settings
        viewModelScope.launch {
            repository.allSettings.collect { settingsList ->
                settingsList.forEach { s ->
                    when (s.key) {
                        "pos_pin" -> _currentPin.value = s.value
                        "pos_recovery_answer" -> _recoveryAnswer.value = s.value
                        "pos_language" -> {
                            _currentLanguage.value = s.value
                            localizationManager.setLanguage(s.value)
                        }
                        "pos_theme" -> _isDarkTheme.value = s.value == "dark"
                        "pos_last_synced_at" -> _lastSyncedAt.value = s.value
                        "pos_logo_theme" -> {
                            val preset = ColorThemePreset.entries.find { it.name.equals(s.value, true) }
                            if (preset != null) _currentThemePreset.value = preset
                        }
                    }
                }
            }
        }
    }

    // --- Navigation Actions ---
    fun navigateTo(view: AppView) {
        _currentView.value = view
    }

    // --- PIN and Security Actions ---
    fun enterPinDigit(digit: String) {
        if (_enteredPin.value.length < 4) {
            val newPin = _enteredPin.value + digit
            _enteredPin.value = newPin
            if (newPin.length == 4) {
                verifyPin(newPin)
            }
        }
    }

    fun deletePinDigit() {
        if (_enteredPin.value.isNotEmpty()) {
            _enteredPin.value = _enteredPin.value.dropLast(1)
            _pinError.value = false
        }
    }

    fun clearPin() {
        _enteredPin.value = ""
        _pinError.value = false
    }

    private fun verifyPin(pin: String) {
        if (pin == _currentPin.value) {
            _isUnlocked.value = true
            _pinError.value = false
            _enteredPin.value = ""
        } else {
            _pinError.value = true
            _enteredPin.value = ""
        }
    }

    fun lockApp() {
        _isUnlocked.value = false
        _enteredPin.value = ""
        _pinError.value = false
    }

    fun resetPinWithAnswer(answer: String, newPin: String): Boolean {
        if (answer.trim().equals(_recoveryAnswer.value.trim(), ignoreCase = true) && newPin.length == 4) {
            viewModelScope.launch {
                _currentPin.value = newPin
                repository.setSetting("pos_pin", newPin)
            }
            return true
        }
        return false
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        if (oldPin == _currentPin.value && newPin.length == 4) {
            viewModelScope.launch {
                _currentPin.value = newPin
                repository.setSetting("pos_pin", newPin)
            }
            return true
        }
        return false
    }

    fun setSecurityQuestionAnswer(answer: String) {
        viewModelScope.launch {
            _recoveryAnswer.value = answer
            repository.setSetting("pos_recovery_answer", answer)
        }
    }

    // --- Theme & Language ---
    fun toggleTheme() {
        val newTheme = !_isDarkTheme.value
        _isDarkTheme.value = newTheme
        viewModelScope.launch {
            repository.setSetting("pos_theme", if (newTheme) "dark" else "light")
        }
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
        localizationManager.setLanguage(lang)
        viewModelScope.launch {
            repository.setSetting("pos_language", lang)
        }
    }

    fun toggleLanguage() {
        val next = if (_currentLanguage.value == "ur") "en" else "ur"
        setLanguage(next)
    }

    fun setThemePreset(preset: ColorThemePreset) {
        _currentThemePreset.value = preset
        viewModelScope.launch {
            repository.setSetting("pos_logo_theme", preset.name.lowercase())
        }
    }

    // --- Catalog & Cart Actions ---
    fun selectCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: ProductEntity) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val existing = currentList[index]
            currentList[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentList.add(CartItem(product, 1))
        }
        _cart.value = currentList
    }

    fun updateCartQuantity(productId: Long, quantity: Int) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            if (quantity <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = currentList[index].copy(quantity = quantity)
            }
            _cart.value = currentList
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun setSelectedOrderFactory(factoryName: String?) {
        _selectedOrderFactory.value = factoryName
    }

    fun setOrderDate(date: String) {
        _orderDate.value = date
    }

    // --- Order Checkout & Booking with Mathematical Auto-Solve ---
    fun checkoutOrder(
        paid: Boolean,
        discount: Double = 0.0,
        advancePaid: Double = 0.0,
        paymentMethod: String = "Cash"
    ) {
        val currentCart = _cart.value
        if (currentCart.isEmpty()) return

        val factoryName = _selectedOrderFactory.value ?: "Walk-in Customer"
        val grossSubtotal = currentCart.sumOf { it.lineTotal }
        val netTotal = (grossSubtotal - discount).coerceAtLeast(0.0)
        val finalPaid = if (paid) netTotal else advancePaid.coerceIn(0.0, netTotal)
        val isFullyPaid = finalPaid >= netTotal

        val totalCount = currentCart.sumOf { it.quantity }
        val itemsSummary = currentCart.joinToString("\n") { it.product.name }
        val itemCounts = currentCart.joinToString("\n") { it.quantity.toString() }
        val itemRates = currentCart.joinToString("\n") { it.product.price.toInt().toString() }
        val itemProductIds = currentCart.joinToString("\n") { it.product.id.toString() }
        val sizes = currentCart.mapNotNull { it.product.size }.joinToString("\n")
        val colors = currentCart.mapNotNull { it.product.color }.joinToString("\n")

        val txns = transactions.value
        val nextIdNumber = (txns.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0) + 1
        val orderId = "%04d".format(nextIdNumber)

        val newTransaction = OrderTransactionEntity(
            id = orderId,
            date = _orderDate.value,
            time = getCurrentTimeString(),
            itemsSummary = itemsSummary,
            itemCount = totalCount,
            itemCounts = itemCounts,
            itemRates = itemRates,
            itemProductIds = itemProductIds,
            total = netTotal,
            factory = factoryName,
            confirmed = true,
            delivered = false,
            sizes = sizes,
            colors = colors,
            paid = isFullyPaid,
            method = if (finalPaid > 0) paymentMethod else null,
            syncStatus = "pending"
        )

        viewModelScope.launch {
            repository.saveTransaction(newTransaction)
            repository.enqueueSync(
                SyncQueueEntity(
                    entityType = "ORDER",
                    entityId = orderId,
                    action = "UPSERT",
                    payloadJson = "{\"orderId\":\"$orderId\",\"total\":$netTotal,\"customer\":\"$factoryName\"}"
                )
            )

            // Decrement stock for tracked products and log inventory movement in Room
            currentCart.forEach { item ->
                if (item.product.stock != null) {
                    val prevStock = item.product.stock
                    val newStock = (prevStock - item.quantity).coerceAtLeast(0)
                    repository.decrementStock(item.product.id, item.quantity)
                    repository.recordInventoryMovement(
                        InventoryMovementEntity(
                            productId = item.product.id,
                            productName = item.product.name,
                            movementType = "SALE",
                            quantityChange = -item.quantity,
                            previousStock = prevStock,
                            newStock = newStock,
                            referenceId = "Order #$orderId",
                            note = "Sold to $factoryName",
                            date = _orderDate.value,
                            time = getCurrentTimeString(),
                            syncStatus = "pending"
                        )
                    )
                }
            }

            // If advance or full payment made, record payment entry
            if (finalPaid > 0) {
                val payment = CustomerPaymentEntity(
                    id = "cp_${System.currentTimeMillis()}",
                    txnId = orderId,
                    date = _orderDate.value,
                    time = getCurrentTimeString(),
                    amount = finalPaid,
                    method = paymentMethod,
                    detail = if (isFullyPaid) "Full payment at checkout" else "Advance deposit (Gross: Rs $grossSubtotal, Disc: Rs $discount)",
                    receivedBy = "Amir",
                    receivedIn = "Counter Cash"
                )
                repository.recordCustomerPayment(payment)
            }

            // Create corresponding customer ledger entry with auto-deducted balance
            val ledgerEntry = LedgerEntryEntity(
                id = "cl_$orderId",
                ledgerType = "CUSTOMER",
                partyName = factoryName,
                date = _orderDate.value,
                time = getCurrentTimeString(),
                desc = "Order #$orderId — ${currentCart.firstOrNull()?.product?.name ?: "Fan Guards"}${if (discount > 0) " (Disc: Rs $discount)" else ""}",
                debit = netTotal,
                credit = finalPaid,
                method = if (finalPaid > 0) paymentMethod else ""
            )
            repository.saveLedgerEntry(ledgerEntry)

            clearCart()
            _selectedOrderFactory.value = null
            _orderDate.value = getCurrentDateString()

            // Trigger sync to cloud
            syncManager.triggerSync()
        }
    }

    // --- Order Actions ---
    fun markOrderConfirmed(orderId: String) {
        viewModelScope.launch {
            val txn = transactions.value.find { it.id == orderId } ?: return@launch
            repository.updateTransaction(txn.copy(confirmed = true))
            syncManager.triggerSync()
        }
    }

    fun markOrderDelivered(orderId: String) {
        viewModelScope.launch {
            val txn = transactions.value.find { it.id == orderId } ?: return@launch
            repository.updateTransaction(txn.copy(delivered = true))
            syncManager.triggerSync()
        }
    }

    fun recordOrderPayment(orderId: String, amount: Double, method: String, detail: String, receivedBy: String, receivedIn: String) {
        viewModelScope.launch {
            val txn = transactions.value.find { it.id == orderId } ?: return@launch
            val paymentId = "cp_${System.currentTimeMillis()}"
            val payment = CustomerPaymentEntity(
                id = paymentId,
                txnId = orderId,
                date = getCurrentDateString(),
                time = getCurrentTimeString(),
                amount = amount,
                method = method,
                detail = detail,
                receivedBy = receivedBy,
                receivedIn = receivedIn
            )
            repository.recordCustomerPayment(payment)

            // Check if fully paid
            val existingPayments = repository.getPaymentsForTransaction(orderId).first()
            val totalPaid = existingPayments.sumOf { it.amount } + amount
            val fullyPaid = totalPaid >= txn.total
            repository.updateTransaction(txn.copy(paid = fullyPaid))

            // Add payment to customer ledger
            if (!txn.factory.isNullOrEmpty()) {
                val ledgerEntry = LedgerEntryEntity(
                    id = "cl_pay_${System.currentTimeMillis()}",
                    ledgerType = "CUSTOMER",
                    partyName = txn.factory,
                    date = getCurrentDateString(),
                    time = getCurrentTimeString(),
                    desc = "Payment for Order #$orderId",
                    debit = 0.0,
                    credit = amount,
                    method = method,
                    detail = detail,
                    receivedBy = receivedBy,
                    receivedIn = receivedIn
                )
                repository.saveLedgerEntry(ledgerEntry)
            }

            syncManager.triggerSync()
        }
    }

    fun nilOrder(orderId: String) {
        viewModelScope.launch {
            val txn = transactions.value.find { it.id == orderId } ?: return@launch
            val payments = repository.getPaymentsForTransaction(orderId).first()
            val paidAmount = payments.sumOf { it.amount }
            val remainingDue = (txn.total - paidAmount).coerceAtLeast(0.0)

            repository.updateTransaction(
                txn.copy(
                    total = paidAmount,
                    writtenOff = remainingDue,
                    paid = true
                )
            )
            syncManager.triggerSync()
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(orderId)
            syncManager.triggerSync()
        }
    }

    // --- Product & Inventory Management ---
    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            if (product.id == 0L) {
                val newId = repository.saveProduct(product)
                // If initial stock is specified, record initial inventory movement in Room
                if (product.stock != null && product.stock > 0) {
                    repository.recordInventoryMovement(
                        InventoryMovementEntity(
                            productId = newId,
                            productName = product.name,
                            movementType = "INITIAL_STOCK",
                            quantityChange = product.stock,
                            previousStock = 0,
                            newStock = product.stock,
                            referenceId = "New Product",
                            note = "Initial stock recorded locally",
                            date = getCurrentDateString(),
                            time = getCurrentTimeString(),
                            syncStatus = "pending"
                        )
                    )
                }
            } else {
                repository.updateProduct(product)
            }
            syncManager.triggerSync()
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
            syncManager.triggerSync()
        }
    }

    fun adjustProductStock(
        product: ProductEntity,
        newStock: Int,
        movementType: String = "ADJUSTMENT",
        reason: String = "Physical Stock Count",
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.adjustProductStock(
                product = product,
                newStock = newStock,
                movementType = movementType,
                reason = reason,
                note = note,
                date = getCurrentDateString(),
                time = getCurrentTimeString()
            )
            syncManager.triggerSync()
        }
    }

    fun restockProduct(
        product: ProductEntity,
        addedQty: Int,
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.restockProduct(
                product = product,
                addedQty = addedQty,
                note = note,
                date = getCurrentDateString(),
                time = getCurrentTimeString()
            )
            syncManager.triggerSync()
        }
    }

    // --- Factory Management ---
    fun saveFactory(name: String, location: String, contact: String) {
        viewModelScope.launch {
            repository.saveFactory(FactoryEntity(name = name, location = location, contact = contact))
            syncManager.triggerSync()
        }
    }

    fun deleteFactory(name: String) {
        viewModelScope.launch {
            repository.deleteFactory(name)
            repository.deleteLedgerEntriesForParty("CUSTOMER", name)
            syncManager.triggerSync()
        }
    }

    // --- Ledger Entry Additions ---
    fun addLedgerEntry(entry: LedgerEntryEntity) {
        viewModelScope.launch {
            repository.saveLedgerEntry(entry)
            syncManager.triggerSync()
        }
    }

    fun deleteLedgerEntry(id: String) {
        viewModelScope.launch {
            repository.deleteLedgerEntry(id)
            syncManager.triggerSync()
        }
    }

    fun updateChequeStatus(id: String, status: String) {
        viewModelScope.launch {
            val entries = paintEntries.value + rawMaterialEntries.value + scrapEntries.value + customerLedgerEntries.value + customLedgerEntries.value
            val entry = entries.find { it.id == id } ?: return@launch
            repository.updateLedgerEntry(entry.copy(chequeStatus = status))
            syncManager.triggerSync()
        }
    }

    // --- Expenses ---
    fun addExpense(desc: String, category: String, amount: Double, method: String, detail: String = "") {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                id = "exp_${System.currentTimeMillis()}",
                date = getCurrentDateStringIso(),
                desc = desc,
                category = category,
                amount = amount,
                method = method,
                detail = detail
            )
            repository.saveExpense(expense)
            syncManager.triggerSync()
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            repository.deleteExpense(id)
            syncManager.triggerSync()
        }
    }

    // --- Withdrawals ---
    fun addWithdrawal(desc: String, amount: Double, method: String = "Cash", detail: String = "", isReversal: Boolean = false) {
        viewModelScope.launch {
            val withdrawal = WithdrawalEntity(
                id = "wd_${System.currentTimeMillis()}",
                date = getCurrentDateStringIso(),
                desc = desc,
                amount = if (isReversal) -amount else amount,
                method = method,
                detail = detail,
                isReversal = isReversal
            )
            repository.saveWithdrawal(withdrawal)
            syncManager.triggerSync()
        }
    }

    fun deleteWithdrawal(id: String) {
        viewModelScope.launch {
            repository.deleteWithdrawal(id)
            syncManager.triggerSync()
        }
    }

    // --- Labour Worker Attendance & Payments ---
    fun addWorker(name: String, workType: String, rateType: String, rate: Double) {
        viewModelScope.launch {
            repository.saveWorker(WorkerEntity(name = name, workType = workType, rateType = rateType, rate = rate))
            syncManager.triggerSync()
        }
    }

    fun deleteWorker(name: String) {
        viewModelScope.launch {
            repository.deleteWorker(name)
            syncManager.triggerSync()
        }
    }

    fun markWorkerAttendance(workerName: String, status: String, units: Double = 0.0, note: String = "") {
        viewModelScope.launch {
            val worker = workers.value.find { it.name == workerName } ?: return@launch
            val creditEarned = when (worker.rateType) {
                "piece" -> units * worker.rate
                "hourly" -> units * worker.rate
                else -> when (status) {
                    "present" -> worker.rate
                    "half" -> worker.rate / 2.0
                    else -> 0.0
                }
            }

            val entry = LabourEntryEntity(
                id = "wl_${System.currentTimeMillis()}",
                workerName = workerName,
                date = getCurrentDateString(),
                time = getCurrentTimeString(),
                kind = "attendance",
                status = status,
                units = units,
                credit = creditEarned,
                debit = 0.0,
                note = note,
                workType = worker.workType
            )
            repository.saveLabourEntry(entry)
            syncManager.triggerSync()
        }
    }

    fun recordWorkerPayment(workerName: String, amount: Double, kind: String, method: String = "Cash", note: String = "") {
        viewModelScope.launch {
            val entry = LabourEntryEntity(
                id = "wl_${System.currentTimeMillis()}",
                workerName = workerName,
                date = getCurrentDateString(),
                time = getCurrentTimeString(),
                kind = kind, // "payment", "advance", "loan", "damage"
                debit = amount,
                credit = 0.0,
                method = method,
                note = note
            )
            repository.saveLabourEntry(entry)
            syncManager.triggerSync()
        }
    }

    fun deleteLabourEntry(id: String) {
        viewModelScope.launch {
            repository.deleteLabourEntry(id)
            syncManager.triggerSync()
        }
    }

    // --- Product Returns & Complaints ---
    fun addProductReturn(factory: String, product: String, quantity: Double, reason: String, orderId: String = "") {
        viewModelScope.launch {
            val ret = ProductReturnEntity(
                id = "pr_${System.currentTimeMillis()}",
                date = getCurrentDateString(),
                factory = factory,
                product = product,
                quantity = quantity,
                reason = reason,
                originalOrderId = orderId
            )
            repository.saveProductReturn(ret)
            syncManager.triggerSync()
        }
    }

    fun resolveProductReturn(
        returnId: String,
        resolution: String, // "reworked" or "scrapped"
        reworkCost: Double = 0.0,
        refundAmount: Double = 0.0,
        destination: String = "factory"
    ) {
        viewModelScope.launch {
            val ret = productReturns.value.find { it.id == returnId } ?: return@launch
            val updated = ret.copy(
                status = "resolved",
                resolution = resolution,
                reworkCost = reworkCost,
                refundAmount = refundAmount,
                destinationChoice = destination
            )
            repository.updateProductReturn(updated)

            // If rework had a cost, log as an Expense
            if (reworkCost > 0) {
                addExpense("Return rework for ${ret.product}", "Labour", reworkCost, "Cash")
            }

            // If refund granted, credit customer ledger
            if (refundAmount > 0) {
                val ledgerEntry = LedgerEntryEntity(
                    id = "cl_ret_${System.currentTimeMillis()}",
                    ledgerType = "CUSTOMER",
                    partyName = ret.factory,
                    date = getCurrentDateString(),
                    time = getCurrentTimeString(),
                    desc = "Credit refund for returned ${ret.product}",
                    debit = 0.0,
                    credit = refundAmount,
                    method = "Adjustment"
                )
                repository.saveLedgerEntry(ledgerEntry)
            }

            syncManager.triggerSync()
        }
    }

    fun deleteProductReturn(id: String) {
        viewModelScope.launch {
            repository.deleteProductReturn(id)
            syncManager.triggerSync()
        }
    }

    // --- Raw Material Stock Computations ---
    fun computeRawMaterialStock(): Map<String, RawStockSummary> {
        val stockMap = mutableMapOf<String, RawStockSummary>()

        // Add from Raw Material Ledger
        rawMaterialEntries.value.forEach { entry ->
            val matName = if (entry.stockName.isNotEmpty()) entry.stockName else entry.desc
            if (matName.isNotEmpty()) {
                val curr = stockMap.getOrPut(matName) { RawStockSummary(matName) }
                val weightIn = entry.weightIn ?: 0.0
                val itemsIn = entry.itemsIn ?: 0.0
                curr.weightReceived += weightIn
                curr.itemsReceived += itemsIn
            }
        }

        // Subtract materials used by booked & delivered orders
        transactions.value.forEach { txn ->
            val productNames = txn.itemsSummary.split("\n").filter { it.isNotBlank() }
            val counts = txn.itemCounts?.split("\n")?.mapNotNull { it.toIntOrNull() } ?: emptyList()

            productNames.forEachIndexed { idx, pName ->
                val pQty = counts.getOrNull(idx) ?: 1
                val prod = products.value.find { it.name == pName }
                if (prod?.recipeJson != null) {
                    try {
                        val jsonArr = JSONArray(prod.recipeJson)
                        for (i in 0 until jsonArr.length()) {
                            val item = jsonArr.getJSONObject(i)
                            val mat = item.getString("material")
                            val wPerUnit = if (item.has("weightPerUnit") && !item.isNull("weightPerUnit")) item.getDouble("weightPerUnit") else 0.0
                            val iPerUnit = if (item.has("itemsPerUnit") && !item.isNull("itemsPerUnit")) item.getDouble("itemsPerUnit") else 0.0

                            val curr = stockMap.getOrPut(mat) { RawStockSummary(mat) }
                            curr.weightUsed += wPerUnit * pQty
                            curr.itemsUsed += iPerUnit * pQty
                        }
                    } catch (e: Exception) {
                        // ignore malformed recipe
                    }
                }
            }
        }

        return stockMap
    }

    fun setPeriodFilter(period: String) {
        _selectedPeriod.value = period
    }

    fun manualSync() {
        viewModelScope.launch {
            syncManager.syncNow()
        }
    }

    fun enqueueTestSyncItem(type: String = "MANUAL_ENTRY", entityId: String = "test_${System.currentTimeMillis()}") {
        viewModelScope.launch {
            repository.enqueueSync(
                SyncQueueEntity(
                    entityType = type,
                    entityId = entityId,
                    action = "UPSERT",
                    payloadJson = "{\"type\":\"$type\",\"time\":\"${getCurrentTimeString()}\"}"
                )
            )
        }
    }

    fun clearAllPendingQueue() {
        viewModelScope.launch {
            repository.clearSyncQueue()
        }
    }

    companion object {
        fun getCurrentDateString(): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date())
        }

        fun getCurrentDateStringIso(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun getCurrentTimeString(): String {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}

data class RawStockSummary(
    val materialName: String,
    var weightReceived: Double = 0.0,
    var weightUsed: Double = 0.0,
    var itemsReceived: Double = 0.0,
    var itemsUsed: Double = 0.0,
    var lowStockThreshold: Double = 10.0
) {
    val weightRemaining: Double get() = (weightReceived - weightUsed).coerceAtLeast(0.0)
    val itemsRemaining: Double get() = (itemsReceived - itemsUsed).coerceAtLeast(0.0)
    val isLowStock: Boolean get() = weightReceived > 0 && weightRemaining <= lowStockThreshold
}
