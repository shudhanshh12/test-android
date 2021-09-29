package `in`.okcredit.backend.contract

import io.reactivex.Observable

interface GetCustomerAccountNetBalance {
    fun getNetBalance(businessId: String): Observable<Long>
}
