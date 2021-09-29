package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface GetKycStatus {
    fun execute(shouldFetchWhenCollectionNotAdopted: Boolean = false): Observable<KycStatus>
}
