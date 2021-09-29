package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.merchant.customer_ui.data.server.model.request.EditAction
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class EditStaffCollectionLink @Inject constructor(
    private val customerRepositoryImpl: Lazy<CustomerRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    suspend fun execute(linkId: String, customerIds: List<String>, action: EditAction): List<String> {
        val businessId = getActiveBusinessId.get().execute().await()
        customerRepositoryImpl.get().editCollectionStaffLink(linkId, customerIds, action, businessId)
        return customerIds
    }

    suspend fun execute(linkId: String, currentCustomerIds: Set<String>, newCustomerIds: Set<String>): Set<String> {
        val deletedCustomerIds = currentCustomerIds.subtract(newCustomerIds)
        val addedCustomerIds = newCustomerIds.subtract(currentCustomerIds)
        val businessId = getActiveBusinessId.get().execute().await()
        if (deletedCustomerIds.isNotEmpty()) {
            customerRepositoryImpl.get()
                .editCollectionStaffLink(linkId, deletedCustomerIds.toList(), EditAction.DELETE, businessId)
        }
        if (addedCustomerIds.isNotEmpty()) {
            customerRepositoryImpl.get()
                .editCollectionStaffLink(linkId, addedCustomerIds.toList(), EditAction.ADD, businessId)
        }
        return newCustomerIds
    }
}
