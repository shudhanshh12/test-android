package `in`.okcredit.collection.contract

import io.reactivex.Completable

interface TriggerMerchantPayout {

    fun executePayout(paymentType: String, collectionType: String, payoutId: String, paymentId: String): Completable
    fun executeRefund(
        paymentType: String,
        collectionType: String,
        payoutId: String,
        paymentId: String,
        txnId: String
    ): Completable
}
