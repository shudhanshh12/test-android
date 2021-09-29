package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface GetCustomerCollectionProfile {
    fun execute(customerId: String): Observable<CollectionCustomerProfile>
}
