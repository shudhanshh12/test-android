package `in`.okcredit.backend._offline.model

import androidx.annotation.Keep

@Keep
data class TransactionImageAdapter(
    var id: String?,
    var request_id: String,
    val transaction_id: String?,
    var url: String,
    var create_time: Long
)
