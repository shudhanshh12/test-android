package `in`.okcredit.merchant.customer_ui.ui.payment

import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class CheckForNewCustomerCollections @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
) {

    suspend fun execute() {
        val businessId = getActiveBusinessId.get().execute().await()
        triggerSync(businessId)
    }

    private suspend fun triggerSync(businessId: String) {
        repeat(Int.MAX_VALUE) {
            delay(2_000)
            collectionSyncer.get().executeSyncCustomerCollections(businessId)
        }
    }
}
