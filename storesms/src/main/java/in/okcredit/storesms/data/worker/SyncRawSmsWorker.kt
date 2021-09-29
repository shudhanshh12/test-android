package `in`.okcredit.storesms.data.worker

import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.storesms.StoreSmsRepository
import `in`.okcredit.storesms.StoreSmsRepositoryImpl
import `in`.okcredit.storesms.data.server.RawSms
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import androidx.work.*
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.awaitFirst
import kotlinx.coroutines.withContext
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncRawSmsWorker constructor(
    private val context: Context,
    workerParameters: WorkerParameters,
    private val storeSmsRepository: Lazy<StoreSmsRepository>,
    private val ab: Lazy<AbRepository>,
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
) : CoroutineWorker(context, workerParameters) {

    companion object {
        const val SYNC_SMS_FEATURE_FLAG = "sms_sync_service"

        fun scheduleSyncRawSMStoServer(context: Context, syncIntervalInHours: Long) {
            val workRequest = PeriodicWorkRequestBuilder<SyncRawSmsWorker>(
                syncIntervalInHours, TimeUnit.HOURS,
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(StoreSmsRepositoryImpl.WORKER_SYNC_RAW_SMS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    StoreSmsRepositoryImpl.WORKER_RETRY_INTERVAL_SECONDS,
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    StoreSmsRepositoryImpl.WORKER_SYNC_RAW_SMS,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
        }
    }

    @Throws(Exception::class)
    private suspend fun syncRawSms(businessId: String): Result {
        val lastSyncTime = storeSmsRepository.get().getLastRawSmsSyncedTimerFromServer(businessId)
        return if (lastSyncTime > -1)
            getAndSyncRawSms(lastSyncTime, businessId)
        else
            Result.failure()
    }

    private suspend fun syncRawSmsBusinessWise(): Result {
        getBusinessIdList.get().execute().first().forEach { businessId ->
            val isSyncSmsFeatureFlagEnabled = isSyncSmsFeatureFlagEnabled(businessId)

            if (isSyncSmsFeatureFlagEnabled) {
                val result = syncRawSms(businessId)
                if (result !is Result.Success) {
                    return result
                }
            }
        }
        return Result.success()
    }

    private suspend fun getAndSyncRawSms(lastSyncTime: Long, businessId: String): Result {
        val selectionArgs = arrayOf(lastSyncTime.toString())
        val listSms: ArrayList<RawSms> = arrayListOf()
        val message = Uri.parse("content://sms/inbox")
        val cr: ContentResolver = context.contentResolver
        val c: Cursor? = cr.query(message, null, Telephony.Sms.DATE + ">?", selectionArgs, "date ASC")
        c?.let {
            val totalSMS = it.count
            if (it.moveToFirst()) {
                for (i in 0 until totalSMS) {
                    val objSms = RawSms(
                        senderAddress = c.getString(c.getColumnIndexOrThrow("address")),
                        rawMessage = c.getString(c.getColumnIndexOrThrow("body")),
                        messageReceivedTime = c.getString(c.getColumnIndexOrThrow("date")).toLong(),
                        creationTime = System.currentTimeMillis()
                    )
                    listSms.add(objSms)
                    c.moveToNext()
                }
            }
        }
        c?.close()

        return storeSmsRepository.get().sendRawSmsToServer(
            if (listSms.size > 2000) listSms.subList(0, 2000) else listSms,
            businessId
        )
    }

    private suspend fun isSyncSmsFeatureFlagEnabled(businessId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                ab.get().isFeatureEnabled(SYNC_SMS_FEATURE_FLAG, businessId = businessId).awaitFirst()
            } catch (t: Exception) {
                false
            }
        }
    }

    override suspend fun doWork(): Result {
        try {
            if (runAttemptCount >= 3) {
                return Result.failure()
            }
            return syncRawSmsBusinessWise()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    class Factory @Inject constructor(
        private val storeSmsRepository: Lazy<StoreSmsRepository>,
        private val ab: Lazy<AbRepository>,
        private val getBusinessIdList: Lazy<GetBusinessIdList>,
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return SyncRawSmsWorker(context, params, storeSmsRepository, ab, getBusinessIdList)
        }
    }
}
