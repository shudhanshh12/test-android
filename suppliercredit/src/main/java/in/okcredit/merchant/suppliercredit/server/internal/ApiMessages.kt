package `in`.okcredit.merchant.suppliercredit.server.internal

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

interface ApiMessages {

    @Keep
    data class Supplier(
        val id: String,
        val registered: Boolean,
        val deleted: Boolean,
        val name: String,
        val create_time: DateTime,
        val txn_start_time: Long,
        val mobile: String?,
        val address: String?,
        val profile_image: String?,
        val balance: Long,
        val txn_alert_enabled: Boolean,
        val lang: String?,
        val add_transaction_restricted: Boolean,
        val state: Int,
        val blocked_by_supplier: Boolean,
        val restrict_contact_sync: Boolean,
    )

    @Keep
    data class Transaction(
        val id: String,
        val supplier_id: String,
        val collection_id: String?,
        val payment: Boolean,
        val amount: Long,
        val note: String?,
        val receipt_url: String?,
        val bill_date: DateTime,
        val create_time: DateTime,
        val created_by_supplier: Boolean,
        val deleted: Boolean,
        val delete_time: DateTime?,
        val deleted_by_supplier: Boolean,
        val update_time: DateTime,
        val transaction_state: Int,
        val tx_category: Int?,
    )

    @Keep
    data class UpdateSupplierRequest(
        val supplier: Supplier,
        val update_txn_alert_enabled: Boolean,
        val state: Int,
        val update_state: Boolean,
    )

    @Keep
    data class SupplierRequest(
        val name: String,
        val mobile: String?,
        val profile_image: String?,
    )

    @Keep
    data class AddTransactionRequest(
        val request_id: String,
        val transaction: Transaction,
        val mobile: String?,
    )

    @Keep
    data class SuppliersResponse(
        val suppliers: List<Supplier>,
    )

    @Keep
    data class TransactionsResponse(
        val transactions: List<Transaction>,
    )

    @Keep
    data class FeatureFindRequest(
        val feature: String,
    )

    @Keep
    data class FeatureFindResponse(
        val results: List<FeatureFindResult>,
    )

    @Keep
    data class FeatureFindResult(
        val customer_id: String?,
        val feature_enabled: Boolean,
        val restrict_customer_txn_enabled: Boolean,
        val add_transaction_restricted: Boolean,
    )

    data class Report(@SerializedName("report_url") val reportUrl: String)

    @Keep
    data class NotificationReminders(
        val id: String,
        @SerializedName("account_id")
        val accountId: String,
        @SerializedName("created_at")
        val createdAt: String,
        @SerializedName("expires_at")
        val expiresAt: String,
        val status: Int,
    )

    @Keep
    data class NotificationRemindersResponse(
        @SerializedName("notifications")
        val notificationReminders: List<NotificationReminders>,
    )

    @Keep
    data class CreateNotificationReminderResponse(
        val success: Boolean,
    )

    @Keep
    data class CreateNotificationReminderRequest(
        @SerializedName("account_id")
        val accountId: String,
    )

    @Keep
    data class NotificationReminderAction(
        val id: String,
        val action: Int,
    )

    @Keep
    data class UpdateNotificationReminder(
        val notifications: List<NotificationReminderAction>,
    )

    @Keep
    data class UpdateNotificationReminderAction(
        val id: String,
        val success: Boolean,
        val error: Error?,
    )

    @Keep
    data class Error(
        val code: String,
        val desc: String,
    )

    @Keep
    data class UpdateNotificationReminderActionResponse(
        @SerializedName("notifications")
        val updateNotificationReminderAction: List<UpdateNotificationReminderAction>,
    )
}
