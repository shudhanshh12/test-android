package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend.contract.GetTotalTxnCount
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

// This is duplicate of [GetTotalTxnCount]
// TODO Remove this usecase
class GetAllTransactionCount @Inject constructor(
    private val getTotalTxnCount: Lazy<GetTotalTxnCount>
) :
    UseCase<Unit, Int> {

    override fun execute(req: Unit): Observable<Result<Int>> =
        UseCase.wrapSingle(getTotalTxnCount.get().execute())
}
