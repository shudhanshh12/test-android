package `in`.okcredit.collection.contract

import org.joda.time.DateTime

data class CollectionOnlinePayment(
    val id: String,
    val createdTime: DateTime,
    val updatedTime: DateTime,
    val status: Int,
    val merchantId: String? = null,
    val accountId: String,
    val amount: Double,
    val paymentId: String,
    val payoutId: String? = null,
    val paymentSource: String? = null,
    val paymentMode: String? = null,
    val type: String,
    val read: Boolean = false,
    val errorCode: String,
    val errorDescription: String,
)
