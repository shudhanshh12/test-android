package `in`.okcredit.merchant.customer_ui.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import merchant.android.okstream.contract.OkStreamService
import org.junit.Test

class CustomerEventTrackerTest {
    private val analyticsProvider: AnalyticsProvider = mock()
    private val okStreamService: OkStreamService = mock()
    private val customerEventTracker = CustomerEventTracker({ analyticsProvider }, { okStreamService })

    @Test
    fun `trackSelectProfile should correct event with properties`() {
        customerEventTracker.trackSelectProfile(
            accountId = "1234",
            screen = "screen",
            relation = "relation",
            field = "field"
        )

        verify(analyticsProvider).trackEvents(
            eventName = "Select Profile",
            properties = mapOf(
                "Relation" to "relation",
                "account_id" to "1234",
                "Field" to "field",
                "Screen" to "screen"
            )
        )
    }

    @Test
    fun `trackSkipSelectProfile should correct event with properties`() {
        customerEventTracker.trackSkipSelectProfile(
            accountId = "1234",
            screen = "screen",
            relation = "relation",
            field = "field"
        )

        verify(analyticsProvider).trackEvents(
            eventName = "Skip Select Profile",
            properties = mapOf(
                "Relation" to "relation",
                "account_id" to "1234",
                "Field" to "field",
                "Screen" to "screen"
            )
        )
    }

    @Test
    fun `trackUpdateProfile should correct event with properties`() {
        customerEventTracker.trackUpdateProfile(
            accountId = "1234",
            screen = "screen",
            relation = "relation",
            field = "field"
        )

        verify(analyticsProvider).trackEvents(
            eventName = "Update Profile",
            properties = mapOf(
                "Relation" to "relation",
                "account_id" to "1234",
                "Field" to "field",
                "Screen" to "screen"
            )
        )
    }

    @Test
    fun `trackUpdateProfileFailed should correct event with properties`() {
        customerEventTracker.trackUpdateProfileFailed(
            accountId = "1234",
            screen = "screen",
            relation = "relation",
            field = "field",
            reason = "reason"
        )

        verify(analyticsProvider).trackEvents(
            eventName = "Update Profile Failed",
            properties = mapOf(
                "Relation" to "relation",
                "account_id" to "1234",
                "Field" to "field",
                "Screen" to "screen",
                "Reason" to "reason"
            )
        )
    }

    @Test
    fun `trackDateRangeClick should correct event with properties`() {
        customerEventTracker.trackDateRangeClick(
            accountId = "1234",
            screen = "screen",
            relation = "relation",
        )

        verify(analyticsProvider).trackEvents(
            eventName = "acct_report_date_click",
            properties = mapOf(
                "Relation" to "relation",
                "account_id" to "1234",
                "Screen" to "screen",
            )
        )
    }

    @Test
    fun `trackDateRangeUpdate should correct event with properties`() {
        customerEventTracker.trackDateRangeUpdate(
            accountId = "1234",
            screen = "screen",
            relation = "relation",
            value = "value",
            dateRange = mutableListOf("date1", "date2")
        )

        verify(analyticsProvider).trackEvents(
            eventName = "acct_report_date_update",
            properties = mapOf(
                "Relation" to "relation",
                "account_id" to "1234",
                "Screen" to "screen",
                "date_range" to mutableListOf("date1", "date2"),
                "Value" to "value"
            )
        )
    }

    @Test
    fun `trackDateRangeCancel should correct event with properties`() {
        customerEventTracker.trackPopUpClicked(
            customerId = "1234",
            relationCustomer = CustomerEventTracker.RELATION_CUSTOMER,
            relationshipScreen = CustomerEventTracker.RELATIONSHIP_SCREEN,
            contextualType = CustomerEventTracker.CONTEXTUAL_TYPE,
            action = CustomerEventTracker.DISMISS
        )

        verify(analyticsProvider).trackEvents(
            eventName = "Popup Clicked",
            properties = mapOf(
                "Relation" to "Customer",
                "Type" to "Contextual Permissioning",
                "Action" to "Dismiss",
                "account_id" to "1234",
                "Screen" to "Relationship"
            )
        )
    }

    @Test
    fun `trackRuntimePermission should correct event with properties`() {
        customerEventTracker.trackRuntimePermission(
            screen = CustomerEventTracker.CUSTOMER_REPORTS_SCREEN,
            type = PropertyValue.STORAGE,
            granted = true
        )

        verify(analyticsProvider).trackEvents(
            "Grant Permission",
            mapOf(
                "Type" to "Storage", "Screen" to "Customer Reports Screen"
            )
        )
    }

    @Test
    fun `trackInAppNotificationDisplayed should correct event with properties`() {
        customerEventTracker.trackInAppNotificationDisplayed(
            screen = CustomerEventTracker.CUSTOMER_REPORT_SCREEN,
            type = CustomerEventTracker.REPORT_DATE,
            accountId = "1234"
        )

        verify(analyticsProvider).trackEvents(
            "InAppNotification Displayed",
            mapOf(
                "Type" to "Report Date", "account_id" to "1234", "Screen" to "CustomerReport"
            )
        )
    }

    @Test
    fun `trackInputCalculator should correct event with properties`() {
        customerEventTracker.trackInputCalculator(
            operatorsUsed = "MAD",
            accountId = "1234",
        )

        verify(analyticsProvider).trackEvents(
            "Input Calculator",
            mapOf(
                "Type" to "MAD",
                "account_id" to "1234",
                "Relation" to "Customer"
            )
        )
    }

    @Test
    fun `trackInputCalculatorError should correct event with properties`() {
        customerEventTracker.trackInputCalculatorError(
            operatorsUsed = "MAD",
            accountId = "1234",
        )

        verify(analyticsProvider).trackEvents(
            "Input Calculator Error",
            mapOf(
                "Type" to "MAD",
                "account_id" to "1234",
                "Relation" to "Customer"
            )
        )
    }

    @Test
    fun `trackUpdateBillDate should correct event with properties`() {
        customerEventTracker.trackUpdateBillDate(
            default = false,
            accountId = "1234",
        )

        verify(analyticsProvider).trackEvents(
            "Update Bill Date",
            mapOf(
                "default" to false,
                "account_id" to "1234",
                "Relation" to "Customer"
            )
        )
    }

    @Test
    fun `trackSelectBillDate should correct event with properties`() {
        customerEventTracker.trackSelectBillDate(
            accountId = "1234",
        )

        verify(analyticsProvider).trackEvents(
            "Select Bill Date",
            mapOf(
                "account_id" to "1234",
                "Relation" to "Customer"
            )
        )
    }

    @Test
    fun `trackAddNoteStarted should correct event with properties`() {
        customerEventTracker.trackAddNoteStarted(
            type = "Credit",
            screenView = "full_view",
            accountId = "1234",
        )

        verify(analyticsProvider).trackEvents(
            "Add Note Started",
            mapOf(
                "Type" to "Credit",
                "screen_view" to "full_view",
                "Flow" to "Add Transaction",
                "account_id" to "1234",
                "Relation" to "Customer"
            )
        )
    }

    @Test
    fun `trackAddNoteVoiceClicked given collectingSample false should correct event with properties`() {
        customerEventTracker.trackAddNoteVoiceClicked(
            accountId = "1234",
        )

        verify(analyticsProvider).trackEvents(
            "voice_note_clicked",
            mapOf(
                "Flow" to "Add Transaction",
                "account_id" to "1234",
                "Relation" to "Customer",
                "collecting_sample" to false
            )
        )
    }

    @Test
    fun `trackTransactionDetails fires event with correct properties`() {
        customerEventTracker.trackTransactionDetails(
            amount = 1000L,
            billDate = "12/03/2021",
            customerId = "123"
        )

        verify(analyticsProvider).trackEvents(
            "Transaction Details Track",
            mapOf(
                "Amount" to 1000L,
                "BillDate" to "12/03/2021",
                "Transaction_id" to "not_assigned_yet",
                "Customer_id" to "123",
                "Screen" to "AddTransactionScreen",
            )
        )
    }

    @Test
    fun `trackAddNoteVoiceClicked given collectingSample true should correct event with properties`() {
        customerEventTracker.trackAddNoteVoiceClicked(
            accountId = "1234",
            collectingSample = true
        )

        verify(analyticsProvider).trackEvents(
            "voice_note_clicked",
            mapOf(
                "Flow" to "Add Transaction",
                "account_id" to "1234",
                "Relation" to "Customer",
                "collecting_sample" to true
            )
        )
    }

    @Test
    fun `trackPaymentPasswordSkip should correct event with properties`() {
        customerEventTracker.trackPaymentPasswordSkip(
            accountId = "1234",
        )

        verify(analyticsProvider).trackEvents(
            "payment_password_skip",
            mapOf(
                "account_id" to "1234",
                "Relation" to "Customer",
                "screen" to "Relationship",
            )
        )
    }

    @Test
    fun `trackAddTransactionLoaded should correct event with properties`() {
        customerEventTracker.trackAddTransactionLoaded(
            accountId = "1234",
            type = "Credit",
            screenView = "full_view",
        )

        verify(analyticsProvider).trackEvents(
            "transaction_page_load",
            mapOf(
                "Type" to "Credit",
                "screen_view" to "full_view",
                "account_id" to "1234",
                "Relation" to "Customer",
            )
        )
    }

    @Test
    fun `trackAddTransactionAmountEntered should correct event with properties`() {
        customerEventTracker.trackAddTransactionAmountEntered(
            accountId = "1234",
            type = "Credit",
            screenView = "full_view",
        )

        verify(analyticsProvider).trackEvents(
            "transaction_amount_entered",
            mapOf(
                "Type" to "Credit",
                "screen_view" to "full_view",
                "account_id" to "1234",
                "Relation" to "Customer",
            )
        )
    }

    @Test
    fun `trackAddTransactionClosed should correct event with properties`() {
        customerEventTracker.trackAddTransactionClosed(
            accountId = "1234",
            type = "Credit",
            screenView = "full_view",
        )

        verify(analyticsProvider).trackEvents(
            "transaction_screen_close",
            mapOf(
                "Type" to "Credit",
                "screen_view" to "full_view",
                "account_id" to "1234",
                "Relation" to "Customer",
            )
        )
    }

    @Test
    fun `trackInAppNotificationClicked should correct event with properties`() {
        customerEventTracker.trackInAppNotificationClicked(
            accountId = "1234",
            type = "Credit",
            screen = "screen",
            focalArea = false
        )

        verify(analyticsProvider).trackEvents(
            "InAppNotification Clicked",
            mapOf(
                "Screen" to "screen",
                "Type" to "Credit",
                "account_id" to "1234",
                "Focal Area" to false
            )
        )
    }

    @Test
    fun `trackAddTransactionConfirmed should correct event with properties`() {
        customerEventTracker.trackAddTransactionConfirmed(
            customerId = "1234",
            type = "Credit",
            screen = "screen",
            amount = "1000",
            calculatorUsed = false,
            txnId = "txn_id",
            source = "source",
            commonLedger = true,
            customerSyncStatus = "",
            notes = "notes"
        )

        verify(analyticsProvider).trackEvents(
            "Add Transaction: Confirm",
            mapOf(
                "Type" to "Credit",
                "customer_id" to "1234",
                "amount" to "1000",
                "Relation" to "Customer",
                "Screen" to "screen",
                "if_calculator_used" to "no",
                "tx_id" to "txn_id",
                "Source" to "source",
                "Common ledger" to true,
                "CustomerSyncSuccess" to "",
                "Notes" to "notes",
            )
        )
    }

    @Test
    fun `trackOperatorsClicked should correct event with properties`() {
        customerEventTracker.trackOperatorsClicked(
            customerId = "1234",
        )

        verify(analyticsProvider).trackEvents(
            "calculator_symbols_clicked",
            mapOf(
                "customer_id" to "1234",
            )
        )
    }

    @Test
    fun `trackOptOutFromVoiceSamplesCollection should fire correct event`() {
        customerEventTracker.trackOptOutFromVoiceSamplesCollection()

        verify(analyticsProvider).trackEvents(
            "Opt Out Voice Collection", null
        )
    }

    @Test
    fun `trackPopUpClosed should fire correct event`() {
        customerEventTracker.trackPopUpClosed(type = "type")

        verify(analyticsProvider).trackEvents(
            "PopUp Closed",
            mapOf(
                "Type" to "type",
            )
        )
    }

    @Test
    fun `trackInAppNotificationDisplayed should fire correct event`() {
        customerEventTracker.trackInAppNotificationDisplayed(type = "type", screen = "screen")

        verify(analyticsProvider).trackEvents(
            "InAppNotification Displayed",
            mapOf(
                "Type" to "type",
                "Screen" to "screen",
            )
        )
    }

    @Test
    fun `trackInAppNotificationClicked should fire correct event`() {
        customerEventTracker.trackInAppNotificationClicked(
            type = "type",
            screen = "screen",
            focalArea = false
        )

        verify(analyticsProvider).trackEvents(
            "InAppNotification Clicked",
            mapOf(
                "Type" to "type",
                "Screen" to "screen",
                "Focal Area" to false
            )
        )
    }

    @Test
    fun `trackCustomerTxnAlertPopUpDisplayed should fire correct event`() {
        customerEventTracker.trackCustomerTxnAlertPopUpDisplayed(
            customerId = "1234",
            relationCustomer = "Customer",
            relationshipScreen = "relationshipScreen",
            contextualType = "contextualType"
        )

        verify(analyticsProvider).trackEvents(
            "Popup Displayed",
            mapOf(
                "Relation" to "Customer",
                "Screen" to "relationshipScreen",
                "Type" to "contextualType",
                "account_id" to "1234"
            )
        )
    }
}
