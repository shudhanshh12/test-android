package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetDefaulterCustomerList @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Observable<List<Customer>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            customerRepo.get().listCustomersByLastPayment(businessId)
                .map { it.filter { customer -> customer.canSendCollectionLink() } }
        }
    }
}
