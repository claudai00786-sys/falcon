package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.dao.PosDao
import com.example.data.database.AppDatabase
import com.example.data.model.InventoryEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.OrderTransactionEntity
import com.example.data.model.ProductEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabasePersistenceTest {

    private lateinit var database: AppDatabase
    private lateinit var posDao: PosDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        posDao = database.posDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testProductEntityPersistence() = runBlocking {
        val product = ProductEntity(
            name = "Test Ceiling Rod",
            cat = "Rods",
            price = 450.0,
            color = "White",
            size = "24 inch",
            weight = "1.2 kg",
            stock = 15,
            reorderLevel = 5
        )

        val insertedId = posDao.insertProduct(product)
        assertTrue(insertedId > 0)

        val fetched = posDao.getProductById(insertedId)
        assertNotNull(fetched)
        assertEquals("Test Ceiling Rod", fetched?.name)
        assertEquals(450.0, fetched?.price ?: 0.0, 0.001)
        assertEquals(15, fetched?.stock)
        assertFalse(fetched?.isLowStock ?: true)

        // Decrement stock and verify low stock detection
        posDao.decrementStock(insertedId, 12)
        val afterDecrement = posDao.getProductById(insertedId)
        assertEquals(3, afterDecrement?.stock)
        assertTrue(afterDecrement?.isLowStock ?: false)

        val lowStockList = posDao.getLowStockProducts().first()
        assertTrue(lowStockList.any { it.id == insertedId })
    }

    @Test
    fun testTransactionEntityPersistence() = runBlocking {
        val transaction: TransactionEntity = OrderTransactionEntity(
            id = "TXN-9001",
            date = "18/09/2026",
            time = "10:30 AM",
            itemsSummary = "Test Ceiling Rod x 2",
            itemCount = 2,
            total = 900.0,
            factory = "Lahore Fan Mills",
            confirmed = true,
            delivered = false,
            syncStatus = "pending"
        )

        posDao.insertTransaction(transaction)

        val fetched = posDao.getTransactionById("TXN-9001")
        assertNotNull(fetched)
        assertEquals("Lahore Fan Mills", fetched?.factory)
        assertEquals(900.0, fetched?.total ?: 0.0, 0.001)
        assertEquals("pending", fetched?.syncStatus)

        // Update sync status
        posDao.updateTransactionSyncStatus("TXN-9001", "synced")
        val updated = posDao.getTransactionById("TXN-9001")
        assertEquals("synced", updated?.syncStatus)
    }

    @Test
    fun testInventoryMovementEntityPersistence() = runBlocking {
        val movement: InventoryEntity = InventoryMovementEntity(
            productId = 101L,
            productName = "Fancy Tikka Tala",
            movementType = "RESTOCK",
            quantityChange = 50,
            previousStock = 10,
            newStock = 60,
            referenceId = "Batch #2026-09",
            note = "Direct factory arrival",
            date = "18/09/2026",
            time = "11:00 AM",
            syncStatus = "synced"
        )

        val movementId = posDao.insertInventoryMovement(movement)
        assertTrue(movementId > 0)

        val movementsForProduct = posDao.getMovementsForProduct(101L).first()
        assertEquals(1, movementsForProduct.size)
        assertEquals("RESTOCK", movementsForProduct[0].movementType)
        assertEquals(50, movementsForProduct[0].quantityChange)
        assertEquals(60, movementsForProduct[0].newStock)

        val restockMovements = posDao.getMovementsByType("RESTOCK").first()
        assertTrue(restockMovements.any { it.id == movementId })
    }

    @Test
    fun testOfflinePendingSyncQueue() = runBlocking {
        // Initially 0 pending
        val initialQueueCount = posDao.getPendingSyncQueueCount().first()
        assertEquals(0, initialQueueCount)

        // Add 2 pending sync queue entries
        posDao.enqueueSync(com.example.data.model.SyncQueueEntity(entityType = "ORDER", entityId = "ORD-001", action = "INSERT", payloadJson = "{}"))
        posDao.enqueueSync(com.example.data.model.SyncQueueEntity(entityType = "PAYMENT", entityId = "PAY-002", action = "INSERT", payloadJson = "{}"))

        val queueCount = posDao.getPendingSyncQueueCount().first()
        assertEquals(2, queueCount)

        // Insert pending transactions
        posDao.insertTransaction(OrderTransactionEntity(id = "ORD-001", date = "18/09/2026", time = "12:00 PM", itemsSummary = "Item", itemCount = 1, total = 500.0, factory = "Factory", syncStatus = "pending"))
        posDao.insertTransaction(OrderTransactionEntity(id = "ORD-002", date = "18/09/2026", time = "12:05 PM", itemsSummary = "Item", itemCount = 1, total = 300.0, factory = "Factory", syncStatus = "pending"))

        val pendingTxnCount = posDao.getPendingTransactionsCount().first()
        assertEquals(2, pendingTxnCount)

        // Simulate successful sync transmission
        posDao.markAllTransactionsSynced()
        posDao.clearSyncQueue()

        val afterSyncQueueCount = posDao.getPendingSyncQueueCount().first()
        val afterSyncTxnCount = posDao.getPendingTransactionsCount().first()
        assertEquals(0, afterSyncQueueCount)
        assertEquals(0, afterSyncTxnCount)
    }
}
