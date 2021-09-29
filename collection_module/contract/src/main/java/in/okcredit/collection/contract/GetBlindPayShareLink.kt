package `in`.okcredit.collection.contract

import io.reactivex.Single

interface GetBlindPayShareLink {
    fun execute(paymentId: String): Single<String>
}
