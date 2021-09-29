package `in`.okcredit.collection.contract

import io.reactivex.Single

interface GetCashbackBannerClosed {
    fun execute(customerId: String): Single<Boolean>
}
