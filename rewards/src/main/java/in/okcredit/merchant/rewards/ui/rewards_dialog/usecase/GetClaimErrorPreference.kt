package `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase

import `in`.okcredit.rewards.contract.RewardsRepository
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetClaimErrorPreference @Inject constructor(
    private val rewardsRepository: Lazy<RewardsRepository>
) {
    fun execute(): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(
            rewardsRepository.get().getClaimErrorPreference()
        )
    }
}
