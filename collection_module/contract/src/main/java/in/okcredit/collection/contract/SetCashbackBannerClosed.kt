package `in`.okcredit.collection.contract

import io.reactivex.Completable

interface SetCashbackBannerClosed {
    fun execute(customerId: String): Completable
}
