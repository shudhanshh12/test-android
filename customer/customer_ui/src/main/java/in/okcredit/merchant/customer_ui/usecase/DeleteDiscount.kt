package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class DeleteDiscount @Inject constructor(
    private val syncTransactionsImpl: SyncTransactionsImpl,
    private val remoteSource: BackendRemoteSource,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<String, Unit> {

    class Request(val txnId: String, val password: String?)

    override fun execute(txnId: String): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                deleteDiscount(txnId, businessId)
            }
        )
    }

    private fun deleteDiscount(txnId: String, businessId: String): Completable {
        return remoteSource.deleteDiscount(txnId, businessId)
            .andThen(
                syncTransactionsImpl.execute(
                    "delete_discount",
                    null,
                    false,
                    businessId
                )
            )
    }
}
