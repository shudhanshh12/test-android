package tech.okcredit.use_case

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.bills.BillRepository
import javax.inject.Inject

class ScheduleBillSync @Inject constructor(
    private val billRepository: Lazy<BillRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            billRepository.get().scheduleBillSync(businessId)
        }
    }
}
