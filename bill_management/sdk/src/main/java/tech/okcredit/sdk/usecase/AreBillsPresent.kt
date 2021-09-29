package tech.okcredit.sdk.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import tech.okcredit.sdk.store.BillLocalSource
import javax.inject.Inject

class AreBillsPresent @Inject constructor(
    private val billLocalSource: BillLocalSource
) : UseCase<AreBillsPresent.Request, Boolean> {
    override fun execute(req: Request): Observable<Result<Boolean>> {

        return UseCase.wrapObservable(billLocalSource.areBillsPresent(req.accountId))
    }

    data class Request(val accountId: String)
}
