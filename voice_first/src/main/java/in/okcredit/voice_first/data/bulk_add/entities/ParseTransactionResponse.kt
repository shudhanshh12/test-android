package `in`.okcredit.voice_first.data.bulk_add.entities

import androidx.annotation.Keep

@Keep
data class ParseTransactionResponse(
    val draft: DraftTransaction,
)
