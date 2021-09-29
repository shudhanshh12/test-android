package tech.okcredit.user_migration.contract.models.create_customer_transaction

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

data class Transactions(
    @SerializedName("type") val type: Int?,
    @SerializedName("amount") val amount: Long?,
    @SerializedName("bill_date") val billDate: DateTime
)
