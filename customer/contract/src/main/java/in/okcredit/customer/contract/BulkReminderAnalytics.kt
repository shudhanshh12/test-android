package `in`.okcredit.customer.contract

interface BulkReminderAnalytics {

    fun trackEntryPointViewed()
    fun trackEntryPointClicked(bellIconClicked: Boolean, numberOfReminders: Int)
}
