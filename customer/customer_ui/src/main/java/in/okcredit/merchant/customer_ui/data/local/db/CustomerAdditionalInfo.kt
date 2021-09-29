package `in`.okcredit.merchant.customer_ui.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CustomerAdditionalInfo(
    @PrimaryKey
    val customer_id: String,
    val collectionContextualEnabled: Boolean = false,
    val txnIdForCollectionTrigger: String? = null,
    val txnCountOnPaymentIntentTrigger: Int = 0,
    @ColumnInfo(index = true) val businessId: String
)
