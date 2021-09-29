package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import javax.inject.Inject

class GetCustomerImpl @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetCustomer {

    override fun execute(customerId: String?) = getActiveBusinessId.get().execute().flatMapObservable { businessId ->
        customerRepo.get().getCustomer(customerId, businessId)
    }

    override fun executeObservable(customerId: String?) =
        UseCase.wrapObservable(execute(customerId))
}
