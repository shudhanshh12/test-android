package `in`.okcredit.collection.contract

import io.reactivex.Completable

interface SetPaymentOutDestination {

    fun execute(
        accountId: String,
        accountType: String,
        paymentType: String,
        paymentAddress: String
    ): Completable
}
