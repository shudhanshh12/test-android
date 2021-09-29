package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class IsTransactionPresent @Inject constructor(
    private val transactionRepo: TransactionRepo,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<String, Boolean> {

    companion object {
        const val TAG = "IsTransactionPresent"
    }

    override fun execute(req: String): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(
            Observable.interval(300, TimeUnit.MILLISECONDS)
                .flatMapSingle { getActiveBusinessId.get().execute() }
                .flatMap { businessId -> transactionRepo.isTransactionPresent(req, businessId).toObservable() }
        )
    }
}
