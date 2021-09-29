package `in`.okcredit.merchant.core.sync

import `in`.okcredit.accounting_core.contract.SyncState
import io.reactivex.Completable
import io.reactivex.subjects.BehaviorSubject

interface CoreTransactionSyncer {
    fun schedule(source: String, businessId: String): Completable
    fun execute(
        source: String,
        req: BehaviorSubject<SyncState>? = null,
        isFromSyncScreen: Boolean = false,
        isFromForceSync: Boolean = false,
        businessId: String? = null,
    ): Completable
}
