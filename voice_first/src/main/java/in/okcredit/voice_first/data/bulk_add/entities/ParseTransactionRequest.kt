package `in`.okcredit.voice_first.data.bulk_add.entities

import androidx.annotation.Keep

@Keep
data class ParseTransactionRequest(
    val draft_transaction_id: String,
    val voice_transcript: String,
)
