package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.Command
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class UpdateTransactionImageLocally @Inject constructor(
    val transactionRepo: TransactionRepo,
    private val coreSdk: CoreSdk,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<UpdateTransactionImageLocally.Request, Unit> {

    override fun execute(req: Request): Observable<Result<Unit>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            coreSdk.isCoreSdkFeatureEnabled(businessId)
                .flatMapObservable {
                    if (it) {
                        coreUpdateTransactionImageLocally(req, businessId)
                    } else {
                        frontendUpdateTransactionImageLocally(req)
                    }
                }
        }
    }

    private fun coreUpdateTransactionImageLocally(req: Request, businessId: String): Observable<Result<Unit>> {
        val imageUriList = req.updatedImagesList.map { it.url }
        return UseCase.wrapCompletable(
            coreSdk.processTransactionCommand(
                Command.UpdateTransactionImages(imageUriList, req.transactionId),
                businessId
            ).ignoreElement()
        )
    }

    private fun frontendUpdateTransactionImageLocally(req: Request): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(transactionRepo.updateTransactionImage(req.updatedImagesList, req.transactionId))
    }

    data class Request(
        val updatedImagesList: ArrayList<merchant.okcredit.accounting.model.TransactionImage>,
        val transactionId: String,
    )
}
