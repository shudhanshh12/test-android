package `in`.okcredit.merchant.customer_ui.ui.staff_link.edit

import org.joda.time.DateTime

data class StaffLinkEditDetailsCustomerItem(
    val id: String,
    val name: String,
    val profilePic: String?,
    val balance: Long,
    val address: String?,
    val mobile: String?,
    val showPaymentReceived: Boolean = false,
    val lasPayment: DateTime?
)
