package `in`.okcredit.merchant.core.server.internal.quick_add_transaction

import `in`.okcredit.merchant.core.server.internal.CoreApiMessages
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class QuickAddTransactionResponse(
    val apiCustomer: CoreApiMessages.ApiCustomer,
    val transaction: CoreApiMessages.Transaction,
)
