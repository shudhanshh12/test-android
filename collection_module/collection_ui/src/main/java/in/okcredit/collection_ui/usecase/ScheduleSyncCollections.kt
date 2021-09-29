package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class ScheduleSyncCollections @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
) {
    fun execute(source: String): Completable {
        return getActiveBusinessId.get().execute().doOnSuccess { businessId ->
            collectionSyncer.get().scheduleSyncEverything(source, businessId)
        }.ignoreElement()
    }
}
