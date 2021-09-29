package `in`.okcredit.merchant.customer_ui.ui.subscription.list.epoxy

import `in`.okcredit.merchant.customer_ui.data.server.model.request.DayOfWeek
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency

data class SubscriptionItem(
    val id: String,
    val name: String,
    val frequency: SubscriptionFrequency,
    val daysInWeek: List<DayOfWeek>?,
    val startDate: Long
)
