package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class TxnAlertAllowAction @Inject constructor(
    private val remoteSource: BackendRemoteSource,
    private val customerRepo: Lazy<CustomerRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<TxnAlertAllowAction.Request, Unit> {

    data class Request(val accountID: String, val action: Int)

    override fun execute(req: Request): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                txnAlertAllowAction(req, businessId)
            }
        )
    }

    private fun txnAlertAllowAction(req: Request, businessId: String): Completable {
        return remoteSource.updateFeatureValueRequest(req.accountID, req.action, businessId).andThen(
            Completable.fromAction {
                val map = customerRepo.get().getCustomerTxnAlertMap(businessId)
                map?.let {
                    it[req.accountID] = false
                    customerRepo.get().updateBuyerMap(it, businessId)
                }
            }
        )
    }

    object Action {
        const val ALLOWED = 1
    }
}
