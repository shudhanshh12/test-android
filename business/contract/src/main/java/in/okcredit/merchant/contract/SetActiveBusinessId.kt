package `in`.okcredit.merchant.contract

import io.reactivex.Completable

interface SetActiveBusinessId {
    fun execute(businessId: String): Completable
}
