package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface GetCustomerAdditionalInfoList {
    fun execute(): Observable<List<CustomerAdditionalInfo>>
}
