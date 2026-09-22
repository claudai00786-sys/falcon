package com.example.data.repository

import com.example.data.dao.PosDao
import com.example.data.model.*
import com.example.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PosRepository(private val dao: PosDao) {

    // --- Products ---
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = dao.getLowStockProducts()

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> =
        dao.getProductsByCategory(category)

    fun searchProducts(query: String): Flow<List<ProductEntity>> =
        dao.searchProducts(query)

    suspend fun getProductById(id: Long): ProductEntity? =
        dao.getProductById(id)

    suspend fun saveProduct(product: ProductEntity): Long =
        dao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) =
        dao.updateProduct(product)

    suspend fun deleteProduct(id: Long) =
        dao.deleteProductById(id)

    suspend fun decrementStock(id: Long, qty: Int) =
        dao.decrementStock(id, qty)

    suspend fun updateStock(id: Long, newStock: Int) =
        dao.updateStock(id, newStock)

    suspend fun incrementStock(id: Long, qty: Int) =
        dao.incrementStock(id, qty)

    // --- Factories ---
    val allFactories: Flow<List<FactoryEntity>> = dao.getAllFactories()

    suspend fun saveFactory(factory: FactoryEntity): Long =
        dao.insertFactory(factory)

    suspend fun updateFactory(factory: FactoryEntity) =
        dao.updateFactory(factory)

    suspend fun deleteFactory(name: String) =
        dao.deleteFactoryByName(name)

    // --- Transactions & Booked Orders with E2E Encryption ---
    val allTransactions: Flow<List<OrderTransactionEntity>> = dao.getAllTransactions()
        .map { list ->
            list.map { txn ->
                // Ensure decrypted detail is accessible if it was encrypted
                if (!txn.encryptedPayload.isNullOrEmpty()) {
                    val decrypted = CryptoManager.decrypt(txn.encryptedPayload)
                    txn.copy(detailBank = decrypted)
                } else txn
            }
        }

    fun getTransactionsByFactory(factory: String): Flow<List<OrderTransactionEntity>> =
        dao.getTransactionsByFactory(factory)

    fun getTransactionsByDateRange(startDate: String, endDate: String): Flow<List<OrderTransactionEntity>> =
        dao.getTransactionsByDateRange(startDate, endDate)

    suspend fun getUnsyncedTransactions(): List<OrderTransactionEntity> =
        dao.getTransactionsBySyncStatus("pending")

    suspend fun updateTransactionSyncStatus(id: String, status: String) =
        dao.updateTransactionSyncStatus(id, status)

    suspend fun saveTransaction(txn: OrderTransactionEntity) {
        // End-to-end encrypt sensitive payment detail
        val sensitiveData = "${txn.method ?: ""}|${txn.detailBank ?: ""}|${txn.detailOnline ?: ""}|${txn.total}"
        val encrypted = CryptoManager.encrypt(sensitiveData)
        val securedTxn = txn.copy(
            encryptedPayload = encrypted,
            lastModified = System.currentTimeMillis()
        )
        dao.insertTransaction(securedTxn)
    }

    suspend fun updateTransaction(txn: OrderTransactionEntity) {
        val sensitiveData = "${txn.method ?: ""}|${txn.detailBank ?: ""}|${txn.detailOnline ?: ""}|${txn.total}"
        val encrypted = CryptoManager.encrypt(sensitiveData)
        val securedTxn = txn.copy(
            encryptedPayload = encrypted,
            lastModified = System.currentTimeMillis()
        )
        dao.updateTransaction(securedTxn)
    }

    suspend fun deleteTransaction(id: String) {
        dao.deleteTransactionById(id)
        dao.deletePaymentsByTxnId(id)
    }

    // --- Customer Payments ---
    val allCustomerPayments: Flow<List<CustomerPaymentEntity>> = dao.getAllCustomerPayments()
        .map { list ->
            list.map { payment ->
                if (!payment.encryptedDetail.isNullOrEmpty()) {
                    val decrypted = CryptoManager.decrypt(payment.encryptedDetail)
                    payment.copy(detail = decrypted)
                } else payment
            }
        }

    fun getPaymentsForTransaction(txnId: String): Flow<List<CustomerPaymentEntity>> =
        dao.getPaymentsForTransaction(txnId)

    suspend fun recordCustomerPayment(payment: CustomerPaymentEntity) {
        val encrypted = CryptoManager.encrypt(payment.detail)
        val secured = payment.copy(
            encryptedDetail = encrypted,
            lastModified = System.currentTimeMillis()
        )
        dao.insertPayment(secured)
    }

    suspend fun deletePayment(payment: CustomerPaymentEntity) =
        dao.deletePayment(payment)

    // --- Ledger Entries (Paint, Raw Material, Scrap, Custom) ---
    fun getLedgerEntriesByType(type: String): Flow<List<LedgerEntryEntity>> =
        dao.getLedgerEntriesByType(type)

    fun getLedgerEntriesForParty(type: String, partyName: String): Flow<List<LedgerEntryEntity>> =
        dao.getLedgerEntriesForParty(type, partyName)

    suspend fun saveLedgerEntry(entry: LedgerEntryEntity) {
        val encrypted = if (!entry.encryptedNotes.isNullOrEmpty()) {
            CryptoManager.encrypt(entry.encryptedNotes)
        } else null
        val secured = entry.copy(
            encryptedNotes = encrypted,
            lastModified = System.currentTimeMillis()
        )
        dao.insertLedgerEntry(secured)
    }

    suspend fun updateLedgerEntry(entry: LedgerEntryEntity) =
        dao.updateLedgerEntry(entry)

    suspend fun deleteLedgerEntry(id: String) =
        dao.deleteLedgerEntryById(id)

    suspend fun deleteLedgerEntriesForParty(type: String, partyName: String) =
        dao.deleteLedgerEntriesForParty(type, partyName)

    // --- Workers & Labour Entries ---
    val allWorkers: Flow<List<WorkerEntity>> = dao.getAllWorkers()

    suspend fun saveWorker(worker: WorkerEntity): Long =
        dao.insertWorker(worker)

    suspend fun updateWorker(worker: WorkerEntity) =
        dao.updateWorker(worker)

    suspend fun deleteWorker(name: String) =
        dao.deleteWorkerByName(name)

    val allLabourEntries: Flow<List<LabourEntryEntity>> = dao.getAllLabourEntries()

    fun getLabourEntriesForWorker(name: String): Flow<List<LabourEntryEntity>> =
        dao.getLabourEntriesForWorker(name)

    suspend fun saveLabourEntry(entry: LabourEntryEntity) {
        val encrypted = if (entry.note.isNotEmpty()) CryptoManager.encrypt(entry.note) else null
        val secured = entry.copy(
            encryptedNotes = encrypted,
            lastModified = System.currentTimeMillis()
        )
        dao.insertLabourEntry(secured)
    }

    suspend fun deleteLabourEntry(id: String) =
        dao.deleteLabourEntryById(id)

    // --- Expenses ---
    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()

    suspend fun saveExpense(expense: ExpenseEntity) =
        dao.insertExpense(expense.copy(lastModified = System.currentTimeMillis()))

    suspend fun deleteExpense(id: String) =
        dao.deleteExpenseById(id)

    // --- Withdrawals ---
    val allWithdrawals: Flow<List<WithdrawalEntity>> = dao.getAllWithdrawals()

    suspend fun saveWithdrawal(withdrawal: WithdrawalEntity) =
        dao.insertWithdrawal(withdrawal.copy(lastModified = System.currentTimeMillis()))

    suspend fun deleteWithdrawal(id: String) =
        dao.deleteWithdrawalById(id)

    // --- Product Returns ---
    val allProductReturns: Flow<List<ProductReturnEntity>> = dao.getAllProductReturns()

    suspend fun saveProductReturn(ret: ProductReturnEntity) =
        dao.insertProductReturn(ret.copy(lastModified = System.currentTimeMillis()))

    suspend fun updateProductReturn(ret: ProductReturnEntity) =
        dao.updateProductReturn(ret.copy(lastModified = System.currentTimeMillis()))

    suspend fun deleteProductReturn(id: String) =
        dao.deleteProductReturnById(id)

    // --- App Settings ---
    val allSettings: Flow<List<AppSettingEntity>> = dao.getAllSettings()

    suspend fun getSetting(key: String): String? =
        dao.getSetting(key)?.value

    suspend fun setSetting(key: String, value: String) =
        dao.setSetting(AppSettingEntity(key, value))

    // --- Sync Queue ---
    suspend fun getPendingSyncQueue() = dao.getPendingSyncQueue()
    fun getPendingSyncQueueCount(): Flow<Int> = dao.getPendingSyncQueueCount()
    fun getAllPendingSyncQueue(): Flow<List<SyncQueueEntity>> = dao.getAllPendingSyncQueue()
    fun getPendingTransactionsCount(): Flow<Int> = dao.getPendingTransactionsCount()
    suspend fun enqueueSync(item: SyncQueueEntity) = dao.enqueueSync(item)
    suspend fun removeSyncQueueItem(id: Long) = dao.removeSyncQueueItem(id)
    suspend fun clearSyncQueue() = dao.clearSyncQueue()
    suspend fun markAllPendingSynced() {
        dao.clearSyncQueue()
        dao.markAllTransactionsSynced()
        dao.markAllInventoryMovementsSynced()
    }

    // --- Inventory Movements & Physical Audit ---
    val allInventoryMovements: Flow<List<InventoryMovementEntity>> = dao.getAllInventoryMovements()

    fun getMovementsForProduct(productId: Long): Flow<List<InventoryMovementEntity>> =
        dao.getMovementsForProduct(productId)

    fun getRecentInventoryMovements(limit: Int = 50): Flow<List<InventoryMovementEntity>> =
        dao.getRecentInventoryMovements(limit)

    fun getMovementsByType(type: String): Flow<List<InventoryMovementEntity>> =
        dao.getMovementsByType(type)

    suspend fun recordInventoryMovement(movement: InventoryMovementEntity): Long =
        dao.insertInventoryMovement(movement)

    suspend fun deleteInventoryMovement(id: Long) =
        dao.deleteInventoryMovementById(id)

    suspend fun adjustProductStock(
        product: ProductEntity,
        newStock: Int,
        movementType: String = "ADJUSTMENT",
        reason: String = "Physical Stock Count",
        referenceId: String? = null,
        note: String? = null,
        date: String,
        time: String
    ) {
        val prevStock = product.stock ?: 0
        val delta = newStock - prevStock
        dao.updateStock(product.id, newStock)
        dao.insertInventoryMovement(
            InventoryMovementEntity(
                productId = product.id,
                productName = product.name,
                movementType = movementType,
                quantityChange = delta,
                previousStock = prevStock,
                newStock = newStock,
                referenceId = referenceId ?: reason,
                note = note ?: "Adjusted on device via local Room database",
                date = date,
                time = time,
                syncStatus = "pending"
            )
        )
    }

    suspend fun restockProduct(
        product: ProductEntity,
        addedQty: Int,
        referenceId: String? = null,
        note: String? = null,
        date: String,
        time: String
    ) {
        val prevStock = product.stock ?: 0
        val newStock = prevStock + addedQty
        dao.incrementStock(product.id, addedQty)
        dao.insertInventoryMovement(
            InventoryMovementEntity(
                productId = product.id,
                productName = product.name,
                movementType = "RESTOCK",
                quantityChange = addedQty,
                previousStock = prevStock,
                newStock = newStock,
                referenceId = referenceId ?: "Batch Restock",
                note = note ?: "Restocked locally on device",
                date = date,
                time = time,
                syncStatus = "pending"
            )
        )
    }
}
