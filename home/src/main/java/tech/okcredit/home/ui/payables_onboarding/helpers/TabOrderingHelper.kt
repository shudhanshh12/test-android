package tech.okcredit.home.ui.payables_onboarding.helpers

import `in`.okcredit.onboarding.contract.OnboardingRepo
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import dagger.Reusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.home.ui.homesearch.HomeConstants.HomeTab.CUSTOMER_TAB
import tech.okcredit.home.ui.homesearch.HomeConstants.HomeTab.SUPPLIER_TAB
import tech.okcredit.home.ui.payables_onboarding.HomeTabOrderList
import tech.okcredit.home.ui.payables_onboarding.PayablesOnboardingVariant
import tech.okcredit.home.ui.payables_onboarding.PayablesOnboardingVariant.CONTROL_GROUP
import tech.okcredit.home.ui.payables_onboarding.PayablesOnboardingVariant.Companion.fromString
import tech.okcredit.home.ui.payables_onboarding.PayablesOnboardingVariant.TEST_GROUP
import javax.inject.Inject

@Reusable
class TabOrderingHelper @Inject constructor(
    private val onboardingRepo: Lazy<OnboardingRepo>,
    private val remoteConfig: Lazy<FirebaseRemoteConfig>,
    private val localeManager: Lazy<LocaleManager>,
) {
    data class HomeTabState(
        val tabOrderList: HomeTabOrderList,
        val isPayablesExperimentEnabled: Boolean?,
    )

    suspend fun getHomeTabState(): HomeTabState = withContext(Dispatchers.IO) {
        val isExperimentEnabled = isExperimentEnabled()
        val homeTabOrderList = HomeTabOrderList(
            if (isExperimentEnabled == true) {
                listOf(SUPPLIER_TAB, CUSTOMER_TAB)
            } else {
                listOf(CUSTOMER_TAB, SUPPLIER_TAB)
            }
        )
        HomeTabState(
            homeTabOrderList,
            isExperimentEnabled,
        )
    }

    suspend fun isExperimentEnabled(): Boolean? = withContext(Dispatchers.IO) {
        val suspectUserIsSupplier = onboardingRepo.get().getSuspectUserIsSupplier()
        val isPayablesOnboarding = isPayablesOnboardingUxEnabled(suspectUserIsSupplier)
        val currentLanguage = localeManager.get().getLanguage()
        when {
            !suspectUserIsSupplier -> null
            isPayablesOnboarding && currentLanguage in passListLanguages -> true
            else -> false
        }
    }

    suspend fun setSuspectUserIsSupplier(value: Map<String, String>?) {
        val suspectUserIsSupplier = value?.get("campaign")
            ?.endsWith("_B2B")
            ?: return
        onboardingRepo.get().setSuspectUserIsSupplier(suspectUserIsSupplier)
    }

    // call only if user is suspected to be supplier
    private suspend fun getPayablesOnboardingVariant(): PayablesOnboardingVariant = withContext(Dispatchers.IO) {
        onboardingRepo.get().getPayablesOnboardingVariant()
            .takeIf { it.isNotBlank() }
            ?.let { fromString(it) }
            ?: listOf(
                TEST_GROUP,
                CONTROL_GROUP
            ).random().also {
                onboardingRepo.get().setPayablesOnboardingVariant(it.value)
            }
    }

    private suspend fun isPayablesOnboardingUxEnabled(isUserSupplier: Boolean): Boolean {
        return isUserSupplier &&
            (
                getPayablesOnboardingVariant() == TEST_GROUP ||
                    remoteConfig.get().getBoolean(PAYABLES_ONBOARDING_FULL_ROLL_OUT)
                )
    }

    companion object {
        private const val PAYABLES_ONBOARDING_FULL_ROLL_OUT = "payables_onboarding_full_roll_out"
        private val passListLanguages =
            setOf(LocaleManager.LANGUAGE_HINDI, LocaleManager.LANGUAGE_ENGLISH, LocaleManager.LANGUAGE_HINGLISH)
    }
}
