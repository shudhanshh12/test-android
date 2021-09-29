package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface GetTargetedReferralInfoList {
    fun execute(): Observable<List<TargetedCustomerReferralInfo>>
}
