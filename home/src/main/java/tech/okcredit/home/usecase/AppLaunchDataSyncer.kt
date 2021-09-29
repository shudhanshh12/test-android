package tech.okcredit.home.usecase

import `in`.okcredit.backend.contract.CheckAuth
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.collection.contract.CollectionSyncer.Source.APP_LAUNCH
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.awaitFirst
import javax.inject.Inject

class AppLaunchDataSyncer @Inject constructor(
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val checkAuth: Lazy<CheckAuth>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    suspend fun execute() {
        val isUserLoggedIn = checkAuth.get().execute().awaitFirst()
        if (isUserLoggedIn) {
            val businessId = getActiveBusinessId.get().execute().await()
            collectionSyncer.get().scheduleSyncEverything(APP_LAUNCH, businessId)
        }
    }
}
