package tech.okcredit.sdk.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.sdk.store.BillLocalSource
import javax.inject.Inject

class GetAccountsUnseenBills @Inject constructor(
    private val billLocalSource: Lazy<BillLocalSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<GetAccountsUnseenBills.Request, Int> {
    override fun execute(req: Request): Observable<Result<Int>> {

        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                billLocalSource.get().getUnseenBillCount(req.accountId, req.startTime, businessId)
            }
        )
    }

    data class Request(val accountId: String, val startTime: Long)
}
