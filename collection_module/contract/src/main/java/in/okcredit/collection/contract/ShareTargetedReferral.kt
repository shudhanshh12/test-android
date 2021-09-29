package `in`.okcredit.collection.contract

import io.reactivex.Completable

interface ShareTargetedReferral {
    fun execute(customerMerchantId: String): Completable
}
