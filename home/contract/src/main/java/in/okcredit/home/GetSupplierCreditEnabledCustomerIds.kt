package `in`.okcredit.home

import io.reactivex.Observable

interface GetSupplierCreditEnabledCustomerIds {
    fun execute(businessId: String? = null): Observable<String>
}
