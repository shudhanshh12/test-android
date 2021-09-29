package `in`.okcredit.backend.contract

import io.reactivex.Completable

interface SyncTransaction {
    fun executeForceSync(businessId: String? = null): Completable
}
