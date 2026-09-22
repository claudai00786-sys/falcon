package com.example.ui.util

/**
 * Centralized, production-grade bilingual dictionary (English & Urdu)
 * backed by the [LocalizationManager] and [ResourceBundleDictionary].
 *
 * Provides seamless access via typed functions, direct bundle properties,
 * or dynamic key-value lookups throughout the application.
 */
object AppStrings {

    val current: ResourceBundleDictionary
        get() = LocalizationManager.getInstance().strings

    fun bundle(isUr: Boolean): ResourceBundleDictionary =
        LocalizationManager.getBundle(isUr)

    fun get(key: String, isUr: Boolean): String =
        bundle(isUr)[key]

    // --- Global & Branding ---
    fun brandName(isUr: Boolean) = bundle(isUr).brandName
    fun brandSubtitle(isUr: Boolean) = bundle(isUr).brandSubtitle
    fun searchEverything(isUr: Boolean) = bundle(isUr).searchEverything
    fun lockApp(isUr: Boolean) = bundle(isUr).lockApp
    fun toggleTheme(isUr: Boolean) = bundle(isUr).toggleTheme
    fun updateLive(isUr: Boolean) = bundle(isUr).updateLive
    fun syncNow(isUr: Boolean) = bundle(isUr).syncNow
    fun search(isUr: Boolean) = bundle(isUr).search
    fun clear(isUr: Boolean) = bundle(isUr).clear
    fun cancel(isUr: Boolean) = bundle(isUr).cancel
    fun save(isUr: Boolean) = bundle(isUr).save
    fun saveChanges(isUr: Boolean) = bundle(isUr).saveChanges
    fun add(isUr: Boolean) = bundle(isUr).add
    fun delete(isUr: Boolean) = bundle(isUr).delete
    fun edit(isUr: Boolean) = bundle(isUr).edit
    fun confirm(isUr: Boolean) = bundle(isUr).confirm
    fun close(isUr: Boolean) = bundle(isUr).close
    fun back(isUr: Boolean) = bundle(isUr).back
    fun all(isUr: Boolean) = bundle(isUr).all
    fun active(isUr: Boolean) = bundle(isUr).active
    fun date(isUr: Boolean) = bundle(isUr).date
    fun notes(isUr: Boolean) = bundle(isUr).notes
    fun amount(isUr: Boolean) = bundle(isUr).amount
    fun quantity(isUr: Boolean) = bundle(isUr).quantity
    fun rate(isUr: Boolean) = bundle(isUr).rate
    fun total(isUr: Boolean) = bundle(isUr).total
    fun pcs(isUr: Boolean) = bundle(isUr).pcs

    // --- Navigation & Menu ---
    fun menuTitle(isUr: Boolean) = bundle(isUr).menuTitle
    fun closeMenu(isUr: Boolean) = bundle(isUr).closeMenu
    fun sectionOperations(isUr: Boolean) = bundle(isUr).sectionOperations
    fun sectionLedgers(isUr: Boolean) = bundle(isUr).sectionLedgers
    fun sectionInventory(isUr: Boolean) = bundle(isUr).sectionInventory
    fun sectionSystem(isUr: Boolean) = bundle(isUr).sectionSystem
    fun navOverview(isUr: Boolean) = bundle(isUr).navOverview
    fun navCatalog(isUr: Boolean) = bundle(isUr).navCatalog
    fun navOrders(isUr: Boolean) = bundle(isUr).navOrders
    fun navLedgers(isUr: Boolean) = bundle(isUr).navLedgers
    fun navStock(isUr: Boolean) = bundle(isUr).navStock
    fun navExpenses(isUr: Boolean) = bundle(isUr).navExpenses
    fun navSettings(isUr: Boolean) = bundle(isUr).navSettings

    // --- Periods ---
    fun periodDaily(isUr: Boolean) = bundle(isUr).periodDaily
    fun periodYesterday(isUr: Boolean) = bundle(isUr).periodYesterday
    fun periodWeekly(isUr: Boolean) = bundle(isUr).periodWeekly
    fun periodMonthly(isUr: Boolean) = bundle(isUr).periodMonthly
    fun periodYearly(isUr: Boolean) = bundle(isUr).periodYearly
    fun translatePeriod(period: String, isUr: Boolean): String = bundle(isUr).translatePeriod(period)

    // --- Overview Screen ---
    fun businessReportTitle(isUr: Boolean) = bundle(isUr).businessReportTitle
    fun grossSales(isUr: Boolean) = bundle(isUr).grossSales
    fun ordersBooked(isUr: Boolean) = bundle(isUr).ordersBooked
    fun unitsSold(isUr: Boolean) = bundle(isUr).unitsSold
    fun avgOrder(isUr: Boolean) = bundle(isUr).avgOrder
    fun periodExpenses(isUr: Boolean) = bundle(isUr).periodExpenses
    fun materialPaid(isUr: Boolean) = bundle(isUr).materialPaid
    fun labourAndPaint(isUr: Boolean) = bundle(isUr).labourAndPaint
    fun withdrawals(isUr: Boolean) = bundle(isUr).withdrawals
    fun netProfit(isUr: Boolean) = bundle(isUr).netProfit
    fun totalReceivables(isUr: Boolean) = bundle(isUr).totalReceivables
    fun totalPayables(isUr: Boolean) = bundle(isUr).totalPayables
    fun netLedgerDiff(isUr: Boolean) = bundle(isUr).netLedgerDiff
    fun quickActions(isUr: Boolean) = bundle(isUr).quickActions
    fun actionNewOrder(isUr: Boolean) = bundle(isUr).actionNewOrder
    fun actionRawMaterial(isUr: Boolean) = bundle(isUr).actionRawMaterial
    fun actionAddExpense(isUr: Boolean) = bundle(isUr).actionAddExpense
    fun actionAttendance(isUr: Boolean) = bundle(isUr).actionAttendance
    fun actionReturns(isUr: Boolean) = bundle(isUr).actionReturns
    fun factorySalesShare(isUr: Boolean) = bundle(isUr).factorySalesShare
    fun outstandingBalances(isUr: Boolean) = bundle(isUr).outstandingBalances
    fun allBalancesCleared(isUr: Boolean) = bundle(isUr).allBalancesCleared
    fun noSalesRecorded(isUr: Boolean) = bundle(isUr).noSalesRecorded

    // --- Catalog & Products ---
    fun searchProducts(isUr: Boolean) = bundle(isUr).searchProducts
    fun searchCatalog(isUr: Boolean) = bundle(isUr).searchCatalog
    fun addProduct(isUr: Boolean) = bundle(isUr).addProduct
    fun editProduct(isUr: Boolean) = bundle(isUr).editProduct
    fun productName(isUr: Boolean) = bundle(isUr).productName
    fun category(isUr: Boolean) = bundle(isUr).category
    fun priceRs(isUr: Boolean) = bundle(isUr).priceRs
    fun weightGrams(isUr: Boolean) = bundle(isUr).weightGrams
    fun pipeType(isUr: Boolean) = bundle(isUr).pipeType
    fun addToCart(isUr: Boolean) = bundle(isUr).addToCart
    fun inCart(isUr: Boolean) = bundle(isUr).inCart
    fun cartAndBooking(isUr: Boolean) = bundle(isUr).cartAndBooking
    fun selectFactory(isUr: Boolean) = bundle(isUr).selectFactory
    fun orderSummary(isUr: Boolean) = bundle(isUr).orderSummary
    fun totalItems(isUr: Boolean) = bundle(isUr).totalItems
    fun grandTotal(isUr: Boolean) = bundle(isUr).grandTotal
    fun bookOrder(isUr: Boolean) = bundle(isUr).bookOrder
    fun clearCart(isUr: Boolean) = bundle(isUr).clearCart
    fun emptyCartMsg(isUr: Boolean) = bundle(isUr).emptyCartMsg
    fun pleaseSelectFactory(isUr: Boolean) = bundle(isUr).pleaseSelectFactory
    fun orderBookedSuccess(isUr: Boolean) = bundle(isUr).orderBookedSuccess
    fun noProductsFound(isUr: Boolean) = bundle(isUr).noProductsFound
    fun currentInvoice(isUr: Boolean) = bundle(isUr).currentInvoice
    fun reviewOrder(isUr: Boolean) = bundle(isUr).reviewOrder
    fun factoryCustomer(isUr: Boolean) = bundle(isUr).factoryCustomer
    fun totalAmount(isUr: Boolean) = bundle(isUr).totalAmount
    fun bookUnpaidOrder(isUr: Boolean) = bundle(isUr).bookUnpaidOrder
    fun paidCheckout(isUr: Boolean) = bundle(isUr).paidCheckout
    fun translateCategory(cat: String, isUr: Boolean): String = bundle(isUr).translateCategory(cat)

    // --- Orders Booked (Transactions) ---
    fun searchOrders(isUr: Boolean) = bundle(isUr).searchOrders
    fun unpaidBalance(isUr: Boolean) = bundle(isUr).unpaidBalance
    fun totalOrders(isUr: Boolean) = bundle(isUr).totalOrders
    fun totalPcsSold(isUr: Boolean) = bundle(isUr).totalPcsSold
    fun totalUnpaid(isUr: Boolean) = bundle(isUr).totalUnpaid
    fun totalItemsSold(isUr: Boolean) = bundle(isUr).totalItemsSold
    fun orderDetails(isUr: Boolean) = bundle(isUr).orderDetails
    fun orderNumber(isUr: Boolean) = bundle(isUr).orderNumber
    fun customerFactory(isUr: Boolean) = bundle(isUr).customerFactory
    fun orderStatus(isUr: Boolean) = bundle(isUr).orderStatus
    fun confirmed(isUr: Boolean) = bundle(isUr).confirmed
    fun delivered(isUr: Boolean) = bundle(isUr).delivered
    fun awaitingDelivery(isUr: Boolean) = bundle(isUr).awaitingDelivery
    fun paid(isUr: Boolean) = bundle(isUr).paid
    fun unpaid(isUr: Boolean) = bundle(isUr).unpaid
    fun partial(isUr: Boolean) = bundle(isUr).partial
    fun markDelivered(isUr: Boolean) = bundle(isUr).markDelivered
    fun recordPayment(isUr: Boolean) = bundle(isUr).recordPayment
    fun shareInvoice(isUr: Boolean) = bundle(isUr).shareInvoice
    fun deleteOrder(isUr: Boolean) = bundle(isUr).deleteOrder
    fun paymentDialogTitle(isUr: Boolean) = bundle(isUr).paymentDialogTitle
    fun paymentReceived(isUr: Boolean) = bundle(isUr).paymentReceived
    fun remainingBalance(isUr: Boolean) = bundle(isUr).remainingBalance
    fun remainingDue(isUr: Boolean) = bundle(isUr).remainingDue
    fun savePayment(isUr: Boolean) = bundle(isUr).savePayment
    fun noOrdersFound(isUr: Boolean) = bundle(isUr).noOrdersFound
    fun orderPaid(isUr: Boolean) = bundle(isUr).orderPaid
    fun orderPartial(isUr: Boolean, dueStr: String = "") = bundle(isUr).orderPartial(dueStr)
    fun orderUnpaid(isUr: Boolean) = bundle(isUr).orderUnpaid
    fun nilDue(isUr: Boolean) = bundle(isUr).nilDue
    fun deliver(isUr: Boolean) = bundle(isUr).deliver
    fun amountRs(isUr: Boolean) = bundle(isUr).amountRs
    fun receivedBy(isUr: Boolean) = bundle(isUr).receivedBy
    fun receivedIn(isUr: Boolean) = bundle(isUr).receivedIn
    fun nilFullBalance(isUr: Boolean) = bundle(isUr).nilFullBalance

    // --- Ledgers (6 Tabs) ---
    fun tabCustomerFactories(isUr: Boolean) = bundle(isUr).tabCustomerFactories
    fun tabPaint(isUr: Boolean) = bundle(isUr).tabPaint
    fun tabRawMaterial(isUr: Boolean) = bundle(isUr).tabRawMaterial
    fun tabScrap(isUr: Boolean) = bundle(isUr).tabScrap
    fun tabLabour(isUr: Boolean) = bundle(isUr).tabLabour
    fun tabCustomFactory(isUr: Boolean) = bundle(isUr).tabCustomFactory
    fun addFactory(isUr: Boolean) = bundle(isUr).addFactory
    fun addWorker(isUr: Boolean) = bundle(isUr).addWorker
    fun markAttendance(isUr: Boolean) = bundle(isUr).markAttendance
    fun addLedgerEntry(isUr: Boolean) = bundle(isUr).addLedgerEntry
    fun debitBilled(isUr: Boolean) = bundle(isUr).debitBilled
    fun creditReceived(isUr: Boolean) = bundle(isUr).creditReceived
    fun balanceDr(isUr: Boolean) = bundle(isUr).balanceDr
    fun balanceCr(isUr: Boolean) = bundle(isUr).balanceCr
    fun viewLedger(isUr: Boolean) = bundle(isUr).viewLedger
    fun partyName(isUr: Boolean) = bundle(isUr).partyName
    fun contactNumber(isUr: Boolean) = bundle(isUr).contactNumber
    fun addressCity(isUr: Boolean) = bundle(isUr).addressCity
    fun openingBalance(isUr: Boolean) = bundle(isUr).openingBalance
    fun workerType(isUr: Boolean) = bundle(isUr).workerType
    fun dailyWage(isUr: Boolean) = bundle(isUr).dailyWage
    fun perPieceRate(isUr: Boolean) = bundle(isUr).perPieceRate
    fun attendancePresent(isUr: Boolean) = bundle(isUr).attendancePresent
    fun attendanceAbsent(isUr: Boolean) = bundle(isUr).attendanceAbsent
    fun attendanceHalfDay(isUr: Boolean) = bundle(isUr).attendanceHalfDay
    fun markAllPresent(isUr: Boolean) = bundle(isUr).markAllPresent
    fun saveAttendance(isUr: Boolean) = bundle(isUr).saveAttendance
    fun pipesCoilsSupplied(isUr: Boolean) = bundle(isUr).pipesCoilsSupplied
    fun weightKg(isUr: Boolean) = bundle(isUr).weightKg
    fun scrapSold(isUr: Boolean) = bundle(isUr).scrapSold
    fun scrapBuyer(isUr: Boolean) = bundle(isUr).scrapBuyer

    // --- Stock Ready & Raw Materials ---
    fun stockReadyTitle(isUr: Boolean) = bundle(isUr).stockReadyTitle
    fun stockReadySubtitle(isUr: Boolean) = bundle(isUr).stockReadySubtitle
    fun ordersReady(isUr: Boolean) = bundle(isUr).ordersReady
    fun stockReadyValue(isUr: Boolean) = bundle(isUr).stockReadyValue
    fun noStockReady(isUr: Boolean) = bundle(isUr).noStockReady
    fun rawMaterialInventory(isUr: Boolean) = bundle(isUr).rawMaterialInventory
    fun rawInventorySubtitle(isUr: Boolean) = bundle(isUr).rawInventorySubtitle
    fun scrapInventoryTitle(isUr: Boolean) = bundle(isUr).scrapInventoryTitle
    fun scrapSubtitle(isUr: Boolean) = bundle(isUr).scrapSubtitle
    fun lowStockWarning(isUr: Boolean) = bundle(isUr).lowStockWarning
    fun available(isUr: Boolean) = bundle(isUr).available
    fun consumed(isUr: Boolean) = bundle(isUr).consumed

    // --- Expenses & Withdrawals ---
    fun operatingExpensesTitle(isUr: Boolean) = bundle(isUr).operatingExpensesTitle
    fun operatingExpensesSubtitle(isUr: Boolean) = bundle(isUr).operatingExpensesSubtitle
    fun withdrawalsTitle(isUr: Boolean) = bundle(isUr).withdrawalsTitle
    fun withdrawalsSubtitle(isUr: Boolean) = bundle(isUr).withdrawalsSubtitle
    fun totalExpenses(isUr: Boolean) = bundle(isUr).totalExpenses
    fun totalWithdrawals(isUr: Boolean) = bundle(isUr).totalWithdrawals
    fun addExpense(isUr: Boolean) = bundle(isUr).addExpense
    fun addWithdrawal(isUr: Boolean) = bundle(isUr).addWithdrawal
    fun expenseCategory(isUr: Boolean) = bundle(isUr).expenseCategory
    fun expenseTitle(isUr: Boolean) = bundle(isUr).expenseTitle
    fun recordedBy(isUr: Boolean) = bundle(isUr).recordedBy
    fun translateExpenseCategory(cat: String, isUr: Boolean): String = bundle(isUr).translateExpenseCategory(cat)

    // --- Product Returns ---
    fun returnsTitle(isUr: Boolean) = bundle(isUr).returnsTitle
    fun returnsSubtitle(isUr: Boolean) = bundle(isUr).returnsSubtitle
    fun totalUnitsReturned(isUr: Boolean) = bundle(isUr).totalUnitsReturned
    fun reworkVsScrap(isUr: Boolean) = bundle(isUr).reworkVsScrap
    fun logReturn(isUr: Boolean) = bundle(isUr).logReturn
    fun defectReason(isUr: Boolean) = bundle(isUr).defectReason
    fun resolution(isUr: Boolean) = bundle(isUr).resolution
    fun sendForRework(isUr: Boolean) = bundle(isUr).sendForRework
    fun sendToScrap(isUr: Boolean) = bundle(isUr).sendToScrap
    fun replacementSent(isUr: Boolean) = bundle(isUr).replacementSent
    fun noReturnsLogged(isUr: Boolean) = bundle(isUr).noReturnsLogged

    // --- Settings & Security ---
    fun settingsTitle(isUr: Boolean) = bundle(isUr).settingsTitle
    fun securitySection(isUr: Boolean) = bundle(isUr).securitySection
    fun securitySubtitle(isUr: Boolean) = bundle(isUr).securitySubtitle
    fun encryptionActive(isUr: Boolean) = bundle(isUr).encryptionActive
    fun encryptionSub(isUr: Boolean) = bundle(isUr).encryptionSub
    fun posPin(isUr: Boolean) = bundle(isUr).posPin
    fun posPinSub(isUr: Boolean) = bundle(isUr).posPinSub
    fun changePin(isUr: Boolean) = bundle(isUr).changePin
    fun securityQuestion(isUr: Boolean) = bundle(isUr).securityQuestion
    fun securityQuestionSub(isUr: Boolean) = bundle(isUr).securityQuestionSub
    fun updateAnswer(isUr: Boolean) = bundle(isUr).updateAnswer
    fun appearanceSection(isUr: Boolean) = bundle(isUr).appearanceSection
    fun appearanceSubtitle(isUr: Boolean) = bundle(isUr).appearanceSubtitle
    fun cloudSyncSection(isUr: Boolean) = bundle(isUr).cloudSyncSection
    fun cloudSyncSubtitle(isUr: Boolean) = bundle(isUr).cloudSyncSubtitle
    fun lastSynced(isUr: Boolean) = bundle(isUr).lastSynced
    fun never(isUr: Boolean) = bundle(isUr).never
    fun dataExportSection(isUr: Boolean) = bundle(isUr).dataExportSection
    fun dataExportSubtitle(isUr: Boolean) = bundle(isUr).dataExportSubtitle
    fun exportLedgers(isUr: Boolean) = bundle(isUr).exportLedgers
    fun languageSection(isUr: Boolean) = bundle(isUr).languageSection
    fun languageSubtitle(isUr: Boolean) = bundle(isUr).languageSubtitle
    fun english(isUr: Boolean) = bundle(isUr).english
    fun urdu(isUr: Boolean) = bundle(isUr).urdu

    // --- Lock Screen ---
    fun enterPinPrompt(isUr: Boolean) = bundle(isUr).enterPinPrompt
    fun enterPinTitle(isUr: Boolean) = bundle(isUr).enterPinTitle
    fun cashierUser(isUr: Boolean) = bundle(isUr).cashierUser
    fun incorrectPin(isUr: Boolean) = bundle(isUr).incorrectPin
    fun forgotPin(isUr: Boolean) = bundle(isUr).forgotPin
    fun defaultPinHint(isUr: Boolean) = bundle(isUr).defaultPinHint
    fun recoveryTitle(isUr: Boolean) = bundle(isUr).recoveryTitle
    fun recoveryQuestionText(isUr: Boolean) = bundle(isUr).recoveryQuestionText
    fun enterAnswerPrompt(isUr: Boolean) = bundle(isUr).enterAnswerPrompt
    fun enterNewPinPrompt(isUr: Boolean) = bundle(isUr).enterNewPinPrompt
    fun confirmNewPinPrompt(isUr: Boolean) = bundle(isUr).confirmNewPinPrompt
    fun resetPinButton(isUr: Boolean) = bundle(isUr).resetPinButton
    fun pinMismatch(isUr: Boolean) = bundle(isUr).pinMismatch
    fun incorrectAnswer(isUr: Boolean) = bundle(isUr).incorrectAnswer

    // --- Currency Format with Urdu option ---
    fun formatMoney(amount: Double, isUr: Boolean): String = bundle(isUr).formatCurrency(amount)
}
