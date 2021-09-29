package tech.okcredit.home.usecase

import `in`.okcredit.accounting_core.contract.SuggestedCustomerIdsForAddTransaction
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetSuggestedCustomersForAddTransactionShortcut @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val suggestedCustomerIdsForAddTransaction: Lazy<SuggestedCustomerIdsForAddTransaction>,
    private val tracker: Lazy<Tracker>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    companion object {
        const val MAX_NUMBER_OF_SUGGESTIONS = 5
        const val TAG = "GetSuggestedCustomersForAddTransactionShortcut"
    }

    fun getSuggestions(): Single<List<Customer>> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            customerRepo.get().listActiveCustomers(businessId)
                .firstOrError()
                .flatMap { customers ->
                    suggestedCustomerIdsForAddTransaction.get().getSuggestionsFromStore(businessId)
                        .map { suggestedIds -> filterSuggestedCustomers(customers, suggestedIds) }
                }.doOnSuccess {
                    if (it.size > MAX_NUMBER_OF_SUGGESTIONS) {
                        tracker.get().trackDebug("$TAG number exceeded: ${it.size}}")
                    }
                }
        }
    }

    private fun filterSuggestedCustomers(
        customers: List<Customer>,
        suggestedCustomerIds: List<String>
    ): List<Customer> {
        val suggestedCustomers = arrayListOf<Customer>()
        customers.forEach { customer ->
            if (suggestedCustomerIds.contains(customer.id)) {
                suggestedCustomers.add(customer)
                if (suggestedCustomers.size == suggestedCustomerIds.size) {
                    return suggestedCustomers
                }
            }
        }
        tracker.get().trackDebug("$TAG: some ids not found", mapOf("ids" to suggestedCustomerIds))
        return suggestedCustomers
    }
}
