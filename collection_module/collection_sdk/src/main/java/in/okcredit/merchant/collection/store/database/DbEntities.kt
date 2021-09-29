package `in`.okcredit.merchant.collection.store.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity
data class Collection(
    @PrimaryKey
    val id: String,
    val create_time: DateTime,
    val update_time: DateTime,
    val status: Int,
    val payment_link: String,
    val amount_requested: Long?,
    val amount_collected: Long?,
    val fee: Long?,
    val expire_time: DateTime?,
    val customer_id: String,
    val discount: Long?,
    val fee_category: Int,
    val settlement_category: Int,
    val lastSyncTime: DateTime?,
    val lastViewTime: DateTime?,
    val merchantName: String?,
    val paymentOriginName: String?,
    val paymentId: String?,
    val errorCode: String = "",
    val errorDescription: String = "",
    val blindPay: Boolean = false,
    val cashbackGiven: Boolean = false,
    @ColumnInfo(index = true) val businessId: String,
)

@Entity
data class CollectionProfile(
    @PrimaryKey
    val merchant_id: String, // TODO rename column to businessId
    val name: String?,
    val payment_address: String,
    val type: String,
    val merchant_vpa: String?,
    val limit_type: String? = null,
    val kyc_limit: Long = 0L,
    val remaining_limit: Long = 0L,
    val merchant_qr_enabled: Boolean = false,
)

@Entity
data class CustomerCollectionProfile(
    @PrimaryKey
    val customerId: String,
    val messageLink: String?,
    val message: String?,
    val qrIntent: String?,
    val showImage: Boolean,
    val linkId: String?,
    val googlePayEnabled: Boolean,
    val paymentIntent: Boolean,
    val destinationUpdateAllowed: Boolean = true,
    val cashbackEligible: Boolean = false,
    @ColumnInfo(index = true) val businessId: String,
)

@Entity
data class SupplierCollectionProfile(
    @PrimaryKey
    val accountId: String,
    val messageLink: String?,
    val linkId: String?,
    val name: String?,
    val type: String?,
    val paymentAddress: String?,
    val destinationUpdateAllowed: Boolean = true,
    @ColumnInfo(index = true) val businessId: String,
)

@Entity
data class CollectionShareInfo(
    @PrimaryKey
    var customer_id: String,
    var shared_time: DateTime,
    @ColumnInfo(index = true) val businessId: String,
)

@Entity
data class KycExternalEntity(
    @PrimaryKey
    var merchantId: String,
    var kyc: String,
    var upiDailyLimit: Long,
    var nonUpiDailyLimit: Long,
    var upiDailyTransactionAmount: Long,
    var nonUpiDailyTransactionAmount: Long,
    var category: String,
)

@Entity
data class CollectionOnlinePaymentEntity(
    @PrimaryKey
    var id: String,
    var createdTime: DateTime,
    var updatedTime: DateTime,
    var status: Int,
    var merchantId: String,
    var accountId: String,
    var amount: Double,
    var paymentId: String,
    var payoutId: String,
    var paymentSource: String,
    var paymentMode: String,
    var type: String,
    var read: Boolean = false,
    val errorCode: String = "",
    val errorDescription: String = "",
    @ColumnInfo(index = true) val businessId: String,
)

@Entity
data class CustomerAdditionalInfoEntity(
    @PrimaryKey
    val id: String,
    val link: String,
    val status: Int,
    val amount: Long,
    val message: String,
    val youtubeLink: String,
    val customerMerchantId: String,
    val ledgerSeen: Boolean = false,
    @ColumnInfo(index = true) val businessId: String,
)

data class CustomerWithQrIntent(
    var customer_id: String,
    var qr_intent: String?,
)
