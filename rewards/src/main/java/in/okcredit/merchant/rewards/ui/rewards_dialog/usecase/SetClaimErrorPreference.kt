package `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase

import `in`.okcredit.rewards.contract.RewardsRepository
import dagger.Lazy
import javax.inject.Inject

class SetClaimErrorPreference @Inject constructor(
    private val rewardsRepository: Lazy<RewardsRepository>
) {
    fun execute(status: Boolean) {
        rewardsRepository.get().setClaimErrorPreference(status)
    }
}
