package `in`.okcredit.collection.contract

import io.reactivex.Completable

interface EnablePaymentAddress {

    fun execute(): Completable
}
