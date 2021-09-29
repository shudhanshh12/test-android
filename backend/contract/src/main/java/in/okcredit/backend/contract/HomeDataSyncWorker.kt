package `in`.okcredit.backend.contract

import io.reactivex.Completable

interface HomeDataSyncWorker {
    fun schedule(): Completable
}
