package `in`.okcredit.collection.contract

import org.joda.time.DateTime
import java.io.Serializable

data class CollectionShareInfo(
    val customer_id: String,
    val shared_time: DateTime
) : Serializable
