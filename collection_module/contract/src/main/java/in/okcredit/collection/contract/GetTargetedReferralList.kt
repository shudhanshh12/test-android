package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface GetTargetedReferralList {
    fun execute(): Observable<List<CustomerAdditionalInfo>>
}
