package `in`.okcredit.merchant.customer_ui.data.server.model.response

enum class SubscriptionFrequency(val value: Int, val analyticsName: String) {
    DAILY(1, "daily"),

    WEEKLY(2, "weekly"),

    MONTHLY(3, "monthly");

    companion object {
        @JvmStatic
        fun getFrequency(frequency: Int) = when (frequency) {
            DAILY.value -> DAILY
            WEEKLY.value -> WEEKLY
            MONTHLY.value -> MONTHLY
            else -> DAILY
        }
    }
}
