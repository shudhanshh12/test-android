package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface GetCollectionMerchantProfile {

    fun execute(): Observable<CollectionMerchantProfile>
}
