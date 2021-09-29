package `in`.okcredit.backend._offline.server.internal

import `in`.okcredit.backend._offline.serverV2.internal.ApiMessagesV2
import androidx.annotation.Keep

@Keep
data class QuickAddTransactionResponse(
    val customer: Customer,
    val transaction: ApiMessagesV2.Transaction
)
