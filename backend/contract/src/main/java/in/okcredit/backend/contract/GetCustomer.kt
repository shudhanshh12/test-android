package `in`.okcredit.backend.contract

import `in`.okcredit.shared.usecase.Result
import io.reactivex.Observable

interface GetCustomer {

    fun execute(customerId: String?): Observable<Customer>
    fun executeObservable(customerId: String?): Observable<Result<Customer>>
}
