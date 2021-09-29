package `in`.okcredit.frontend.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend.contract.HomeRefreshSyncWorker
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class PostSupplierMigration @Inject constructor(
    private val customerRepo: CustomerRepo,
    private val transactionRepo: TransactionRepo,
    private val supplierCreditRepository: SupplierCreditRepository,
    private val homeRefreshSyncWorker: HomeRefreshSyncWorker,
    private val coreSdk: CoreSdk,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<PostSupplierMigration.Request, Unit> {

    override fun execute(req: Request): Observable<Result<Unit>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            coreSdk.isCoreSdkFeatureEnabled(businessId)
                .flatMapObservable {
                    if (it) {
                        coreExecute(req, businessId)
                    } else {
                        backendExecute(req, businessId)
                    }
                }
        }
    }

    private fun backendExecute(req: Request, businessId: String): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            customerRepo.deleteCustomer(req.accountId)
                .doOnComplete {
                    req.porgressSubject.onNext(30)
                }
                .andThen(
                    transactionRepo.deleteAllTransactionForCustomer(req.accountId, businessId)
                ).doOnComplete {
                    req.porgressSubject.onNext(60)
                }
                .andThen(
                    supplierCreditRepository.executeSyncSupplierAndTransactions(req.migratedAccountId, businessId)
                ).doOnComplete {
                    req.porgressSubject.onNext(90)
                }.andThen(
                    homeRefreshSyncWorker.schedule("migration_supplier")
                )
        )
    }

    private fun coreExecute(req: Request, businessId: String): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            coreSdk.deleteLocalCustomer(req.accountId)
                .doOnComplete {
                    req.porgressSubject.onNext(30)
                }
                .andThen(
                    coreSdk.deleteLocalTransactionsForCustomer(req.accountId)
                ).doOnComplete {
                    req.porgressSubject.onNext(60)
                }
                .andThen(
                    supplierCreditRepository.executeSyncSupplierAndTransactions(req.migratedAccountId, businessId)
                ).doOnComplete {
                    req.porgressSubject.onNext(90)
                }.andThen(
                    homeRefreshSyncWorker.schedule("migration_supplier")
                )
        )
    }

    data class Request(
        val accountId: String,
        val migratedAccountId: String,
        val porgressSubject: PublishSubject<Int>
    )
}
