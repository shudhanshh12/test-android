package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.Command
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class UpdateTransactionNote @Inject constructor(
    val transactionRepo: TransactionRepo,
    private val updateTransactionNote: `in`.okcredit.backend._offline.usecase.UpdateTransactionNote,
    private val coreSdk: CoreSdk,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<UpdateTransactionNote.Request, Unit> {

    override fun execute(req: Request): Observable<Result<Unit>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            coreSdk.isCoreSdkFeatureEnabled(businessId)
                .flatMapObservable {
                    if (it) {
                        coreUpdateTransactionNote(req, businessId)
                    } else {
                        frontendUpdateTransactionNote(req)
                    }
                }
        }
    }

    private fun coreUpdateTransactionNote(req: Request, businessId: String): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            coreSdk.processTransactionCommand(Command.UpdateTransactionNote(req.transactionId, req.note), businessId)
                .ignoreElement()
        )
    }

    private fun frontendUpdateTransactionNote(req: Request): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { _businessId ->
                transactionRepo.updateTransactionNote(req.note, req.transactionId).andThen(
                    transactionRepo.getTransaction(req.transactionId, _businessId).firstOrError().flatMapCompletable {
                        if (it.isDirty) {
                            Completable.complete()
                        } else {
                            updateTransactionNote.schedule(req.note, req.transactionId, _businessId)
                        }
                    }
                )
            }
        )
    }

    data class Request(val note: String, val transactionId: String)
}
