package tech.okcredit.sdk.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import tech.okcredit.sdk.store.BillLocalSource
import javax.inject.Inject

class GetBillForId @Inject constructor(
    private val billLocalSource: Lazy<BillLocalSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(billId: String) = getActiveBusinessId.get().execute().flatMapObservable { businessId ->
        billLocalSource.get().getBill(billId, businessId)
    }
}
