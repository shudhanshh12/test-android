package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.collection.contract.GetCollectionActivationStatus
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.awaitFirst
import javax.inject.Inject

class CreateStaffCollectionLink @Inject constructor(
    private val getCollectionActivationStatus: Lazy<GetCollectionActivationStatus>,
    private val customerRepositoryImpl: Lazy<CustomerRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    suspend fun execute(customerIds: List<String>): StaffLinkSummary {
        val isCollectionActive = getCollectionActivationStatus.get().execute().awaitFirst()
        val businessId = getActiveBusinessId.get().execute().await()

        if (!isCollectionActive) {
            throw CollectionNotActivatedError()
        }

        val response = customerRepositoryImpl.get().createCustomerStaffLink(customerIds, businessId)
        return StaffLinkSummary(
            linkId = response.linkId,
            link = response.link,
            customerIds = customerIds,
            linkCreateTime = System.currentTimeMillis()
        )
    }

    class CollectionNotActivatedError : Exception()

    data class StaffLinkSummary(
        val linkId: String,
        val link: String,
        val customerIds: List<String>,
        val linkCreateTime: Long,
    )
}
