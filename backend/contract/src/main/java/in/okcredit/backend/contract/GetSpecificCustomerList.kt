package `in`.okcredit.backend.contract

import io.reactivex.Observable

interface GetSpecificCustomerList {
    fun execute(customerIdList: List<String>): Observable<List<Customer>>
}
