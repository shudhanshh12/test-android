package `in`.okcredit.merchant.customer_ui.ui.staff_link.add

import android.text.Spanned

data class CustomerItem(
    val id: String,
    val profilePic: String?,
    val name: String,
    val subTitle: Spanned,
    var selected: Boolean,
)
