package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetCustomerWithPaymentDue @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(searchQuery: String = ""): Observable<CustomerSearchWrapper> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            customerRepo.get().getCustomersWithBalanceDue(businessId)
                .map { list ->
                    return@map if (searchQuery.isEmpty()) {
                        CustomerSearchWrapper(
                            searchQuery = searchQuery,
                            originalCustomerList = list,
                            filteredCustomerList = list,
                        )
                    } else {
                        CustomerSearchWrapper(
                            searchQuery = searchQuery,
                            originalCustomerList = list,
                            filteredCustomerList = list.filter {
                                searchQuery.containNameOrMobile(it.description, it.mobile)
                            },
                        )
                    }
                }
        }
    }

    data class CustomerSearchWrapper(
        val searchQuery: String,
        val originalCustomerList: List<Customer>,
        val filteredCustomerList: List<Customer>,
    )
}

fun String.containNameOrMobile(
    name: String?,
    mobile: String?,
): Boolean {
    return ((name?.lowercase()?.contains(this.lowercase())) ?: false) ||
        (mobile?.contains(this.lowercase().trim()) ?: false)
}
