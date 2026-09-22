package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.PosDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        FactoryEntity::class,
        OrderTransactionEntity::class,
        CustomerPaymentEntity::class,
        LedgerEntryEntity::class,
        WorkerEntity::class,
        LabourEntryEntity::class,
        ExpenseEntity::class,
        WithdrawalEntity::class,
        ProductReturnEntity::class,
        AppSettingEntity::class,
        SyncQueueEntity::class,
        InventoryMovementEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `inventory_movements` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `productId` INTEGER NOT NULL,
                        `productName` TEXT NOT NULL,
                        `movementType` TEXT NOT NULL,
                        `quantityChange` INTEGER NOT NULL,
                        `previousStock` INTEGER NOT NULL,
                        `newStock` INTEGER NOT NULL,
                        `referenceId` TEXT,
                        `note` TEXT,
                        `date` TEXT NOT NULL,
                        `time` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `device` TEXT,
                        `syncStatus` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "falcon_pos_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.posDao())
                }
            }
        }

        suspend fun populateDatabase(dao: PosDao) {
            // Prepopulate Initial Settings
            val initialSettings = listOf(
                AppSettingEntity("pos_pin", "321"),
                AppSettingEntity("pos_recovery_answer", "amir"),
                AppSettingEntity("pos_device_name", "Shop Counter"),
                AppSettingEntity("pos_autolock_minutes", "5"),
                AppSettingEntity("pos_language", "en"),
                AppSettingEntity("pos_theme", "dark"),
                AppSettingEntity("pos_logo_theme", "amber"),
                AppSettingEntity("pos_ntn", "1234567-8"),
                AppSettingEntity("pos_sales_tax_reg", "12-34-5678-901-23"),
                AppSettingEntity("pos_company_name", "Falcon Rod Maker"),
                AppSettingEntity("pos_company_tagline", "Fan Accessories · Gujrat"),
                AppSettingEntity("pos_company_phone", "0300-1234567"),
                AppSettingEntity("pos_company_address", "Circular Road, Gujrat, Pakistan")
            )
            initialSettings.forEach { dao.setSetting(it) }

            // Prepopulate Factories
            val initialFactories = listOf(
                FactoryEntity(name = "Alhmad Fan (Basharat SB)", location = "Shad Bagh, Lahore", contact = "0300-1234567"),
                FactoryEntity(name = "Hazma Solar Fan", location = "G.T Road, Gujrat", contact = "0321-7654321"),
                FactoryEntity(name = "Royal Fan Workshop", location = "Small Industrial Estate, Gujrat", contact = "0333-9876543")
            )
            dao.insertFactories(initialFactories)

            // Prepopulate Fan Rod Catalog
            val initialProducts = listOf(
                ProductEntity(
                    name = "Ceiling-Fan-Rod-18-Deluxe",
                    cat = "Rod (Ceiling)",
                    price = 420.0,
                    size = "18 inch",
                    weight = "0.85 kg",
                    color = "Black",
                    stock = 45,
                    reorderLevel = 10,
                    recipeJson = """[{"material":"M.S. Steel Pipe","weightPerUnit":0.8,"itemsPerUnit":null,"customerSupplied":false},{"material":"Safety Bolt & Cotter Pin","weightPerUnit":null,"itemsPerUnit":1.0,"customerSupplied":false},{"material":"Rubber Bushing","weightPerUnit":null,"itemsPerUnit":1.0,"customerSupplied":false}]"""
                ),
                ProductEntity(
                    name = "Ceiling-Fan-Rod-24-Heavy",
                    cat = "Rod (Ceiling)",
                    price = 540.0,
                    size = "24 inch",
                    weight = "1.15 kg",
                    color = "Matt Black",
                    stock = 30,
                    reorderLevel = 10,
                    recipeJson = """[{"material":"M.S. Steel Pipe","weightPerUnit":1.1,"itemsPerUnit":null,"customerSupplied":false},{"material":"Safety Bolt & Cotter Pin","weightPerUnit":null,"itemsPerUnit":1.0,"customerSupplied":false}]"""
                ),
                ProductEntity(
                    name = "Ceiling-Fan-Rod-36-Industrial",
                    cat = "Rod (Ceiling)",
                    price = 780.0,
                    size = "36 inch",
                    weight = "1.85 kg",
                    color = "Smoke Grey",
                    stock = 25,
                    reorderLevel = 8,
                    recipeJson = """[{"material":"M.S. Heavy Pipe","weightPerUnit":1.8,"itemsPerUnit":null,"customerSupplied":false},{"material":"Safety Bolt & Cotter Pin","weightPerUnit":null,"itemsPerUnit":1.0,"customerSupplied":false}]"""
                ),
                ProductEntity(
                    name = "Pedestal-Fan-Extension-Rod-20",
                    cat = "Rod (Pedestal)",
                    price = 480.0,
                    size = "20 inch",
                    weight = "0.95 kg",
                    color = "White",
                    stock = 35,
                    reorderLevel = 10,
                    recipeJson = """[{"material":"M.S. Steel Pipe","weightPerUnit":0.9,"itemsPerUnit":null,"customerSupplied":false}]"""
                ),
                ProductEntity(
                    name = "Bracket-Fan-Mounting-Rod-16",
                    cat = "Rod (Bracket)",
                    price = 390.0,
                    size = "16 inch",
                    weight = "0.75 kg",
                    color = "Silver",
                    stock = 40,
                    reorderLevel = 12,
                    recipeJson = """[{"material":"M.S. Steel Pipe","weightPerUnit":0.7,"itemsPerUnit":null,"customerSupplied":false}]"""
                )
            )
            dao.insertProducts(initialProducts)

            // Prepopulate Raw Material initial ledger and stock entries
            val initialRawEntries = listOf(
                LedgerEntryEntity(
                    id = "rl_init_1",
                    ledgerType = "RAW_MATERIAL",
                    partyName = "M.S. Pipe Mills (Gujranwala)",
                    date = "15/09/2026",
                    desc = "Purchase of M.S. Steel Pipe",
                    stockName = "M.S. Steel Pipe",
                    weightIn = 250.0,
                    rate = 290.0,
                    debit = 0.0,
                    credit = 72500.0,
                    method = "Bank",
                    detail = "HBL-9921",
                    receivedBy = "Amir",
                    receivedIn = "Warehouse 1"
                ),
                LedgerEntryEntity(
                    id = "rl_init_2",
                    ledgerType = "RAW_MATERIAL",
                    partyName = "Hardware & Fittings Vendor",
                    date = "15/09/2026",
                    desc = "Safety Bolt & Cotter Pin Delivery",
                    stockName = "Safety Bolt & Cotter Pin",
                    itemsIn = 500.0,
                    rate = 12.0,
                    debit = 0.0,
                    credit = 6000.0,
                    method = "Cash",
                    receivedBy = "Amir",
                    receivedIn = "Shop Counter"
                )
            )
            dao.insertLedgerEntries(initialRawEntries)

            // Prepopulate Painter Ledger
            val initialPainters = listOf(
                LedgerEntryEntity(
                    id = "pl_init_1",
                    ledgerType = "PAINT",
                    partyName = "Rashid Painter",
                    date = "14/09/2026",
                    desc = "Batch of 40 Ceiling Fan Rods powder coated",
                    itemSize = "18 inch",
                    itemType = "Ceiling-Fan-Rod-18-Deluxe",
                    color = "Black, Shine Black",
                    itemCount = "40",
                    ratePerItem = "45",
                    debit = 1500.0, // Advance paid
                    credit = 1800.0, // Work value
                    method = "Cash",
                    receivedBy = "Rashid",
                    receivedIn = "Workshop"
                )
            )
            dao.insertLedgerEntries(initialPainters)

            // Prepopulate Labour Workers
            val initialWorkers = listOf(
                WorkerEntity(name = "Rafiq", workType = "Pipe Threading", rateType = "daily", rate = 1200.0, startDate = "01/01/2026"),
                WorkerEntity(name = "Aslam", workType = "Swaging & Punching", rateType = "daily", rate = 1100.0, startDate = "01/01/2026"),
                WorkerEntity(name = "Tariq", workType = "Pipe Cutting", rateType = "piece", rate = 15.0, startDate = "15/01/2026")
            )
            dao.insertWorkers(initialWorkers)

            // Prepopulate Daily Expenses
            val initialExpenses = listOf(
                ExpenseEntity(id = "exp_1", date = "2026-09-15", desc = "Workshop Electricity Bill", category = "Electricity", amount = 14500.0, method = "Online", detail = "JazzCash"),
                ExpenseEntity(id = "exp_2", date = "2026-09-15", desc = "Staff Tea & Lunch", category = "Labour", amount = 650.0, method = "Cash"),
                ExpenseEntity(id = "exp_3", date = "2026-09-14", desc = "Rickshaw Transport of Raw Material", category = "Transport", amount = 1200.0, method = "Cash")
            )
            dao.insertExpenses(initialExpenses)

            // Prepopulate Sample Transactions / Booked Orders
            val initialTxns = listOf(
                OrderTransactionEntity(
                    id = "0001",
                    date = "15/09/2026",
                    time = "11:30 AM",
                    itemsSummary = "Ceiling-Fan-Rod-18-Deluxe",
                    itemCount = 10,
                    itemCounts = "10",
                    itemRates = "420",
                    itemProductIds = "1",
                    total = 4200.0,
                    factory = "Alhmad Fan (Basharat SB)",
                    confirmed = true,
                    delivered = true,
                    sizes = "18 inch",
                    colors = "Black",
                    paid = true,
                    method = "Bank",
                    detailBank = "MCB Cheque #49281"
                ),
                OrderTransactionEntity(
                    id = "0002",
                    date = "16/09/2026",
                    time = "09:45 AM",
                    itemsSummary = "Ceiling-Fan-Rod-24-Heavy\nPedestal-Fan-Extension-Rod-20",
                    itemCount = 8,
                    itemCounts = "4\n4",
                    itemRates = "540\n480",
                    itemProductIds = "2\n4",
                    total = 4080.0,
                    factory = "Hazma Solar Fan",
                    confirmed = true,
                    delivered = false,
                    sizes = "24 inch\n20 inch",
                    colors = "Matt Black\nWhite",
                    paid = false,
                    method = null
                )
            )
            dao.insertTransactions(initialTxns)

            // Prepopulate Customer Ledger record for Order 0001
            dao.insertLedgerEntry(
                LedgerEntryEntity(
                    id = "cl_order_0001",
                    ledgerType = "CUSTOMER",
                    partyName = "Alhmad Fan (Basharat SB)",
                    date = "15/09/2026",
                    time = "11:30 AM",
                    desc = "Order #0001 — Ceiling-Fan-Rod-18-Deluxe (18 inch, Black)",
                    debit = 4200.0,
                    credit = 4200.0,
                    method = "Bank",
                    detail = "Cheque #49281",
                    receivedBy = "Amir",
                    receivedIn = "HBL Main"
                )
            )
            dao.insertLedgerEntry(
                LedgerEntryEntity(
                    id = "cl_order_0002",
                    ledgerType = "CUSTOMER",
                    partyName = "Hazma Solar Fan",
                    date = "16/09/2026",
                    time = "09:45 AM",
                    desc = "Order #0002 — Ceiling-Fan-Rod-24-Heavy (24 inch, Matt Black); Pedestal-Fan-Extension-Rod-20 (20 inch, White)",
                    debit = 4080.0,
                    credit = 0.0
                )
            )

            // Prepopulate Initial Inventory Movements Audit Log in Room
            val initialMovements = listOf(
                InventoryMovementEntity(
                    productId = 1L,
                    productName = "Ceiling-Fan-Rod-18-Deluxe",
                    movementType = "INITIAL_STOCK",
                    quantityChange = 45,
                    previousStock = 0,
                    newStock = 45,
                    referenceId = "System Init",
                    note = "Opening physical inventory stock on device",
                    date = "15/09/2026",
                    time = "08:00 AM",
                    syncStatus = "synced"
                ),
                InventoryMovementEntity(
                    productId = 2L,
                    productName = "Ceiling-Fan-Rod-24-Heavy",
                    movementType = "INITIAL_STOCK",
                    quantityChange = 30,
                    previousStock = 0,
                    newStock = 30,
                    referenceId = "System Init",
                    note = "Opening physical inventory stock on device",
                    date = "15/09/2026",
                    time = "08:00 AM",
                    syncStatus = "synced"
                ),
                InventoryMovementEntity(
                    productId = 1L,
                    productName = "Ceiling-Fan-Rod-18-Deluxe",
                    movementType = "SALE",
                    quantityChange = -10,
                    previousStock = 45,
                    newStock = 35,
                    referenceId = "Order #0001",
                    note = "Sold to Alhmad Fan (Basharat SB)",
                    date = "15/09/2026",
                    time = "11:30 AM",
                    syncStatus = "synced"
                ),
                InventoryMovementEntity(
                    productId = 2L,
                    productName = "Ceiling-Fan-Rod-24-Heavy",
                    movementType = "INITIAL_STOCK",
                    quantityChange = 25,
                    previousStock = 0,
                    newStock = 25,
                    referenceId = "System Init",
                    note = "Opening inventory count",
                    date = "15/09/2026",
                    time = "08:00 AM",
                    syncStatus = "synced"
                ),
                InventoryMovementEntity(
                    productId = 2L,
                    productName = "Ceiling-Fan-Rod-24-Heavy",
                    movementType = "SALE",
                    quantityChange = -4,
                    previousStock = 25,
                    newStock = 21,
                    referenceId = "Order #0002",
                    note = "Sold to Hazma Solar Fan",
                    date = "16/09/2026",
                    time = "09:45 AM",
                    syncStatus = "synced"
                )
            )
            dao.insertInventoryMovements(initialMovements)
        }
    }
}
