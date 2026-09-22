package com.example.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Supported application languages.
 */
enum class AppLanguage(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val isRtl: Boolean
) {
    ENGLISH("en", "English", "English", false),
    URDU("ur", "Urdu", "اردو", true);

    fun toggle(): AppLanguage = if (this == ENGLISH) URDU else ENGLISH

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
    }
}

/**
 * Interface contract for state-based resource bundle dictionary.
 * Allows both typed property access and dynamic key-based indexing.
 */
interface ResourceBundleDictionary {
    val language: AppLanguage
    val rawDictionary: Map<String, String>

    operator fun get(key: String): String = rawDictionary[key] ?: key

    fun getString(key: String, vararg args: Any): String {
        val template = rawDictionary[key] ?: key
        return if (args.isEmpty()) {
            template
        } else {
            try {
                String.format(if (language == AppLanguage.URDU) Locale("ur", "PK") else Locale.US, template, *args)
            } catch (e: Exception) {
                template
            }
        }
    }

    // Formatting Helpers
    fun formatCurrency(amount: Double): String
    fun formatPcs(count: Number): String
    fun formatWeight(kg: Double): String
    fun translatePeriod(period: String): String
    fun translateCategory(category: String): String
    fun translateExpenseCategory(category: String): String

    // --- Global & Action Strings ---
    val brandName: String
    val brandSubtitle: String
    val searchEverything: String
    val lockApp: String
    val toggleTheme: String
    val updateLive: String
    val syncNow: String
    val search: String
    val clear: String
    val cancel: String
    val save: String
    val saveChanges: String
    val add: String
    val delete: String
    val edit: String
    val confirm: String
    val close: String
    val back: String
    val all: String
    val active: String
    val date: String
    val notes: String
    val amount: String
    val quantity: String
    val rate: String
    val total: String
    val pcs: String

    // --- Navigation & Menu ---
    val menuTitle: String
    val closeMenu: String
    val sectionOperations: String
    val sectionLedgers: String
    val sectionInventory: String
    val sectionSystem: String
    val navOverview: String
    val navCatalog: String
    val navOrders: String
    val navLedgers: String
    val navStock: String
    val navExpenses: String
    val navSettings: String

    // --- Periods ---
    val periodDaily: String
    val periodYesterday: String
    val periodWeekly: String
    val periodMonthly: String
    val periodYearly: String

    // --- Overview Screen ---
    val businessReportTitle: String
    val grossSales: String
    val ordersBooked: String
    val unitsSold: String
    val avgOrder: String
    val periodExpenses: String
    val materialPaid: String
    val labourAndPaint: String
    val withdrawals: String
    val netProfit: String
    val totalReceivables: String
    val totalPayables: String
    val netLedgerDiff: String
    val quickActions: String
    val actionNewOrder: String
    val actionRawMaterial: String
    val actionAddExpense: String
    val actionAttendance: String
    val actionReturns: String
    val factorySalesShare: String
    val outstandingBalances: String
    val allBalancesCleared: String
    val noSalesRecorded: String

    // --- Catalog Screen ---
    val searchProducts: String
    val searchCatalog: String
    val addProduct: String
    val editProduct: String
    val productName: String
    val category: String
    val priceRs: String
    val weightGrams: String
    val pipeType: String
    val addToCart: String
    val inCart: String
    val cartAndBooking: String
    val selectFactory: String
    val orderSummary: String
    val totalItems: String
    val grandTotal: String
    val bookOrder: String
    val clearCart: String
    val emptyCartMsg: String
    val pleaseSelectFactory: String
    val orderBookedSuccess: String
    val noProductsFound: String
    val currentInvoice: String
    val reviewOrder: String
    val factoryCustomer: String
    val totalAmount: String
    val bookUnpaidOrder: String
    val paidCheckout: String

    // --- Orders Booked (Transactions) ---
    val searchOrders: String
    val unpaidBalance: String
    val totalOrders: String
    val totalPcsSold: String
    val totalUnpaid: String
    val totalItemsSold: String
    val orderDetails: String
    val orderNumber: String
    val customerFactory: String
    val orderStatus: String
    val confirmed: String
    val delivered: String
    val awaitingDelivery: String
    val paid: String
    val unpaid: String
    val partial: String
    val markDelivered: String
    val recordPayment: String
    val shareInvoice: String
    val deleteOrder: String
    val paymentDialogTitle: String
    val paymentReceived: String
    val remainingBalance: String
    val remainingDue: String
    val savePayment: String
    val noOrdersFound: String
    val orderPaid: String
    fun orderPartial(dueStr: String = ""): String
    val orderUnpaid: String
    val nilDue: String
    val deliver: String
    val amountRs: String
    val receivedBy: String
    val receivedIn: String
    val nilFullBalance: String

    // --- Ledgers ---
    val tabCustomerFactories: String
    val tabPaint: String
    val tabRawMaterial: String
    val tabScrap: String
    val tabLabour: String
    val tabCustomFactory: String
    val addFactory: String
    val addWorker: String
    val markAttendance: String
    val addLedgerEntry: String
    val debitBilled: String
    val creditReceived: String
    val balanceDr: String
    val balanceCr: String
    val viewLedger: String
    val partyName: String
    val contactNumber: String
    val addressCity: String
    val openingBalance: String
    val workerType: String
    val dailyWage: String
    val perPieceRate: String
    val attendancePresent: String
    val attendanceAbsent: String
    val attendanceHalfDay: String
    val markAllPresent: String
    val saveAttendance: String
    val pipesCoilsSupplied: String
    val weightKg: String
    val scrapSold: String
    val scrapBuyer: String

    // --- Stock & Ready Inventory ---
    val stockReadyTitle: String
    val stockReadySubtitle: String
    val ordersReady: String
    val stockReadyValue: String
    val noStockReady: String
    val rawMaterialInventory: String
    val rawInventorySubtitle: String
    val scrapInventoryTitle: String
    val scrapSubtitle: String
    val lowStockWarning: String
    val available: String
    val consumed: String

    // --- Expenses & Withdrawals ---
    val operatingExpensesTitle: String
    val operatingExpensesSubtitle: String
    val withdrawalsTitle: String
    val withdrawalsSubtitle: String
    val totalExpenses: String
    val totalWithdrawals: String
    val addExpense: String
    val addWithdrawal: String
    val expenseCategory: String
    val expenseTitle: String
    val recordedBy: String

    // --- Returns ---
    val returnsTitle: String
    val returnsSubtitle: String
    val totalUnitsReturned: String
    val reworkVsScrap: String
    val logReturn: String
    val defectReason: String
    val resolution: String
    val sendForRework: String
    val sendToScrap: String
    val replacementSent: String
    val noReturnsLogged: String

    // --- Settings & Security ---
    val settingsTitle: String
    val securitySection: String
    val securitySubtitle: String
    val encryptionActive: String
    val encryptionSub: String
    val posPin: String
    val posPinSub: String
    val changePin: String
    val securityQuestion: String
    val securityQuestionSub: String
    val updateAnswer: String
    val appearanceSection: String
    val appearanceSubtitle: String
    val cloudSyncSection: String
    val cloudSyncSubtitle: String
    val lastSynced: String
    val never: String
    val dataExportSection: String
    val dataExportSubtitle: String
    val exportLedgers: String
    val languageSection: String
    val languageSubtitle: String
    val english: String
    val urdu: String

    // --- Lock Screen ---
    val enterPinPrompt: String
    val enterPinTitle: String
    val cashierUser: String
    val incorrectPin: String
    val forgotPin: String
    val defaultPinHint: String
    val recoveryTitle: String
    val recoveryQuestionText: String
    val enterAnswerPrompt: String
    val enterNewPinPrompt: String
    val confirmNewPinPrompt: String
    val resetPinButton: String
    val pinMismatch: String
    val incorrectAnswer: String
}

/**
 * English Resource Bundle implementation.
 */
object EnglishResourceBundle : ResourceBundleDictionary {
    override val language: AppLanguage = AppLanguage.ENGLISH

    override val rawDictionary: Map<String, String> = mapOf(
        "brand_name" to "Falcon Rod Maker",
        "brand_subtitle" to "FAN ACCESSORIES · GUJRAT",
        "search_everything" to "Search everything…",
        "lock_app" to "Lock POS",
        "toggle_theme" to "Toggle Theme",
        "update_live" to "Update Live",
        "sync_now" to "Sync Database",
        "search" to "Search…",
        "clear" to "Clear",
        "cancel" to "Cancel",
        "save" to "Save",
        "save_changes" to "Save Changes",
        "add" to "Add",
        "delete" to "Delete",
        "edit" to "Edit",
        "confirm" to "Confirm",
        "close" to "Close",
        "back" to "Back",
        "all" to "All",
        "active" to "Active",
        "date" to "Date",
        "notes" to "Notes / Remarks",
        "amount" to "Amount (Rs)",
        "quantity" to "Quantity",
        "rate" to "Rate",
        "total" to "Total",
        "pcs" to "pcs",
        "gross_sales" to "Period Gross Sales",
        "net_profit" to "Estimated Net Profit",
        "total_receivables" to "Total Receivables (Debtors)",
        "total_payables" to "Total Payables (Creditors)",
        "factory_sales_share" to "Factory Sales Share",
        "outstanding_balances" to "Outstanding Factory Balances",
        "all_balances_cleared" to "All factory balances are settled!",
        "quick_actions" to "Quick Action Hub",
        "action_new_order" to "New Order",
        "action_raw_material" to "Raw Material",
        "action_add_expense" to "Add Expense",
        "action_attendance" to "Attendance",
        "action_returns" to "Product Returns"
    )

    override fun formatCurrency(amount: Double): String {
        val formatted = String.format(Locale.US, "%,.0f", amount)
        return "Rs $formatted"
    }

    override fun formatPcs(count: Number): String = "$count pcs"
    override fun formatWeight(kg: Double): String = String.format(Locale.US, "%.1f kg", kg)

    override fun translatePeriod(period: String): String = when (period.lowercase()) {
        "daily" -> "Daily"
        "yesterday" -> "Yesterday"
        "weekly" -> "Weekly"
        "monthly" -> "Monthly"
        "yearly" -> "Yearly"
        else -> period
    }

    override fun translateCategory(category: String): String = category

    override fun translateExpenseCategory(category: String): String = category

    override val brandName: String = "Falcon Rod Maker"
    override val brandSubtitle: String = "FAN ACCESSORIES · GUJRAT"
    override val searchEverything: String = "Search everything…"
    override val lockApp: String = "Lock POS"
    override val toggleTheme: String = "Toggle Theme"
    override val updateLive: String = "Update Live"
    override val syncNow: String = "Sync Database"
    override val search: String = "Search…"
    override val clear: String = "Clear"
    override val cancel: String = "Cancel"
    override val save: String = "Save"
    override val saveChanges: String = "Save Changes"
    override val add: String = "Add"
    override val delete: String = "Delete"
    override val edit: String = "Edit"
    override val confirm: String = "Confirm"
    override val close: String = "Close"
    override val back: String = "Back"
    override val all: String = "All"
    override val active: String = "Active"
    override val date: String = "Date"
    override val notes: String = "Notes / Remarks"
    override val amount: String = "Amount (Rs)"
    override val quantity: String = "Quantity"
    override val rate: String = "Rate"
    override val total: String = "Total"
    override val pcs: String = "pcs"

    override val menuTitle: String = "Falcon POS Menu"
    override val closeMenu: String = "Close Menu"
    override val sectionOperations: String = "Sales & Orders"
    override val sectionLedgers: String = "Accounting & Ledgers"
    override val sectionInventory: String = "Inventory & Factory"
    override val sectionSystem: String = "System & Security"

    override val navOverview: String = "Overview"
    override val navCatalog: String = "Catalog"
    override val navOrders: String = "Orders"
    override val navLedgers: String = "Ledgers"
    override val navStock: String = "Stock"
    override val navExpenses: String = "Expenses"
    override val navSettings: String = "Settings"

    override val periodDaily: String = "Daily"
    override val periodYesterday: String = "Yesterday"
    override val periodWeekly: String = "Weekly"
    override val periodMonthly: String = "Monthly"
    override val periodYearly: String = "Yearly"

    override val businessReportTitle: String = "Business Performance Report"
    override val grossSales: String = "Period Gross Sales"
    override val ordersBooked: String = "Orders Booked"
    override val unitsSold: String = "Units Sold"
    override val avgOrder: String = "Avg Order"
    override val periodExpenses: String = "Period Expenses"
    override val materialPaid: String = "Material Paid"
    override val labourAndPaint: String = "Labour & Paint"
    override val withdrawals: String = "Owner Withdrawals"
    override val netProfit: String = "Estimated Net Profit"
    override val totalReceivables: String = "Total Receivables (Debtors)"
    override val totalPayables: String = "Total Payables (Creditors)"
    override val netLedgerDiff: String = "Net Balance (Receivables - Payables)"
    override val quickActions: String = "Quick Action Hub"
    override val actionNewOrder: String = "New Order"
    override val actionRawMaterial: String = "Raw Material"
    override val actionAddExpense: String = "Add Expense"
    override val actionAttendance: String = "Attendance"
    override val actionReturns: String = "Product Returns"
    override val factorySalesShare: String = "Factory Sales Share"
    override val outstandingBalances: String = "Outstanding Factory Balances"
    override val allBalancesCleared: String = "All factory balances are settled!"
    override val noSalesRecorded: String = "No orders recorded for this period."

    override val searchProducts: String = "Search fan accessories…"
    override val searchCatalog: String = "Search catalog…"
    override val addProduct: String = "Add Product"
    override val editProduct: String = "Edit Product"
    override val productName: String = "Product Name"
    override val category: String = "Category"
    override val priceRs: String = "Standard Price (Rs)"
    override val weightGrams: String = "Standard Weight (Grams)"
    override val pipeType: String = "Pipe / Metal Type"
    override val addToCart: String = "Add to Cart"
    override val inCart: String = "In Cart"
    override val cartAndBooking: String = "Cart & Order Booking"
    override val selectFactory: String = "Select Customer / Factory"
    override val orderSummary: String = "Order Summary"
    override val totalItems: String = "Total Items"
    override val grandTotal: String = "Grand Total"
    override val bookOrder: String = "Confirm & Book Order"
    override val clearCart: String = "Clear Cart"
    override val emptyCartMsg: String = "Your cart is empty. Select products from catalog."
    override val pleaseSelectFactory: String = "Please select a customer factory first!"
    override val orderBookedSuccess: String = "Order booked successfully!"
    override val noProductsFound: String = "No products found."
    override val currentInvoice: String = "Current Invoice"
    override val reviewOrder: String = "Review Order"
    override val factoryCustomer: String = "Customer / Factory"
    override val totalAmount: String = "Total Amount"
    override val bookUnpaidOrder: String = "Book Unpaid Order"
    override val paidCheckout: String = "Paid Checkout"

    override val searchOrders: String = "Search by factory name or order #…"
    override val unpaidBalance: String = "Total Unpaid Balance"
    override val totalOrders: String = "Total Orders"
    override val totalPcsSold: String = "Total Rods Sold"
    override val totalUnpaid: String = "Total Unpaid"
    override val totalItemsSold: String = "Total Items Sold"
    override val orderDetails: String = "Order Details"
    override val orderNumber: String = "Order #"
    override val customerFactory: String = "Customer Factory"
    override val orderStatus: String = "Status"
    override val confirmed: String = "Confirmed"
    override val delivered: String = "Delivered"
    override val awaitingDelivery: String = "Stock Ready (Awaiting Delivery)"
    override val paid: String = "Paid"
    override val unpaid: String = "Unpaid"
    override val partial: String = "Partial"
    override val markDelivered: String = "Mark Delivered"
    override val recordPayment: String = "Record Payment"
    override val shareInvoice: String = "Share Receipt"
    override val deleteOrder: String = "Delete Order"
    override val paymentDialogTitle: String = "Record Customer Payment"
    override val paymentReceived: String = "Payment Received (Rs)"
    override val remainingBalance: String = "Remaining Balance"
    override val remainingDue: String = "Remaining Due"
    override val savePayment: String = "Save Payment"
    override val noOrdersFound: String = "No orders found."
    override val orderPaid: String = "PAID"
    override fun orderPartial(dueStr: String): String = "PARTIAL" + (if (dueStr.isNotEmpty()) " (Due: $dueStr)" else "")
    override val orderUnpaid: String = "UNPAID"
    override val nilDue: String = "NIL DUE"
    override val deliver: String = "Deliver"
    override val amountRs: String = "Amount (Rs)"
    override val receivedBy: String = "Received By"
    override val receivedIn: String = "Payment Method"
    override val nilFullBalance: String = "Full balance settled"

    override val tabCustomerFactories: String = "Factories (Customers)"
    override val tabPaint: String = "Paint Ledger"
    override val tabRawMaterial: String = "Raw Material"
    override val tabScrap: String = "Scrap Ledger"
    override val tabLabour: String = "Labour & Attendance"
    override val tabCustomFactory: String = "Custom Ledgers"
    override val addFactory: String = "Add Factory"
    override val addWorker: String = "Add Worker"
    override val markAttendance: String = "Daily Attendance"
    override val addLedgerEntry: String = "Add Entry"
    override val debitBilled: String = "Debit (Billed/Given)"
    override val creditReceived: String = "Credit (Received/Paid)"
    override val balanceDr: String = "Receivable (Dr)"
    override val balanceCr: String = "Payable (Cr)"
    override val viewLedger: String = "View Ledger"
    override val partyName: String = "Party Name"
    override val contactNumber: String = "Contact Phone"
    override val addressCity: String = "Address / City"
    override val openingBalance: String = "Opening Balance"
    override val workerType: String = "Worker Type"
    override val dailyWage: String = "Daily Wage (Rs)"
    override val perPieceRate: String = "Per Piece Rate (Rs)"
    override val attendancePresent: String = "Present"
    override val attendanceAbsent: String = "Absent"
    override val attendanceHalfDay: String = "Half Day"
    override val markAllPresent: String = "Mark All Present"
    override val saveAttendance: String = "Save Attendance"
    override val pipesCoilsSupplied: String = "Pipes/Coils Supplied"
    override val weightKg: String = "Weight (KG)"
    override val scrapSold: String = "Scrap Sold"
    override val scrapBuyer: String = "Scrap Buyer / Factory"

    override val stockReadyTitle: String = "Stock Ready (in Factory)"
    override val stockReadySubtitle: String = "Confirmed factory orders awaiting delivery gate receipt"
    override val ordersReady: String = "Orders Ready"
    override val stockReadyValue: String = "Stock Ready Value"
    override val noStockReady: String = "No pending ready stock in factory. All orders delivered!"
    override val rawMaterialInventory: String = "Raw Material Inventory & Pipe Usage"
    override val rawInventorySubtitle: String = "Steel pipe coils & rods supplied vs consumed in fabrication"
    override val scrapInventoryTitle: String = "Scrap Metal Tracking"
    override val scrapSubtitle: String = "Metal offcuts & filings generated from fabrication"
    override val lowStockWarning: String = "Low Stock!"
    override val available: String = "Available"
    override val consumed: String = "Consumed"

    override val operatingExpensesTitle: String = "Factory Operating Expenses"
    override val operatingExpensesSubtitle: String = "Utilities, maintenance, tea, fuel, and daily factory costs"
    override val withdrawalsTitle: String = "Owner Personal Withdrawals"
    override val withdrawalsSubtitle: String = "Capital draws and personal cash taken by owner"
    override val totalExpenses: String = "Total Operating Expenses"
    override val totalWithdrawals: String = "Total Owner Withdrawals"
    override val addExpense: String = "Add Expense"
    override val addWithdrawal: String = "Add Withdrawal"
    override val expenseCategory: String = "Expense Category"
    override val expenseTitle: String = "Expense Title"
    override val recordedBy: String = "Recorded By"

    override val returnsTitle: String = "Damaged & Returned Goods"
    override val returnsSubtitle: String = "Log defective customer batches and route to rework or scrap"
    override val totalUnitsReturned: String = "Total Units Returned"
    override val reworkVsScrap: String = "Rework vs Scrap"
    override val logReturn: String = "Log Return"
    override val defectReason: String = "Defect Reason"
    override val resolution: String = "Action / Resolution"
    override val sendForRework: String = "Send for Rework"
    override val sendToScrap: String = "Scrap Material"
    override val replacementSent: String = "Replaced from Ready Stock"
    override val noReturnsLogged: String = "No defective returns logged. Quality is pristine!"

    override val settingsTitle: String = "Settings & Security"
    override val securitySection: String = "Security & End-to-End Encryption"
    override val securitySubtitle: String = "Hardware-backed AES-256-GCM cipher protection for sensitive records"
    override val encryptionActive: String = "AES-256-GCM Encryption"
    override val encryptionSub: String = "All transaction payload records encrypted at rest"
    override val posPin: String = "POS Lock PIN"
    override val posPinSub: String = "4-digit security code for cashier unlock"
    override val changePin: String = "Change PIN"
    override val securityQuestion: String = "Security Recovery Question"
    override val securityQuestionSub: String = "Reset PIN without database reset if forgotten"
    override val updateAnswer: String = "Update Answer"
    override val appearanceSection: String = "Appearance & Color Presets"
    override val appearanceSubtitle: String = "Select high-contrast industrial theme tailored for workshop lighting"
    override val cloudSyncSection: String = "Cloud Sync & Offline Backup"
    override val cloudSyncSubtitle: String = "Continuous background sync whenever network is restored"
    override val lastSynced: String = "Last Synced"
    override val never: String = "Never"
    override val dataExportSection: String = "Data Export & Audit Records"
    override val dataExportSubtitle: String = "Download complete accounting and sales ledgers as encrypted JSON/CSV"
    override val exportLedgers: String = "Export All Records"
    override val languageSection: String = "App Language"
    override val languageSubtitle: String = "Switch effortlessly between English and Urdu across all screens"
    override val english: String = "English"
    override val urdu: String = "Urdu"

    override val enterPinPrompt: String = "Enter 4-Digit PIN"
    override val enterPinTitle: String = "Unlock Falcon Rod Maker"
    override val cashierUser: String = "Cashier: Amir"
    override val incorrectPin: String = "Incorrect PIN. Try again."
    override val forgotPin: String = "Forgot PIN?"
    override val defaultPinHint: String = "Default PIN: 321"
    override val recoveryTitle: String = "Security PIN Recovery"
    override val recoveryQuestionText: String = "Security Question: What was your childhood factory nickname?"
    override val enterAnswerPrompt: String = "Enter answer (default: amir)"
    override val enterNewPinPrompt: String = "Enter New 4-Digit PIN"
    override val confirmNewPinPrompt: String = "Confirm New PIN"
    override val resetPinButton: String = "Set New PIN"
    override val pinMismatch: String = "PINs do not match!"
    override val incorrectAnswer: String = "Incorrect answer. Try again."
}

/**
 * Urdu Resource Bundle implementation.
 */
object UrduResourceBundle : ResourceBundleDictionary {
    override val language: AppLanguage = AppLanguage.URDU

    override val rawDictionary: Map<String, String> = mapOf(
        "brand_name" to "فالکن راڈ میکر",
        "brand_subtitle" to "فین ایکسیسریز · گجرات",
        "search_everything" to "ہر چیز تلاش کریں…",
        "lock_app" to "ایپ لاک کریں",
        "toggle_theme" to "تھیم تبدیل کریں",
        "update_live" to "تازہ ترین اپ ڈیٹ",
        "sync_now" to "کلاؤڈ سنک کریں",
        "search" to "تلاش کریں…",
        "clear" to "صاف کریں",
        "cancel" to "منسوخ کریں",
        "save" to "محفوظ کریں",
        "save_changes" to "تبدیلیاں محفوظ کریں",
        "add" to "شامل کریں",
        "delete" to "حذف کریں",
        "edit" to "ترمیم کریں",
        "confirm" to "تصدیق کریں",
        "close" to "بند کریں",
        "back" to "واپس",
        "all" to "سب",
        "active" to "فعال",
        "date" to "تاریخ",
        "notes" to "تفصیل / نوٹس",
        "amount" to "رقم (روپے)",
        "quantity" to "تعداد",
        "rate" to "ریٹ",
        "total" to "کل میزان",
        "pcs" to "عدد",
        "gross_sales" to "کل فروخت (سیلز)",
        "net_profit" to "خالص منافع (ارب)",
        "total_receivables" to "کل وصولیاں (گاہکوں کے ذمے)",
        "total_payables" to "کل واجبات (سپلائرز کے بقایا)",
        "factory_sales_share" to "فیکٹریوں کی فروخت کا تناسب",
        "outstanding_balances" to "فیکٹریوں کے ذمے بقایا رقم",
        "all_balances_cleared" to "تمام فیکٹریوں کے کھاتے کلیئر ہیں!",
        "quick_actions" to "فوری ایکشن ہب",
        "action_new_order" to "نیا آرڈر بُک کریں",
        "action_raw_material" to "خام مال اندراج",
        "action_add_expense" to "خرچہ درج کریں",
        "action_attendance" to "حاضری مارک کریں",
        "action_returns" to "مال واپسی"
    )

    override fun formatCurrency(amount: Double): String {
        val formatted = String.format(Locale.US, "%,.0f", amount)
        return "$formatted روپے"
    }

    override fun formatPcs(count: Number): String = "$count عدد"
    override fun formatWeight(kg: Double): String = String.format(Locale.US, "%.1f کلوگرام", kg)

    override fun translatePeriod(period: String): String = when (period.lowercase()) {
        "daily" -> "روزانہ"
        "yesterday" -> "گزشتہ کل"
        "weekly" -> "ہفتہ وار"
        "monthly" -> "ماہانہ"
        "yearly" -> "سالانہ"
        else -> period
    }

    override fun translateCategory(category: String): String = when (category.trim()) {
        "American Pedestal Fan" -> "امریکن پیڈسٹل فین"
        "American Exhaust Fan" -> "امریکن ایگزاسٹ فین"
        "American Universal Fan" -> "امریکن یونیورسل فین"
        "Bracket Pedestal Fan" -> "بریکٹ پیڈسٹل فین"
        "Bracket Exhaust Fan" -> "بریکٹ ایگزاسٹ فین"
        "Bracket Universal Fan" -> "بریکٹ یونیورسل فین"
        else -> category
    }

    override fun translateExpenseCategory(category: String): String = when (category.lowercase().trim()) {
        "electricity", "utility", "بجلی" -> "بجلی و یوٹیلٹی بلز"
        "rent", "factory rent", "کرایہ" -> "فیکٹری کرایہ"
        "maintenance", "مرمت" -> "مشینری اور اوزار مرمت"
        "fuel", "generator", "ایندھن" -> "ایندھن اور جنریٹر"
        "food", "tea", "چائے" -> "چائے، کھانا اور مہمان نوازی"
        "transport", "گاڑی" -> "ٹرانسپورٹ اور کرایہ باربرداری"
        else -> "متفرق اخراجات"
    }

    override val brandName: String = "فالکن راڈ میکر"
    override val brandSubtitle: String = "فین ایکسیسریز · گجرات"
    override val searchEverything: String = "ہر چیز تلاش کریں…"
    override val lockApp: String = "ایپ لاک کریں"
    override val toggleTheme: String = "تھیم تبدیل کریں"
    override val updateLive: String = "تازہ ترین اپ ڈیٹ"
    override val syncNow: String = "کلاؤڈ سنک کریں"
    override val search: String = "تلاش کریں…"
    override val clear: String = "صاف کریں"
    override val cancel: String = "منسوخ کریں"
    override val save: String = "محفوظ کریں"
    override val saveChanges: String = "تبدیلیاں محفوظ کریں"
    override val add: String = "شامل کریں"
    override val delete: String = "حذف کریں"
    override val edit: String = "ترمیم کریں"
    override val confirm: String = "تصدیق کریں"
    override val close: String = "بند کریں"
    override val back: String = "واپس"
    override val all: String = "سب"
    override val active: String = "فعال"
    override val date: String = "تاریخ"
    override val notes: String = "تفصیل / نوٹس"
    override val amount: String = "رقم (روپے)"
    override val quantity: String = "تعداد"
    override val rate: String = "ریٹ"
    override val total: String = "کل میزان"
    override val pcs: String = "عدد"

    override val menuTitle: String = "فالکن پی او ایس مینو"
    override val closeMenu: String = "مینو بند کریں"
    override val sectionOperations: String = "سیلز اور آرڈرز"
    override val sectionLedgers: String = "کھاتے و لیجرز"
    override val sectionInventory: String = "اسٹاک اور فیکٹری"
    override val sectionSystem: String = "سسٹم اور سیکیورٹی"

    override val navOverview: String = "خلاصہ"
    override val navCatalog: String = "کیٹلاگ"
    override val navOrders: String = "آرڈرز"
    override val navLedgers: String = "لیجرز"
    override val navStock: String = "اسٹاک"
    override val navExpenses: String = "اخراجات"
    override val navSettings: String = "سیٹنگز"

    override val periodDaily: String = "روزانہ"
    override val periodYesterday: String = "گزشتہ کل"
    override val periodWeekly: String = "ہفتہ وار"
    override val periodMonthly: String = "ماہانہ"
    override val periodYearly: String = "سالانہ"

    override val businessReportTitle: String = "کاروباری کارکردگی کا جائزہ"
    override val grossSales: String = "کل فروخت (سیلز)"
    override val ordersBooked: String = "بُک شدہ آرڈرز"
    override val unitsSold: String = "فروخت شدہ راڈز"
    override val avgOrder: String = "اوسط فی آرڈر"
    override val periodExpenses: String = "فیکٹری اخراجات"
    override val materialPaid: String = "خام مال ادائیگیاں"
    override val labourAndPaint: String = "مزدوری و پینٹ"
    override val withdrawals: String = "مالک کی رقم نکاسی"
    override val netProfit: String = "خالص منافع (ارب)"
    override val totalReceivables: String = "کل وصولیاں (گاہکوں کے ذمے)"
    override val totalPayables: String = "کل واجبات (سپلائرز کے بقایا)"
    override val netLedgerDiff: String = "خالص بیلنس (وصولیاں - واجبات)"
    override val quickActions: String = "فوری ایکشن ہب"
    override val actionNewOrder: String = "نیا آرڈر بُک کریں"
    override val actionRawMaterial: String = "خام مال اندراج"
    override val actionAddExpense: String = "خرچہ درج کریں"
    override val actionAttendance: String = "حاضری مارک کریں"
    override val actionReturns: String = "مال واپسی"
    override val factorySalesShare: String = "فیکٹریوں کی فروخت کا تناسب"
    override val outstandingBalances: String = "فیکٹریوں کے ذمے بقایا رقم"
    override val allBalancesCleared: String = "تمام فیکٹریوں کے کھاتے کلیئر ہیں!"
    override val noSalesRecorded: String = "اس مدت میں کوئی آرڈر درج نہیں ہوا۔"

    override val searchProducts: String = "فین راڈز اور سامان تلاش کریں…"
    override val searchCatalog: String = "کیٹلاگ تلاش کریں…"
    override val addProduct: String = "نئی پروڈکٹ شامل کریں"
    override val editProduct: String = "پروڈکٹ میں ترمیم کریں"
    override val productName: String = "پروڈکٹ کا نام"
    override val category: String = "کیٹیگری"
    override val priceRs: String = "قیمت (روپے)"
    override val weightGrams: String = "معیاری وزن (گرام)"
    override val pipeType: String = "پائپ گیج / قسم"
    override val addToCart: String = "ٹوکری میں شامل کریں"
    override val inCart: String = "ٹوکری میں ہے"
    override val cartAndBooking: String = "آرڈر بکنگ اور ٹوکری"
    override val selectFactory: String = "گاہک فیکٹری منتخب کریں"
    override val orderSummary: String = "آرڈر کا خلاصہ"
    override val totalItems: String = "کل اشیاء"
    override val grandTotal: String = "کل رقم"
    override val bookOrder: String = "آرڈر بُک کریں"
    override val clearCart: String = "ٹوکری خالی کریں"
    override val emptyCartMsg: String = "ٹوکری خالی ہے۔ کیٹلاگ سے راڈز شامل کریں۔"
    override val pleaseSelectFactory: String = "براہ کرم پہلے گاہک فیکٹری منتخب کریں!"
    override val orderBookedSuccess: String = "آرڈر کامیابی سے بُک ہو گیا!"
    override val noProductsFound: String = "کوئی پروڈکٹ نہیں ملی۔"
    override val currentInvoice: String = "موجودہ انوائس"
    override val reviewOrder: String = "آرڈر کا جائزہ"
    override val factoryCustomer: String = "گاہک فیکٹری"
    override val totalAmount: String = "کل رقم"
    override val bookUnpaidOrder: String = "ادھار آرڈر بُک کریں"
    override val paidCheckout: String = "نقد ادا شدہ چیک آؤٹ"

    override val searchOrders: String = "فیکٹری یا آرڈر نمبر سے تلاش کریں…"
    override val unpaidBalance: String = "کل غیر ادا شدہ رقم"
    override val totalOrders: String = "کل آرڈرز"
    override val totalPcsSold: String = "کل راڈز فروخت"
    override val totalUnpaid: String = "کل غیر ادا شدہ"
    override val totalItemsSold: String = "کل فروخت شدہ اشیاء"
    override val orderDetails: String = "آرڈر کی تفصیلات"
    override val orderNumber: String = "آرڈر نمبر"
    override val customerFactory: String = "کسٹمر فیکٹری"
    override val orderStatus: String = "آرڈر اسٹیٹس"
    override val confirmed: String = "تصدیق شدہ"
    override val delivered: String = "فیکٹری روانہ (ڈیلیور شدہ)"
    override val awaitingDelivery: String = "اسٹاک تیار (روانگی باقی)"
    override val paid: String = "مکمل ادا شدہ"
    override val unpaid: String = "غیر ادا شدہ (بقایا)"
    override val partial: String = "جزوی ادائیگی"
    override val markDelivered: String = "ڈیلیور نشان زد کریں"
    override val recordPayment: String = "ادائیگی وصول کریں"
    override val shareInvoice: String = "رسید شیئر کریں"
    override val deleteOrder: String = "آرڈر حذف کریں"
    override val paymentDialogTitle: String = "کسٹمر ادائیگی کا اندراج"
    override val paymentReceived: String = "وصول شدہ رقم (روپے)"
    override val remainingBalance: String = "بقیہ واجب الادا"
    override val remainingDue: String = "بقیہ واجب الادا"
    override val savePayment: String = "ادائیگی جمع کریں"
    override val noOrdersFound: String = "کوئی آرڈر نہیں ملا۔"
    override val orderPaid: String = "مکمل ادا شدہ"
    override fun orderPartial(dueStr: String): String = "جزوی ادا شدہ" + (if (dueStr.isNotEmpty()) " ($dueStr باقی)" else "")
    override val orderUnpaid: String = "غیر ادا شدہ"
    override val nilDue: String = "کوئی بقایا نہیں"
    override val deliver: String = "روانہ کریں"
    override val amountRs: String = "رقم (روپے)"
    override val receivedBy: String = "وصول کنندہ"
    override val receivedIn: String = "وصول بذریعہ"
    override val nilFullBalance: String = "مکمل ادا شدہ (کوئی بقایا نہیں)"

    override val tabCustomerFactories: String = "فیکٹریاں (کسٹمرز)"
    override val tabPaint: String = "پینٹ کھاتہ"
    override val tabRawMaterial: String = "خام مال کھاتہ"
    override val tabScrap: String = "اسکریپ کھاتہ"
    override val tabLabour: String = "مزدوری و حاضری"
    override val tabCustomFactory: String = "کسٹم کھاتے"
    override val addFactory: String = "نئی فیکٹری شامل کریں"
    override val addWorker: String = "نیا ورکر شامل کریں"
    override val markAttendance: String = "روزانہ حاضری"
    override val addLedgerEntry: String = "نیا اندراج کریں"
    override val debitBilled: String = "بنام (ڈیبٹ)"
    override val creditReceived: String = "جمع (کریڈٹ)"
    override val balanceDr: String = "قابل وصول (Dr)"
    override val balanceCr: String = "قابل ادا (Cr)"
    override val viewLedger: String = "کھاتہ دیکھیں"
    override val partyName: String = "کھاتے دار کا نام"
    override val contactNumber: String = "موبائل نمبر"
    override val addressCity: String = "پتہ / شہر"
    override val openingBalance: String = "سابقہ بقایا رقم"
    override val workerType: String = "ورکر کی قسم"
    override val dailyWage: String = "یومیہ اجرت (روپے)"
    override val perPieceRate: String = "فی راڈ ریٹ (روپے)"
    override val attendancePresent: String = "حاضر"
    override val attendanceAbsent: String = "غیر حاضر"
    override val attendanceHalfDay: String = "آدھا دن"
    override val markAllPresent: String = "سب کو حاضر مارک کریں"
    override val saveAttendance: String = "حاضری محفوظ کریں"
    override val pipesCoilsSupplied: String = "پائپ / کوائل کی ترسیل"
    override val weightKg: String = "وزن (کلوگرام)"
    override val scrapSold: String = "اسکریپ فروخت"
    override val scrapBuyer: String = "اسکریپ خریدار"

    override val stockReadyTitle: String = "اسٹاک تیار (فیکٹری میں)"
    override val stockReadySubtitle: String = "تیار فین راڈز جن کی ڈلیوری باقی ہے"
    override val ordersReady: String = "تیار آرڈرز"
    override val stockReadyValue: String = "تیار اسٹاک کی مالیت"
    override val noStockReady: String = "اس وقت کوئی غیر ڈیلیور شدہ آرڈر نہیں ہے۔ تمام مال بھیج دیا گیا ہے۔"
    override val rawMaterialInventory: String = "خام مال کا اسٹاک اور کھپت"
    override val rawInventorySubtitle: String = "سپلائرز سے موصول شدہ اور پیداوار میں استعمال شدہ پائپ"
    override val scrapInventoryTitle: String = "اسکریپ میٹل ٹریکنگ"
    override val scrapSubtitle: String = "کٹائی اور خراد کے بعد بچا ہوا لوہا"
    override val lowStockWarning: String = "اسٹاک کم ہے!"
    override val available: String = "دستیاب"
    override val consumed: String = "استعمال شدہ"

    override val operatingExpensesTitle: String = "فیکٹری کے عمومی اخراجات"
    override val operatingExpensesSubtitle: String = "بجلی، کرایہ، ایندھن اور متفرق روزمرہ اخراجات"
    override val withdrawalsTitle: String = "مالک کی ذاتی رقم نکاسی"
    override val withdrawalsSubtitle: String = "کاروبار سے ذاتی استعمال کے لیے نکالی گئی رقوم"
    override val totalExpenses: String = "کل اخراجات"
    override val totalWithdrawals: String = "کل نکاسی"
    override val addExpense: String = "نیا خرچ درج کریں"
    override val addWithdrawal: String = "رقم نکاسی درج کریں"
    override val expenseCategory: String = "خرچے کی قسم"
    override val expenseTitle: String = "خرچے کی تفصیل"
    override val recordedBy: String = "اندراج کنندہ"

    override val returnsTitle: String = "خراب و واپس شدہ راڈز"
    override val returnsSubtitle: String = "گاہکوں سے موصول شدہ نقص والا مال، مرمت یا اسکریپ"
    override val totalUnitsReturned: String = "کل واپس شدہ یونٹس"
    override val reworkVsScrap: String = "مرمت بمقابلہ اسکریپ"
    override val logReturn: String = "مال واپسی درج کریں"
    override val defectReason: String = "خرابی کی وجہ"
    override val resolution: String = "فیصلہ / کارروائی"
    override val sendForRework: String = "دوبارہ مرمت کروائیں"
    override val sendToScrap: String = "اسکریپ میں ڈالیں"
    override val replacementSent: String = "نئے اسٹاک سے متبادل دیا"
    override val noReturnsLogged: String = "کوئی واپس شدہ مال درج نہیں ہے۔ کوالٹی بہترین ہے!"

    override val settingsTitle: String = "ایپ ترتیبات اور سیکیورٹی"
    override val securitySection: String = "سیکیورٹی اور خفیہ کاری (انکرپشن)"
    override val securitySubtitle: String = "ہارڈویئر بیسڈ AES-256-GCM خفیہ پروٹیکشن"
    override val encryptionActive: String = "AES-256-GCM انکرپشن فعال ہے"
    override val encryptionSub: String = "تمام لیجرز اور رقوم کے ریکارڈز انکرپٹڈ ہیں"
    override val posPin: String = "پی او ایس لاک پن (PIN)"
    override val posPinSub: String = "ایپ کھولنے کے لیے 4 ہندسوں کا سیکیورٹی کوڈ"
    override val changePin: String = "پن تبدیل کریں"
    override val securityQuestion: String = "حفاظتی ریکوری سوال"
    override val securityQuestionSub: String = "پن بھول جانے کی صورت میں بازیابی کے لیے"
    override val updateAnswer: String = "جواب تبدیل کریں"
    override val appearanceSection: String = "ظاہری شکل اور رنگین تھیمز"
    override val appearanceSubtitle: String = "فیکٹری ماحول کے مطابق ہائی کنٹراسٹ رنگ منتخب کریں"
    override val cloudSyncSection: String = "کلاؤڈ بیک اپ اور ہم وقت سازی"
    override val cloudSyncSubtitle: String = "انٹرنیٹ آنے پر خودکار کلاؤڈ بیک اپ"
    override val lastSynced: String = "آخری بار ہم وقت کیا گیا"
    override val never: String = "ابھی تک نہیں"
    override val dataExportSection: String = "ڈیٹا ایکسپورٹ اور بیک اپ فائل"
    override val dataExportSubtitle: String = "مکمل لیجرز اور سیلز کا بیک اپ محفوظ کریں"
    override val exportLedgers: String = "مکمل ریکارڈز ایکسپورٹ کریں"
    override val languageSection: String = "ایپ کی زبان (Language)"
    override val languageSubtitle: String = "انگریزی اور اردو کے درمیان فوراً تبدیل کریں"
    override val english: String = "English"
    override val urdu: String = "اردو"

    override val enterPinPrompt: String = "چار ہندسوں کا پن درج کریں"
    override val enterPinTitle: String = "فالکن راڈ میکر میں لاگ ان کریں"
    override val cashierUser: String = "صارف: عامر (منیجر)"
    override val incorrectPin: String = "غلط پن! دوبارہ کوشش کریں۔"
    override val forgotPin: String = "پن بھول گئے؟"
    override val defaultPinHint: String = "ابتدائی ڈیفالٹ پن: 321"
    override val recoveryTitle: String = "پن کی بازیافت (ریکوری)"
    override val recoveryQuestionText: String = "حفاظتی سوال: آپ کا بچپن کا فیکٹری نک نیم کیا تھا؟"
    override val enterAnswerPrompt: String = "حفاظتی جواب درج کریں (ڈیفالٹ: amir)"
    override val enterNewPinPrompt: String = "نیا 4 ہندسوں کا پن درج کریں"
    override val confirmNewPinPrompt: String = "نئے پن کی تصدیق کریں"
    override val resetPinButton: String = "نیا پن لاگو کریں"
    override val pinMismatch: String = "دونوں پن مماثل نہیں ہیں!"
    override val incorrectAnswer: String = "غلط جواب! دوبارہ کوشش کریں۔"
}

/**
 * Singleton Localization Manager providing state-based language switching,
 * resource bundle management, and localized formatting.
 */
class LocalizationManager private constructor(initialLang: AppLanguage = AppLanguage.ENGLISH) {

    private val _currentLanguage = MutableStateFlow(initialLang)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _stringsBundle = MutableStateFlow<ResourceBundleDictionary>(getBundle(initialLang))
    val stringsBundle: StateFlow<ResourceBundleDictionary> = _stringsBundle.asStateFlow()

    val strings: ResourceBundleDictionary get() = _stringsBundle.value
    val isUrdu: Boolean get() = _currentLanguage.value == AppLanguage.URDU
    val isRtl: Boolean get() = _currentLanguage.value.isRtl

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        _stringsBundle.value = getBundle(language)
    }

    fun setLanguage(code: String) {
        setLanguage(AppLanguage.fromCode(code))
    }

    fun toggleLanguage(): AppLanguage {
        val next = _currentLanguage.value.toggle()
        setLanguage(next)
        return next
    }

    fun getString(key: String, vararg args: Any): String {
        return _stringsBundle.value.getString(key, *args)
    }

    fun formatCurrency(amount: Double): String {
        return _stringsBundle.value.formatCurrency(amount)
    }

    companion object {
        @Volatile
        private var instance: LocalizationManager? = null

        fun getInstance(initialLang: AppLanguage = AppLanguage.ENGLISH): LocalizationManager {
            return instance ?: synchronized(this) {
                instance ?: LocalizationManager(initialLang).also { instance = it }
            }
        }

        fun getBundle(language: AppLanguage): ResourceBundleDictionary {
            return when (language) {
                AppLanguage.ENGLISH -> EnglishResourceBundle
                AppLanguage.URDU -> UrduResourceBundle
            }
        }

        fun getBundle(isUrdu: Boolean): ResourceBundleDictionary {
            return if (isUrdu) UrduResourceBundle else EnglishResourceBundle
        }
    }
}

// Composition Locals for seamless Compose integration
val LocalLocalization = staticCompositionLocalOf { LocalizationManager.getInstance() }
val LocalStrings = staticCompositionLocalOf<ResourceBundleDictionary> { EnglishResourceBundle }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

/**
 * Top-level Composable wrapper providing the active LocalizationManager,
 * reactive StringsBundle, and dynamic RTL/LTR layout direction.
 */
@Composable
fun ProvideLocalization(
    localizationManager: LocalizationManager,
    content: @Composable () -> Unit
) {
    val language by localizationManager.currentLanguage.collectAsState()
    val bundle by localizationManager.stringsBundle.collectAsState()
    val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalLocalization provides localizationManager,
        LocalStrings provides bundle,
        LocalAppLanguage provides language,
        LocalLayoutDirection provides layoutDirection
    ) {
        content()
    }
}
