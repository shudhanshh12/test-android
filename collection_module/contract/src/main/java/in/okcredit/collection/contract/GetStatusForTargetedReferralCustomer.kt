package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface GetStatusForTargetedReferralCustomer {
    fun execute(customerId: String): Observable<Int>
}
