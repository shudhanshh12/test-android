package `in`.okcredit.merchant.suppliercredit.model

data class NotificationReminderData(
    val createdAt: String,
    val name: String,
    val profileImage: String?,
    val balance: Int?,
    val lastPayment: Int?,
    val lastPaymentDate: Long?,
    val accountId: String,
    val notificationId: String,
)

data class NotificationReminderForUi(
    val totalNotificationCount: Int,
    val name: String,
    val profileImage: String?,
    val balance: String,
    val lastPayment: String?,
    val lastPaymentDate: String?,
    val accountId: String,
    val notificationId: String,
    val balanceInPaisa: String,
    val lastPaymentInPaisa: String?
)
