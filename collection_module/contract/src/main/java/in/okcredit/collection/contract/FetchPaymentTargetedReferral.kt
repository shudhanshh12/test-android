package `in`.okcredit.collection.contract

import io.reactivex.Completable

interface FetchPaymentTargetedReferral {

    fun execute(): Completable
}
