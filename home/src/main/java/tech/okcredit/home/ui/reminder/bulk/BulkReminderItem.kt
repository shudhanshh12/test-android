package tech.okcredit.home.ui.reminder.bulk

data class BulkReminderItem(
    val customerId: String,
    val profilePic: String?,
    val name: String?,
    val amountDue: Long,
    var checked: Boolean = false,
)
