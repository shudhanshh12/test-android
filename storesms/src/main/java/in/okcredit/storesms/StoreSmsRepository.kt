package `in`.okcredit.storesms

import `in`.okcredit.storesms.data.server.RawSms
import androidx.work.ListenableWorker

interface StoreSmsRepository {

    suspend fun sendRawSmsToServer(list: List<RawSms>, businessId: String): ListenableWorker.Result

    suspend fun getLastRawSmsSyncedTimerFromServer(businessId: String): Long
}
