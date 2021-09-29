package `in`.okcredit.backend.contract

import io.reactivex.Completable

interface NonActiveBusinessesDataSyncWorker {
    fun schedule(): Completable
}
