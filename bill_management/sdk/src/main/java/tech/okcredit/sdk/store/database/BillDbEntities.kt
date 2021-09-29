package tech.okcredit.sdk.store.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

data class LocalBill(
    val id: String,
    val transactionId: String? = null,
    val accountId: String? = null,
    val createdByMe: Boolean = false,
    val localBillDocList: List<LocalBillDoc>,
    val createdAt: String,
    val updatedAt: String? = null,
    val deletedAt: String? = null,
    val note: String? = null,
    val amount: String? = null,
    val txnName: TxnName? = null,
    val billDate: String? = null,
    var status: String = "",
    val txnType: TxnType? = null,
    val deleted: Boolean = false,

)

enum class TxnName {
    PAYMENT,
    CREDIT
}

enum class TxnType(val type: Int) {
    PAYMENT(2),
    CREDIT(1),
    UNKNOWN(0)
}

@Entity(indices = [Index(value = ["billDate", "deleted"], unique = false)])
data class DBBill(
    @PrimaryKey
    val id: String,
    val transactionId: String? = null,
    val accountId: String? = null,
    val createdByMe: Boolean = false,
    val createdAt: String,
    val updatedAt: String? = null,
    val deletedAt: String? = null,
    val billDate: String? = null,
    val note: String? = null,
    val amount: String? = null,
    val txnType: Int = 0,
    val deleted: Boolean = false,
    @ColumnInfo(index = true) val businessId: String
)

@Entity
data class DbBillDoc(
    @PrimaryKey
    val id: String,
    val url: String,
    val createdAt: String,
    val updatedAt: String? = null,
    val deletedAt: String? = null,
    val billId: String,
    @ColumnInfo(index = true) val businessId: String
)

// local prefix means this entity is used in classes in bill management
data class LocalBillDoc(
    val billDocId: String,
    val url: String,
    val createdAt: String,
    val updatedAt: String? = null,
    val deletedAt: String? = null,
    val billId: String,
    var imageUrl: String? = null, // final path after searching for local copy
)

@Entity
data class Account(
    @PrimaryKey
    val accountId: String,
    val lastSeen: String,
    @ColumnInfo(index = true) val businessId: String
)
