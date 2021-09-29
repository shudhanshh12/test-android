package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class UpdateTransactionImage @Inject constructor(val transactionRepo: TransactionRepo) : UseCase<UpdateTransactionImage.Request, Unit> {

    override fun execute(req: Request): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(transactionRepo.updateTransactionImage(req.selectedImages, req.transactionId))
    }

    data class Request(
        val selectedImages: ArrayList<merchant.okcredit.accounting.model.TransactionImage>,
        val transactionId: String
    )
}
