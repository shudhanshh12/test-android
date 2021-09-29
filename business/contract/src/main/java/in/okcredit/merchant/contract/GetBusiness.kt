package `in`.okcredit.merchant.contract

import io.reactivex.Observable

interface GetBusiness {
    fun execute(businessId: String): Observable<Business>
}
