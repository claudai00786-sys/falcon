package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {

    // --- Products ---
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE cat = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR cat LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock IS NOT NULL AND stock <= COALESCE(reorderLevel, 5) ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("UPDATE products SET stock = stock - :qty WHERE id = :id AND stock IS NOT NULL")
    suspend fun decrementStock(id: Long, qty: Int)

    @Query("UPDATE products SET stock = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int)

    @Query("UPDATE products SET stock = COALESCE(stock, 0) + :qty WHERE id = :id")
    suspend fun incrementStock(id: Long, qty: Int)

    // --- Factories ---
    @Query("SELECT * FROM factories ORDER BY name ASC")
    fun getAllFactories(): Flow<List<FactoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactories(factories: List<FactoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactory(factory: FactoryEntity): Long

    @Update
    suspend fun updateFactory(factory: FactoryEntity)

    @Delete
    suspend fun deleteFactory(factory: FactoryEntity)

    @Query("DELETE FROM factories WHERE name = :name")
    suspend fun deleteFactoryByName(name: String)

    // --- Orders & Transactions ---
    @Query("SELECT * FROM order_transactions ORDER BY date DESC, time DESC")
    fun getAllTransactions(): Flow<List<OrderTransactionEntity>>

    @Query("SELECT * FROM order_transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): OrderTransactionEntity?

    @Query("SELECT * FROM order_transactions WHERE factory = :factoryName ORDER BY id ASC")
    fun getTransactionsByFactory(factoryName: String): Flow<List<OrderTransactionEntity>>

    @Query("SELECT * FROM order_transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, time DESC")
    fun getTransactionsByDateRange(startDate: String, endDate: String): Flow<List<OrderTransactionEntity>>

    @Query("SELECT * FROM order_transactions WHERE syncStatus = :syncStatus")
    suspend fun getTransactionsBySyncStatus(syncStatus: String): List<OrderTransactionEntity>

    @Query("UPDATE order_transactions SET syncStatus = :status WHERE id = :id")
    suspend fun updateTransactionSyncStatus(id: String, status: String)

    @Query("UPDATE order_transactions SET syncStatus = 'synced' WHERE syncStatus = 'pending'")
    suspend fun markAllTransactionsSynced()

    @Query("UPDATE inventory_movements SET syncStatus = 'synced' WHERE syncStatus = 'pending'")
    suspend fun markAllInventoryMovementsSynced()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<OrderTransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: OrderTransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: OrderTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: OrderTransactionEntity)

    @Query("DELETE FROM order_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    // --- Customer Payments ---
    @Query("SELECT * FROM customer_payments ORDER BY date DESC, time DESC")
    fun getAllCustomerPayments(): Flow<List<CustomerPaymentEntity>>

    @Query("SELECT * FROM customer_payments WHERE txnId = :txnId ORDER BY date DESC, time DESC")
    fun getPaymentsForTransaction(txnId: String): Flow<List<CustomerPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<CustomerPaymentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: CustomerPaymentEntity)

    @Delete
    suspend fun deletePayment(payment: CustomerPaymentEntity)

    @Query("DELETE FROM customer_payments WHERE txnId = :txnId")
    suspend fun deletePaymentsByTxnId(txnId: String)

    // --- Ledger Entries ---
    @Query("SELECT * FROM ledger_entries WHERE ledgerType = :ledgerType ORDER BY date ASC, time ASC")
    fun getLedgerEntriesByType(ledgerType: String): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE ledgerType = :ledgerType AND partyName = :partyName ORDER BY date ASC, time ASC")
    fun getLedgerEntriesForParty(ledgerType: String, partyName: String): Flow<List<LedgerEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntries(entries: List<LedgerEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: LedgerEntryEntity)

    @Update
    suspend fun updateLedgerEntry(entry: LedgerEntryEntity)

    @Delete
    suspend fun deleteLedgerEntry(entry: LedgerEntryEntity)

    @Query("DELETE FROM ledger_entries WHERE id = :id")
    suspend fun deleteLedgerEntryById(id: String)

    @Query("DELETE FROM ledger_entries WHERE ledgerType = :ledgerType AND partyName = :partyName")
    suspend fun deleteLedgerEntriesForParty(ledgerType: String, partyName: String)

    // --- Labour Workers & Entries ---
    @Query("SELECT * FROM labour_workers ORDER BY name ASC")
    fun getAllWorkers(): Flow<List<WorkerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkers(workers: List<WorkerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: WorkerEntity): Long

    @Update
    suspend fun updateWorker(worker: WorkerEntity)

    @Delete
    suspend fun deleteWorker(worker: WorkerEntity)

    @Query("DELETE FROM labour_workers WHERE name = :name")
    suspend fun deleteWorkerByName(name: String)

    @Query("SELECT * FROM labour_entries ORDER BY date ASC, time ASC")
    fun getAllLabourEntries(): Flow<List<LabourEntryEntity>>

    @Query("SELECT * FROM labour_entries WHERE workerName = :workerName ORDER BY date ASC, time ASC")
    fun getLabourEntriesForWorker(workerName: String): Flow<List<LabourEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabourEntries(entries: List<LabourEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabourEntry(entry: LabourEntryEntity)

    @Delete
    suspend fun deleteLabourEntry(entry: LabourEntryEntity)

    @Query("DELETE FROM labour_entries WHERE id = :id")
    suspend fun deleteLabourEntryById(id: String)

    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: String)

    // --- Withdrawals ---
    @Query("SELECT * FROM withdrawals ORDER BY date DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawals(withdrawals: List<WithdrawalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity)

    @Delete
    suspend fun deleteWithdrawal(withdrawal: WithdrawalEntity)

    @Query("DELETE FROM withdrawals WHERE id = :id")
    suspend fun deleteWithdrawalById(id: String)

    // --- Product Returns ---
    @Query("SELECT * FROM product_returns ORDER BY date DESC")
    fun getAllProductReturns(): Flow<List<ProductReturnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductReturns(returns: List<ProductReturnEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductReturn(productReturn: ProductReturnEntity)

    @Update
    suspend fun updateProductReturn(productReturn: ProductReturnEntity)

    @Delete
    suspend fun deleteProductReturn(productReturn: ProductReturnEntity)

    @Query("DELETE FROM product_returns WHERE id = :id")
    suspend fun deleteProductReturnById(id: String)

    // --- App Settings ---
    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): AppSettingEntity?

    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)

    // --- Offline Sync Queue ---
    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    suspend fun getPendingSyncQueue(): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getPendingSyncQueueCount(): Flow<Int>

    @Query("SELECT * FROM sync_queue ORDER BY timestamp DESC")
    fun getAllPendingSyncQueue(): Flow<List<SyncQueueEntity>>

    @Query("SELECT COUNT(*) FROM order_transactions WHERE syncStatus = 'pending'")
    fun getPendingTransactionsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueSync(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun removeSyncQueueItem(id: Long)

    @Query("DELETE FROM sync_queue")
    suspend fun clearSyncQueue()

    // --- Inventory Movements & Tracking ---
    @Query("SELECT * FROM inventory_movements ORDER BY timestamp DESC")
    fun getAllInventoryMovements(): Flow<List<InventoryMovementEntity>>

    @Query("SELECT * FROM inventory_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<InventoryMovementEntity>>

    @Query("SELECT * FROM inventory_movements ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentInventoryMovements(limit: Int = 50): Flow<List<InventoryMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryMovement(movement: InventoryMovementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryMovements(movements: List<InventoryMovementEntity>)

    @Query("SELECT * FROM inventory_movements WHERE movementType = :type ORDER BY timestamp DESC")
    fun getMovementsByType(type: String): Flow<List<InventoryMovementEntity>>

    @Query("DELETE FROM inventory_movements WHERE id = :id")
    suspend fun deleteInventoryMovementById(id: Long)
}
