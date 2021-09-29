package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.merchant.customer_ui.data.server.model.response.ActiveStaffLinkResponse
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class GetActiveStaffLinkDetails @Inject constructor(
    private val customerRepositoryImpl: Lazy<CustomerRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    suspend fun execute(): ActiveStaffLinkResponse {
        val businessId = getActiveBusinessId.get().execute().await()
        return (customerRepositoryImpl.get().activeStaffLinkDetails(businessId))
    }

    data class ActiveStaffLinkDetails(
        val link: String,
        val customers: List<Customer>,
    )
}
