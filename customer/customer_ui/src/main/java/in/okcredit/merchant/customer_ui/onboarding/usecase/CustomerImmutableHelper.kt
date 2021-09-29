package `in`.okcredit.merchant.customer_ui.onboarding.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.model.Customer
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import javax.inject.Inject
import `in`.okcredit.backend.contract.Customer as backend_customer_model

class CustomerImmutableHelper @Inject constructor(
    private val coreSdk: Lazy<CoreSdk>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    private suspend fun getCleanCustomerByMobile(mobile: String): Customer? {
        val businessId = getActiveBusinessId.get().execute().await()
        return coreSdk.get().getCustomersByMobile(mobile, businessId)
            .firstOrNull { it.customerSyncStatus == Customer.CustomerSyncStatus.CLEAN.code }
    }

    suspend fun deleteImmutableAccount(customerId: String) {
        return coreSdk.get().deleteImmutableAccount(customerId)
    }

    suspend fun getCleanCustomerDescriptionIfImmutable(
        customer: backend_customer_model,
    ): Pair<backend_customer_model, String?> {
        return if (customer.mobile.isNotNullOrBlank() &&
            customer.customerSyncStatus == Customer.CustomerSyncStatus.IMMUTABLE.code
        ) {
            val cleanCompanionDescription =
                getCleanCustomerByMobile(customer.mobile!!)?.description
            customer to cleanCompanionDescription
        } else {
            customer to null
        }
    }
}
