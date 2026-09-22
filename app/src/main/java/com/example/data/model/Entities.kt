package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val cat: String,
    val price: Double,
    val color: String? = null,
    val size: String? = null,
    val weight: String? = null,
    val stock: Int? = null,
    val reorderLevel: Int? = 5,
    val recipeJson: String? = null, // JSON list of ingredients consumed per unit
    val device: String? = null
) {
    val isLowStock: Boolean
        get() = stock != null && stock <= (reorderLevel ?: 5)
}

@Entity(tableName = "factories")
data class FactoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String,
    val contact: String = "—"
)

@Entity(tableName = "order_transactions")
data class OrderTransactionEntity(
    @PrimaryKey val id: String, // e.g. "0001"
    val date: String,
    val time: String,
    val itemsSummary: String,
    val itemCount: Int,
    val itemCounts: String? = null,
    val itemRates: String? = null,
    val itemProductIds: String? = null,
    val total: Double,
    val factory: String? = null,
    val confirmed: Boolean = false,
    val delivered: Boolean = false,
    val sizes: String? = null,
    val colors: String? = null,
    val paid: Boolean = false,
    val method: String? = null,
    val detailBank: String? = null,
    val detailOnline: String? = null,
    val detailCash: String? = null,
    val isJobWork: Boolean = false,
    val writtenOff: Double = 0.0,
    val device: String? = null,
    val receiptUrl: String? = null,
    // End-to-end encrypted payload for security audits
    val encryptedPayload: String? = null,
    val syncStatus: String = "synced", // "synced", "pending", "failed"
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "customer_payments")
data class CustomerPaymentEntity(
    @PrimaryKey val id: String,
    val txnId: String,
    val date: String,
    val time: String,
    val amount: Double,
    val method: String = "Cash",
    val detail: String? = null,
    val receivedBy: String? = null,
    val receivedIn: String? = null,
    val receiptUrl: String? = null,
    // Sensitive payment credentials encrypted via AES-GCM
    val encryptedDetail: String? = null,
    val device: String? = null,
    val syncStatus: String = "synced",
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "ledger_entries")
data class LedgerEntryEntity(
    @PrimaryKey val id: String,
    val ledgerType: String, // "CUSTOMER", "PAINT", "RAW_MATERIAL", "SCRAP", "CUSTOM_FACTORY"
    val partyName: String,
    val date: String,
    val time: String = "",
    val desc: String = "",
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val method: String = "",
    val detail: String = "",
    val chequeDate: String = "",
    val chequeStatus: String = "", // "pending", "cleared", "bounced"
    val receivedBy: String = "",
    val receivedIn: String = "",
    val color: String = "",
    val itemSize: String = "",
    val itemType: String = "",
    val itemFactory: String = "",
    val itemCount: String = "",
    val ratePerItem: String = "",
    val weight: String = "",
    val weightIn: Double? = null,
    val itemsIn: Double? = null,
    val isReturn: Boolean = false,
    val rateType: String = "",
    val rate: Double? = null,
    val bundleCount: String = "",
    val gaugeCount: String = "",
    val sizeCount: String = "",
    val stockName: String = "",
    val itemName: String = "",
    val scrapType: String = "",
    val itemColour: String = "",
    val itemMethod: String = "",
    val itemWeightStatus: String = "",
    val selfWeightStock: String = "",
    val weightPerItem: Double = 0.0,
    val weightPerScrap: Double = 0.0,
    val remainingWeight: String = "",
    val taxPercent: Double = 0.0,
    val taxAmt: Double = 0.0,
    val receiptUrl: String? = null,
    val encryptedNotes: String? = null,
    val device: String? = null,
    val syncStatus: String = "synced",
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "labour_workers")
data class WorkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val workType: String = "",
    val rateType: String = "daily", // "daily", "piece", "hourly"
    val rate: Double = 0.0,
    val pieceRatesJson: String = "",
    val startDate: String = "",
    val endDate: String = ""
)

@Entity(tableName = "labour_entries")
data class LabourEntryEntity(
    @PrimaryKey val id: String,
    val workerName: String,
    val date: String,
    val time: String = "",
    val kind: String = "attendance", // "attendance", "payment", "advance", "loan", "damage"
    val status: String = "present", // "present", "half", "absent", "leave"
    val size: String = "",
    val units: Double = 0.0,
    val note: String = "",
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val method: String = "Cash",
    val detail: String = "",
    val chequeDate: String = "",
    val chequeStatus: String = "",
    val receivedBy: String = "",
    val receivedIn: String = "",
    val workType: String = "",
    val receiptUrl: String? = null,
    val encryptedNotes: String? = null,
    val device: String? = null,
    val syncStatus: String = "synced",
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val date: String,
    val desc: String,
    val category: String,
    val amount: Double,
    val method: String = "Cash",
    val detail: String = "",
    val receiptUrl: String? = null,
    val device: String? = null,
    val syncStatus: String = "synced",
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey val id: String,
    val date: String,
    val desc: String,
    val amount: Double,
    val method: String = "Cash",
    val detail: String = "",
    val chequeDate: String = "",
    val chequeStatus: String = "",
    val withdrawnBy: String = "",
    val withdrawnIn: String = "",
    val isReversal: Boolean = false,
    val device: String? = null,
    val syncStatus: String = "synced",
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "product_returns")
data class ProductReturnEntity(
    @PrimaryKey val id: String,
    val date: String,
    val factory: String,
    val product: String,
    val quantity: Double,
    val reason: String = "",
    val originalOrderId: String = "",
    val status: String = "pending", // "pending", "resolved"
    val resolution: String = "", // "reworked", "scrapped"
    val reworkCost: Double = 0.0,
    val billingChoice: String = "credit",
    val refundAmount: Double = 0.0,
    val destinationChoice: String = "factory",
    val extraCustomerMaterial: Double = 0.0,
    val compensationRs: Double = 0.0,
    val compensationWeight: Double = 0.0,
    val device: String? = null,
    val syncStatus: String = "synced",
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: String,
    val action: String, // "UPSERT", "DELETE"
    val payloadJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "inventory_movements")
data class InventoryMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val movementType: String, // "SALE", "RESTOCK", "DAMAGE", "ADJUSTMENT", "RETURN", "INITIAL_STOCK"
    val quantityChange: Int, // e.g. -5 for sale, +20 for restock
    val previousStock: Int,
    val newStock: Int,
    val referenceId: String? = null, // e.g. "Order #0001" or "Physical Audit"
    val note: String? = null,
    val date: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val device: String? = null,
    val syncStatus: String = "synced"
)

data class RecipeItem(
    val material: String,
    val weightPerUnit: Double? = null,
    val itemsPerUnit: Double? = null,
    val customerSupplied: Boolean = false
)

// Convenient type aliases for Room entities
typealias TransactionEntity = OrderTransactionEntity
typealias InventoryEntity = InventoryMovementEntity

