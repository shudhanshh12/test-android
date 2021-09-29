package `in`.okcredit.merchant.customer_ui.ui.customer

import merchant.okcredit.accounting.utils.AccountingSharedUtils.TxnGravity

/**
 * Refer to https://okcredit.atlassian.net/wiki/spaces/EN/pages/1543372994/Customer+Screen+-+Transaction+List+Logic+WIP
 * for all the logics around each item.
 */
sealed class CustomerScreenItem {
    object LoadingItem : CustomerScreenItem()

    data class EmptyPlaceHolder(val customerName: String?) : CustomerScreenItem()

    data class DateItem(val date: String) : CustomerScreenItem()

    object LoadMoreItem : CustomerScreenItem()

    data class TransactionItem(
        val id: String,
        val txnGravity: TxnGravity,
        val amount: Long,
        val date: String,
        val isDirty: Boolean,
        val image: String?,
        val imageCount: Int = 0,
        val txnTag: String?,
        val note: String?,
        val currentBalance: Long,
        val discountTransaction: Boolean,
        val deletedTxn: Boolean,
        val cashbackGiven: Boolean,
        val customerId: String,
    ) : CustomerScreenItem()

    data class ProcessingTransaction(
        val id: String,
        val paymentId: String,
        val txnGravity: TxnGravity,
        val amount: Long,
        val date: String,
        val dateTime: String,
        val currentBalance: Long,
        val statusTitle: String,
        val statusNote: String,
        val action: ProcessingTransactionAction,
    ) : CustomerScreenItem()

    enum class ProcessingTransactionAction {
        NONE,
        ADD_BANK,
        HELP,
        KYC
    }

    data class DeletedTransaction(
        val id: String,
        val txnGravity: TxnGravity,
        val isDirty: Boolean,
        val amount: Long,
        val date: String,
        val dateTime: String,
        val currentBalance: Long,
        val onlineTxn: Boolean = false,
        val collectionStatus: Int?,
        val isDeletedByCustomer: Boolean,
        val customerName: String?,
        val isBlindPay: Boolean = false,
        val accountId: String,
        val shouldShowHelpOption: Boolean,
        val supportType: String,
        val paymentId: String,
    ) : CustomerScreenItem()

    data class RequestActionItem(
        val type: Int, // 0 - add bank request, 1 - contextual online payment
        val customerName: String? = null,
    ) : CustomerScreenItem()

    data class InfoNudgeItem(
        // When type is
        // 0 - send reminder nudge for credit
        // 1 - targeted referral status < 2
        // 2 - targeted referral status == 2
        // 3 - targeted referral status == 4
        // 4 - targeted referral status == 5 to 10
        // 4 - send reminder nudge for payment
        val type: Int,
        val gravity: TxnGravity,
        val customerName: String? = null,
    ) : CustomerScreenItem()

    data class AcknowledgeActionItem(
        val type: Int, // 0 - add bank request success
    ) : CustomerScreenItem()
}
