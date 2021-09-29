package `in`.okcredit.backend._offline.server.internal

import com.google.gson.annotations.SerializedName
import merchant.okcredit.accounting.model.TransactionImage

class CreateTransactionImageRequest(
    @SerializedName("merchant_id") var merchantId: String,
    @SerializedName(
        "image"
    ) var image: TransactionImage
)
