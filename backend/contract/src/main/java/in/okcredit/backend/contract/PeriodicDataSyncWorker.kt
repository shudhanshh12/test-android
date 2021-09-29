package `in`.okcredit.backend.contract

import io.reactivex.Completable

interface PeriodicDataSyncWorker {
    fun schedule(businessId: String): Completable
}
