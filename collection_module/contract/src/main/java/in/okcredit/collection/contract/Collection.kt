package `in`.okcredit.collection.contract

import org.joda.time.DateTime
import java.io.Serializable

data class Collection(
    val id: String,
    val create_time: DateTime,
    val update_time: DateTime,
    val status: Int,
    val payment_link: String? = null,
    val amount_requested: Long? = null,
    val amount_collected: Long? = null,
    val fee: Long? = null,
    val expire_time: DateTime? = null,
    val customer_id: String,
    val discount: Long? = null,
    val fee_category: Int = 0,
    val settlement_category: Int = 0,
    val lastSyncTime: DateTime? = null,
    val lastViewTime: DateTime? = null,
    val merchantName: String? = null,
    val paymentOriginName: String? = null,
    val paymentId: String? = null,
    val errorCode: String = "",
    val errorDescription: String = "",
    val blindPay: Boolean = false,
    val cashbackGiven: Boolean = false,
) : Serializable
