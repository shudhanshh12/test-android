package `in`.okcredit.merchant.core.model

import `in`.okcredit.merchant.core.common.Timestamp
import androidx.annotation.Keep

@Keep
data class TransactionAmountHistory(
    val transactionId: String,
    var amount: String? = null,
    var amountUpdated: Boolean? = null,
    var amountUpdatedAt: Timestamp? = null,
    var initialTransactionAmount: Long? = null,
    var initialTransactionCreatedAt: Timestamp? = null,
    val history: List<History> = listOf()
)

@Keep
data class History(
    var oldAmount: Long? = null,
    var newAmount: Long? = null,
    var createdAt: Timestamp? = null
)
