package tech.okcredit.home.usecase.pre_network_onboarding

import `in`.okcredit.onboarding.contract.OnboardingRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetPreNetworkOnboardingNudgeShown @Inject constructor(
    private val onboardingRepo: OnboardingRepo,
) {
    suspend fun execute() = withContext(Dispatchers.IO) {
        onboardingRepo.setVisibilityPreNetworkOnboardingNudge(true)
    }
}
