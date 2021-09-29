package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.DueInfoRepo
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class NullifyDueDate @Inject constructor(
    private val dueInfoRepo: Lazy<DueInfoRepo>,
) {
    fun execute(req: String): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            dueInfoRepo.get().clearDueDateForCustomer(req)
        )
    }
}
