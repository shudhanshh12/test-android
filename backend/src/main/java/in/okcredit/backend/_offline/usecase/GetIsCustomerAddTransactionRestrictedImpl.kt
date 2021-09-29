package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.GetIsCustomerAddTransactionRestricted
import dagger.Lazy
import javax.inject.Inject

class GetIsCustomerAddTransactionRestrictedImpl @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
) : GetIsCustomerAddTransactionRestricted {

    override suspend fun execute(businessId: String, customerId: String): Boolean {
        return customerRepo.get().getIsBlocked(businessId, customerId)
//        Note: Only supplier is prevented from adding transactions using `addTransactionRestricted` flag
//            || customerRepo.get().getIsAddTransactionRestricted(businessId, customerId)
    }
}
