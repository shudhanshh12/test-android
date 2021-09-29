package `in`.okcredit.frontend.contract

import `in`.okcredit.accounting_core.contract.SyncState
import io.reactivex.Completable
import io.reactivex.subjects.BehaviorSubject

interface LoginDataSyncer {
    fun syncDataForBusinessId(
        req: BehaviorSubject<SyncState>,
        timeStartInSec: Long? = null,
        businessId: String,
    ): Completable
}
