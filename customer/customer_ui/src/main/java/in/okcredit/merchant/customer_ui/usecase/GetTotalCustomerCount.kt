package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetTotalCustomerCount @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Single<Long> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            customerRepo.get().getActiveCustomerCount(businessId).firstOrError()
        }
    }
}
