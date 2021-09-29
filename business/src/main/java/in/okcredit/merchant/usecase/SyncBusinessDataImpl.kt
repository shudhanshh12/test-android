package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.SyncBusinessData
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class SyncBusinessDataImpl @Inject constructor(
    private val businessRepository: Lazy<BusinessRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : SyncBusinessData {
    override fun execute(): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            businessRepository.get().executeSyncBusiness(businessId)
                .andThen(businessRepository.get().scheduleSyncBusinessCategoriesAndBusinessTypes(businessId))
        }
    }
}
