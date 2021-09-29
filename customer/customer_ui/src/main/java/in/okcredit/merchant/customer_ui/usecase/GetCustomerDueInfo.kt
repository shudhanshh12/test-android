package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.DueInfoRepo
import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetCustomerDueInfo @Inject constructor(
    private val customerDueInfoRepo: Lazy<DueInfoRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(req: Request): Observable<DueInfo> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            customerDueInfoRepo.get().getDueInfoForCustomer(req.customerId, businessId)
        }
    }

    data class Request(val customerId: String)
}
