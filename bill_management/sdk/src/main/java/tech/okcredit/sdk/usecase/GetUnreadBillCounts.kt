package tech.okcredit.sdk.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import tech.okcredit.bills.BillRepository
import javax.inject.Inject

class GetUnreadBillCounts @Inject constructor(
    private val billRepository: Lazy<BillRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute() = getActiveBusinessId.get().execute().flatMapObservable { businessId ->
        billRepository.get().getUnreadBillCounts(businessId)
    }
}
