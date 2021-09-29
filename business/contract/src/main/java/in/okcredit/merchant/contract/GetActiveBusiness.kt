package `in`.okcredit.merchant.contract

import io.reactivex.Observable

interface GetActiveBusiness {

    fun execute(): Observable<Business>
}
