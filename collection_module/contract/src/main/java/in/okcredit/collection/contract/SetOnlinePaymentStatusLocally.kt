package `in`.okcredit.collection.contract

import io.reactivex.Completable

interface SetOnlinePaymentStatusLocally {

    fun execute(oldStatus: Int, newStatus: Int): Completable
}
