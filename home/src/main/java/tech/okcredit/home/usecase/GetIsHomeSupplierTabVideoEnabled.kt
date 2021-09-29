package tech.okcredit.home.usecase

import `in`.okcredit.backend.contract.Features
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class GetCanShowSupplierTabVideo @Inject constructor(private val ab: AbRepository) : UseCase<Unit, Boolean> {

    override fun execute(req: Unit): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(ab.isFeatureEnabled(Features.HOME_SUPPLIER_TAB_VIDEO))
    }
}
