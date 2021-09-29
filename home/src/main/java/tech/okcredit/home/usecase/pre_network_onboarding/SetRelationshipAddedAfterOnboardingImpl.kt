package tech.okcredit.home.usecase.pre_network_onboarding

import `in`.okcredit.home.SetRelationshipAddedAfterOnboarding
import `in`.okcredit.onboarding.contract.OnboardingRepo
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetRelationshipAddedAfterOnboardingImpl @Inject constructor(
    private val onboardingRepo: Lazy<OnboardingRepo>,
) : SetRelationshipAddedAfterOnboarding {

    override suspend fun execute() = withContext(Dispatchers.IO) {
        onboardingRepo.get().setRelationshipAddedAfterOnboarding(true)
    }
}
