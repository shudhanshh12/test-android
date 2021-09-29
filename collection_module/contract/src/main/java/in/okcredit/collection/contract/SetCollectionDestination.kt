package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface SetCollectionDestination {

    fun execute(
        collectionMerchantProfile: CollectionMerchantProfile,
        async: Boolean = false
    ): Observable<CollectionMerchantProfile>
}
