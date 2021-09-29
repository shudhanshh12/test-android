package `in`.okcredit.dynamicview.data.repository

import `in`.okcredit.dynamicview.data.CustomizationSyncWorker
import `in`.okcredit.dynamicview.data.server.CustomizationServer
import `in`.okcredit.dynamicview.data.store.CustomizationStore
import dagger.Lazy
import dagger.Reusable
import kotlinx.coroutines.rx2.rxCompletable
import merchant.okcredit.dynamicview.contract.DynamicViewRepository
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.workmanager.OkcWorkManager
import javax.inject.Inject

@Reusable
class DynamicViewRepositoryImpl @Inject constructor(
    private val server: Lazy<CustomizationServer>,
    private val store: Lazy<CustomizationStore>,
    private val workManager: Lazy<OkcWorkManager>,
) : DynamicViewRepository {

    suspend fun syncCustomizations(businessId: String) {
        val response = server.get().getCustomizations(businessId)
        try {
            store.get().saveCustomizations(response, businessId)
        } catch (t: Exception) {
            RecordException.recordException(t)
        }
    }

    override fun scheduleSyncCustomizations(businessId: String) =
        rxCompletable { CustomizationSyncWorker.schedule(workManager.get(), businessId) }

    fun getCustomizations(businessId: String) = store.get().getCustomizations(businessId)

    fun getFallbackCustomizations() = store.get().getFallbackCustomizations()

    override fun clearLocalData() = rxCompletable { store.get().clearAllCustomizations() }
}
