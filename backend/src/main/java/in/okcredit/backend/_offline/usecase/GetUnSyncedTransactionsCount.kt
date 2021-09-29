package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetUnSyncedTransactionsCount @Inject
constructor(
    private val transactionRepo: TransactionRepo,
    private val supplierCreditRepository: SupplierCreditRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(): Observable<Int> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            Observable.combineLatest(
                transactionRepo.listDirtyTransactions(null, businessId),
                supplierCreditRepository.listDirtyTransactions(businessId)
            ) { customerUnsyncedTransaction, supplierUnsyncedTransaction ->

                return@combineLatest customerUnsyncedTransaction.size + supplierUnsyncedTransaction.size
            }
        }
    }
}
