package tech.okcredit.sdk

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.bills.BillRepository
import tech.okcredit.sdk.store.BillLocalSource
import tech.okcredit.sdk.workers.BillSyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BillRepositoryImpl @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val billLocalSource: Lazy<BillLocalSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : BillRepository {

    companion object {
        const val WORKER_BILL = "sync_bill"
    }

    override fun scheduleBillSync(businessId: String): Completable {
        return Completable.fromAction {
            val workName = WORKER_BILL

            val workRequest = OneTimeWorkRequestBuilder<BillSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    workDataOf(
                        BillSyncWorker.BUSINESS_ID to businessId
                    )
                )
                .addTag(workName)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            workManager
                .get()
                .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    override fun clearLocalData(): Completable {
        return billLocalSource.get().clearAllTables()
    }

    override fun getUnreadBillCounts(businessId: String) = billLocalSource.get().getUnreadBillCounts(businessId)

    override fun getUnreadBillCount(accountId: String, businessId: String) = billLocalSource.get().getUnreadBillCount(accountId, businessId)

    override fun getTotalBillCount(accountId: String, businessId: String) = billLocalSource.get().getTotalBillCount(accountId, businessId)

    // Feature adoption time is used to figure out unread bill count.
    // All the bills which are added after feature adoption time will be considered as unread
    // Without adoption time on login all the bills are considered as unread since there's no last seen time
    override fun setBillAdoptionTime(): Completable {
        val defaultAdoptionTime = -1L
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            billLocalSource.get().getBillAdoptionTime(defaultAdoptionTime, businessId).flatMapCompletable {
                if (it == defaultAdoptionTime) {
                    billLocalSource.get().setBillAdoptionTime(DateTimeUtils.currentDateTime().millis, businessId)
                } else {
                    Completable.complete()
                }
            }
        }
    }

    override fun resetBillAdoptionTime(businessId: String) =
        billLocalSource.get().setBillAdoptionTime(DateTimeUtils.currentDateTime().millis, businessId)
}
