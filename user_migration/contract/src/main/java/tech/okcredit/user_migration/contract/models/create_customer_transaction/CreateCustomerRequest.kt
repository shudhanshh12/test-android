package tech.okcredit.user_migration.contract.models.create_customer_transaction

import com.google.gson.annotations.SerializedName

data class CreateCustomerRequest(
    @SerializedName("merchant_id") val merchant_id: String,
    @SerializedName("customers") val customers: List<Customers>
)
