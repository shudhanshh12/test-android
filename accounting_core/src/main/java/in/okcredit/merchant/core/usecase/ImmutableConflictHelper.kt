package `in`.okcredit.merchant.core.usecase

import `in`.okcredit.merchant.core.CoreSdkImpl
import `in`.okcredit.merchant.core.model.Customer
import dagger.Lazy
import javax.inject.Inject

class ImmutableConflictHelper @Inject constructor(
    private val coreSdkImpl: Lazy<CoreSdkImpl>,
) {
    suspend fun getCleanCustomerByMobile(mobile: String, businessId: String): Customer? {
        return coreSdkImpl.get().getCustomersByMobile(mobile, businessId)
            .firstOrNull { it.customerSyncStatus == Customer.CustomerSyncStatus.CLEAN.code }
    }
}
