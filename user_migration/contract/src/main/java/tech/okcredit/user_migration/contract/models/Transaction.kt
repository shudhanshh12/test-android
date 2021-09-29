package tech.okcredit.user_migration.contract.models

import com.google.gson.annotations.SerializedName
import tech.okcredit.android.base.utils.DateTimeUtils

data class Transaction(
    @SerializedName("amount") val amount: Long? = 0,
    @SerializedName("transaction_object_id") val transaction_object_id: String? = "",
    @SerializedName("type") val type: Int? = 1,
    @SerializedName("date") val date: Long? = DateTimeUtils.currentDateTime().millis
)
