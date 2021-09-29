package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class ScheduleSyncTransactions @Inject constructor(
    private val syncTransactionImpl: Lazy<SyncTransactionsImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(source: String): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            syncTransactionImpl.get().schedule(source, businessId)
        }
    }
}
