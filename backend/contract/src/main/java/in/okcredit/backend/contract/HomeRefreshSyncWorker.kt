package `in`.okcredit.backend.contract

import io.reactivex.Completable

interface HomeRefreshSyncWorker {
    fun schedule(source: String? = null): Completable
}
