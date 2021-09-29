package `in`.okcredit.onboarding.pre_network_onboarding

import `in`.okcredit.onboarding.contract.OnboardingRepo
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetFreshLogin @Inject constructor(
    private val onboardingRepo: Lazy<OnboardingRepo>,
) {
    suspend fun execute(value: Boolean) = withContext(Dispatchers.IO) {
        onboardingRepo.get().setIsFreshLogin(value)
    }
}
