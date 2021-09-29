package `in`.okcredit.merchant.contract

import io.reactivex.Single

interface SyncBusiness {
    fun execute(businessId: String): Single<Business>
}
