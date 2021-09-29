package `in`.okcredit.backend._offline.server.internal

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

class UpdateCustomerRequest(
    @SerializedName("description") private val desc: String,
    @SerializedName("mobile") private val mobile: String?,
    @SerializedName("address") private val address: String?,
    @SerializedName("profile_image") private val profileImage: String?,
    @SerializedName("lang") private val lang: String?,
    @SerializedName("reminder_mode") private val reminderMode: String?,
    @SerializedName("txn_alert_enabled") private val txnAlertEnabled: Boolean,
    @SerializedName("update_txn_alert_enabled") private val updateTxnAlertEnabled: Boolean,
    @SerializedName("due_custom_date") private val dueCustomDate: DateTime?,
    @SerializedName("update_due_custom_date") private val updateDueCustomDate: Boolean,
    @SerializedName("delete_due_custom_date") private val deleteDueCustomDate: Boolean,
    @SerializedName("add_transaction_restricted") private val addTransactionPermission: Boolean,
    @SerializedName("update_add_transaction_restricted") private val updateAddTransactionRestricted: Boolean,
    @SerializedName("state") private val blockTransaction: Int,
    @SerializedName("update_state") private val updateBlockTransaction: Boolean
)
