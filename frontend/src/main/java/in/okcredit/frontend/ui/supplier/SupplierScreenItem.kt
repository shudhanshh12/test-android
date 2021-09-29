package `in`.okcredit.frontend.ui.supplier

import merchant.okcredit.accounting.utils.AccountingSharedUtils.TxnGravity
import org.joda.time.DateTime

sealed class SupplierScreenItem {
    object LoadingItem : SupplierScreenItem()

    object NetworkErrorItem : SupplierScreenItem()

    data class EmptyPlaceHolder(val supplierName: String) : SupplierScreenItem()

    object LoadMoreItem : SupplierScreenItem()

    data class DateItem(val date: String) : SupplierScreenItem()

    data class TransactionItem(
        val txnId: String,
        val payment: Boolean,
        val amount: Long,
        val note: String?,
        val date: String,
        val syncing: Boolean,
        val receiptUrl: String?,
        val finalReceiptUrl: String?, // url post local copy exist checks
        val isOnlineTxn: Boolean,
        val createdBySupplier: Boolean,
        val currentDue: Long = 0L,
        val supplierName: String?,
        val createTime: DateTime?,
    ) : SupplierScreenItem()

    data class ProcessingTransaction(
        val txnId: String,
        val payment: Boolean,
        val amount: Long,
        val isBlindPay: Boolean,
        val billDate: DateTime,
        val createTime: DateTime,
        val currentDue: Long,
        val shouldShowHelpOption: Boolean,
        val accountId: String,
        val supportType: String,
        val paymentId: String,
    ) : SupplierScreenItem()

    data class DeletedTransaction(
        val id: String,
        val txnGravity: TxnGravity,
        val isDirty: Boolean,
        val amount: Long,
        val date: String,
        val dateTime: String,
        val currentBalance: Long,
        val onlineTxn: Boolean,
        val collectionStatus: Int?,
        val isDeletedBySupplier: Boolean,
        val supplierName: String?,
        val isBlindPay: Boolean,
        val shouldShowHelpOption: Boolean,
        val accountId: String,
        val supportType: String,
        val paymentId: String,
    ) : SupplierScreenItem()
}
