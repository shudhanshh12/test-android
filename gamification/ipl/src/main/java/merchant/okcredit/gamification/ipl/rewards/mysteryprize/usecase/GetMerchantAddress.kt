package merchant.okcredit.gamification.ipl.rewards.mysteryprize.usecase

import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetMerchantAddress @Inject constructor(
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
) {
    fun execute(): Observable<Result<String?>> {
        return UseCase.wrapSingle(
            getActiveBusiness.get().execute()
                .firstOrError()
                .map { it.address }
        )
    }
}
