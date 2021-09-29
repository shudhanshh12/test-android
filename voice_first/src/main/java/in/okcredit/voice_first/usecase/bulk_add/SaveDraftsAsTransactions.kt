package `in`.okcredit.voice_first.usecase.bulk_add

import `in`.okcredit.backend.contract.AddFlyweightCustomerTransaction
import `in`.okcredit.backend.contract.GetIsCustomerAddTransactionRestricted
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction
import `in`.okcredit.voice_first.data.bulk_add.entities.MERCHANT_TYPE_CUSTOMER
import `in`.okcredit.voice_first.data.bulk_add.entities.MERCHANT_TYPE_SUPPLIER
import `in`.okcredit.voice_first.data.bulk_add.entities.TRANSACTION_TYPE_CREDIT
import `in`.okcredit.voice_first.data.bulk_add.entities.TRANSACTION_TYPE_PAYMENT
import `in`.okcredit.voice_first.data.bulk_add.entities.isComplete
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import merchant.okcredit.suppliercredit.contract.AddFlyweightSupplierTransaction
import merchant.okcredit.suppliercredit.contract.GetIsSupplierAddTransactionRestricted
import org.joda.time.DateTime
import javax.inject.Inject

class SaveDraftsAsTransactions @Inject constructor(
    private val analytics: Lazy<BulkAddVoiceTracker>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val addFlyweightCustomerTransaction: Lazy<AddFlyweightCustomerTransaction>,
    private val addFlyweightSupplierTransaction: Lazy<AddFlyweightSupplierTransaction>,
    private val getIsCustomerAddTransactionRestricted: Lazy<GetIsCustomerAddTransactionRestricted>,
    private val getIsSupplierAddTransactionRestricted: Lazy<GetIsSupplierAddTransactionRestricted>,
) {

    suspend fun execute(billDate: DateTime, drafts: List<DraftTransaction>) {
        val completedDrafts = drafts.filter { it.isParsed && it.isComplete() }

        val businessId = getActiveBusinessId.get().execute().await()

        val isMerchantRestricted = completedDrafts.map { it.draftMerchants!![0] }
            .associateBy({ it.merchantId }, { it.merchantType })
            .mapNotNull { (id, type) ->
                when (type) {
                    MERCHANT_TYPE_CUSTOMER -> id to getIsCustomerAddTransactionRestricted.get().execute(businessId, id)
                    MERCHANT_TYPE_SUPPLIER -> id to getIsSupplierAddTransactionRestricted.get().execute(businessId, id)
                    else -> null
                }
            }.toMap()

        completedDrafts.forEach {
            val account = it.draftMerchants?.firstOrNull() ?: return@forEach

            if (isMerchantRestricted[account.merchantId] != false) return@forEach

            val isPayment = when (it.transactionType) {
                TRANSACTION_TYPE_PAYMENT -> true
                TRANSACTION_TYPE_CREDIT -> false
                else -> return@forEach
            }

            when (account.merchantType) {
                MERCHANT_TYPE_CUSTOMER -> addFlyweightCustomerTransaction.get().execute(
                    businessId = businessId,
                    isPayment = isPayment,
                    transactionId = it.draftTransactionId,
                    customerId = account.merchantId,
                    amount = it.amount!!,
                    note = it.note,
                    billDate = billDate,
                )

                MERCHANT_TYPE_SUPPLIER -> addFlyweightSupplierTransaction.get().execute(
                    businessId = businessId,
                    isPayment = isPayment,
                    transactionId = it.draftTransactionId,
                    supplierId = account.merchantId,
                    amount = it.amount!!,
                    note = it.note,
                    billDate = billDate,
                )

                else -> return@forEach
            }
            analytics.get().logTransactionAdded(it.draftTransactionId)
        }
    }
}
