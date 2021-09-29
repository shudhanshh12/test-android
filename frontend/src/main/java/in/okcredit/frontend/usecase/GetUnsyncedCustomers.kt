package `in`.okcredit.frontend.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetUnsyncedCustomers @Inject constructor(
    private val transactionRepo: TransactionRepo,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<Unit, List<String>> {

    override fun execute(req: Unit): Observable<Result<List<String>>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            transactionRepo.listDirtyTransactions(null, businessId)
                .map<Result<List<String>>> {
                    val unsyncedCustomers = mutableListOf<String>()
                    for (unsyncedTxn in it) {
                        if (!unsyncedCustomers.contains(unsyncedTxn.customerId)) {
                            unsyncedCustomers.add(unsyncedTxn.customerId)
                        }
                    }
                    Result.Success(unsyncedCustomers as List<String>)
                }
                .distinctUntilChanged()
        }
    }
}
