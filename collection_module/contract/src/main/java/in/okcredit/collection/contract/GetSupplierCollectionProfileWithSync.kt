package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface GetSupplierCollectionProfileWithSync {

    fun execute(supplierId: String, async: Boolean): Observable<CollectionCustomerProfile>
}
