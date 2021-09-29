package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class DeleteCollectionStaffLink @Inject constructor(
    private val customerRepositoryImpl: Lazy<CustomerRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    suspend fun execute() {
        val businessId = getActiveBusinessId.get().execute().await()
        return customerRepositoryImpl.get().deleteCollectionStaffLink(businessId)
    }
}
