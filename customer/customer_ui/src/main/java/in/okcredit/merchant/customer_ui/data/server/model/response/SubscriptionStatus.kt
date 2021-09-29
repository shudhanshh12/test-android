package `in`.okcredit.merchant.customer_ui.data.server.model.response

import com.google.gson.annotations.SerializedName

enum class SubscriptionStatus(val value: Int) {
    @SerializedName("0")
    UNKNOWN_STATUS(0),

    @SerializedName("1")
    ACTIVE(1),

    @SerializedName("2")
    DELETED(2),

    @SerializedName("3")
    PAUSED(3),

    @SerializedName("4")
    EXPIRED(4);

    companion object {
        @JvmStatic
        fun getStatus(status: Int) = when (status) {
            ACTIVE.value -> ACTIVE
            DELETED.value -> DELETED
            PAUSED.value -> PAUSED
            EXPIRED.value -> EXPIRED
            else -> UNKNOWN_STATUS
        }
    }
}
