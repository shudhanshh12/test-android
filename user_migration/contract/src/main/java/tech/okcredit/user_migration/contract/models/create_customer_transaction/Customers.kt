package tech.okcredit.user_migration.contract.models.create_customer_transaction

import com.google.gson.annotations.SerializedName

data class Customers(
    @SerializedName("name") val name: String?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("transactions") val transactions: List<Transactions>
)
