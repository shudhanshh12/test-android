package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetAllDueCustomersByLastPayment @Inject constructor(
    private val customerRepo: CustomerRepo,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Observable<List<Customer>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            customerRepo.listCustomersByLastPayment(businessId).map { it ->
                val dueCustomers: List<Customer> = it.filter { it.canSendCollectionLink() }
                dueCustomers
            }
        }
    }
}
