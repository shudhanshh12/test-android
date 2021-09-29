package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionStatus
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.collection.contract.SetOnlinePaymentStatusLocally
import `in`.okcredit.collection.contract.TriggerMerchantPayout
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsContract
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class TriggerMerchantPayoutImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val setOnlinePaymentStatusLocally: Lazy<SetOnlinePaymentStatusLocally>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val setOnlinePaymentStatusForRefundTxn: Lazy<SetOnlinePaymentStatusForRefundTxn>,
) : TriggerMerchantPayout {

    override fun executePayout(
        paymentType: String,
        collectionType: String,
        payoutId: String,
        paymentId: String,
    ): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            collectionRepository.get()
                .triggerMerchantPayout(paymentType, collectionType, payoutId, paymentId, businessId)
                .andThen(rxCompletable { collectionSyncer.get().executeSyncOnlinePayments(businessId) })
                .andThen(
                    setOnlinePaymentStatusLocally.get()
                        .execute(CollectionStatus.PAYOUT_FAILED, CollectionStatus.PAYOUT_INITIATED)
                )
        }
    }

    override fun executeRefund(
        paymentType: String,
        collectionType: String,
        payoutId: String,
        paymentId: String,
        txnId: String,
    ): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            collectionRepository.get().triggerMerchantPayout(paymentType, collectionType, payoutId, paymentId, businessId)
                .andThen(
                    rxCompletable { collectionSyncer.get().executeSyncOnlinePayments(businessId) }
                ).andThen(
                    setOnlinePaymentStatusForRefundTxn.get()
                        .execute(
                            txnId,
                            OnlinePaymentsContract.PaymentStatus.REFUND_INITIATED.value
                        )
                )
        }
    }
}
