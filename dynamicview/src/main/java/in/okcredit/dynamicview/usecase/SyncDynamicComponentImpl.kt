package `in`.okcredit.dynamicview.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import merchant.okcredit.dynamicview.contract.DynamicViewRepository
import merchant.okcredit.dynamicview.contract.SyncDynamicComponent
import javax.inject.Inject

@Reusable
class SyncDynamicComponentImpl @Inject constructor(
    private val repository: Lazy<DynamicViewRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : SyncDynamicComponent {

    override fun execute(): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            repository.get().scheduleSyncCustomizations(businessId)
        }
    }
}
