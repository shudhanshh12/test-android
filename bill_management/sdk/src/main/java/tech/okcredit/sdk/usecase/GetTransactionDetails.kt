package tech.okcredit.sdk.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.sdk.store.BillLocalSource
import tech.okcredit.sdk.store.database.LocalBill
import javax.inject.Inject

class GetTransactionDetails @Inject constructor(
    private val billLocalSource: Lazy<BillLocalSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<GetTransactionDetails.Request, LocalBill> {
    override fun execute(req: Request): Observable<Result<LocalBill>> {

        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                billLocalSource.get().getBill(req.billId, businessId)
            }
        )
    }

    data class Request(val billId: String)
}
