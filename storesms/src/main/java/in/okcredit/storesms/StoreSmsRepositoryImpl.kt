package `in`.okcredit.storesms

import `in`.okcredit.storesms.data.server.RawSms
import `in`.okcredit.storesms.data.server.StoreSmsServer
import androidx.work.ListenableWorker
import dagger.Lazy
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.coroutines.DispatcherProvider
import javax.inject.Inject

class StoreSmsRepositoryImpl @Inject constructor(
    private val storeSmsServer: Lazy<StoreSmsServer>,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
) : StoreSmsRepository {

    companion object {
        const val WORKER_SYNC_RAW_SMS = "syn_raw_sms_to_server"
        const val WORKER_RETRY_INTERVAL_SECONDS = 30L
    }

    override suspend fun sendRawSmsToServer(list: List<RawSms>, businessId: String): ListenableWorker.Result {
        return withContext(dispatcherProvider.get().io()) {
            try {
                storeSmsServer.get().sendRawSmsToServer(list, businessId)
                ListenableWorker.Result.success()
            } catch (t: Exception) {
                ListenableWorker.Result.retry()
            }
        }
    }

    override suspend fun getLastRawSmsSyncedTimerFromServer(businessId: String): Long {
        return withContext(dispatcherProvider.get().io()) {
            try {
                val lastSyncTime = storeSmsServer.get().getLastRawSmsSyncedTime(businessId).time
                lastSyncTime
            } catch (t: Exception) {
                -1L
            }
        }
    }
}
