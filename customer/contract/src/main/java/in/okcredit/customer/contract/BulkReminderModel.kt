package `in`.okcredit.customer.contract

data class BulkReminderModel(
    val canShowBanner: Boolean,
    val totalBalanceDue: Long = 0,
    val canShowNotificationIcon: Boolean,
    val totalReminders: Int = 0,
    val defaulterSince: Int = 14,
    val totalCustomers: Int = 0
)
