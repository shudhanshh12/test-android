package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.BusinessRepositoryImpl
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import javax.inject.Inject

class GetActiveBusinessImpl @Inject constructor(
    private val businessApi: Lazy<BusinessRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetActiveBusiness {
    override fun execute() =
        getActiveBusinessId.get().execute()
            .flatMapObservable { businessApi.get().getBusiness(it) }
}
