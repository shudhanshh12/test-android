package tech.okcredit.home.ui.payables_onboarding.helpers

import `in`.okcredit.onboarding.contract.OnboardingRepo
import com.google.common.truth.Truth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Test
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.home.ui.homesearch.HomeConstants.HomeTab.CUSTOMER_TAB
import tech.okcredit.home.ui.homesearch.HomeConstants.HomeTab.SUPPLIER_TAB
import tech.okcredit.home.ui.payables_onboarding.HomeTabOrderList
import tech.okcredit.home.ui.payables_onboarding.helpers.TabOrderingHelper.HomeTabState

class TabOrderingHelperTest {
    private val mockOnboardingRepo: OnboardingRepo = mock()
    private val mockLocaleManager: LocaleManager = mock()
    private val mockRemoteConfig: FirebaseRemoteConfig = mock()

    private val tabOrderingHelper = TabOrderingHelper(
        { mockOnboardingRepo },
        { mockRemoteConfig },
        { mockLocaleManager }
    )

    @Test
    fun `when onboardingRepo marks users as supplier then returns supplier tab first`() = runBlocking {
        whenever(mockOnboardingRepo.getSuspectUserIsSupplier()).thenReturn(true)
        whenever(mockOnboardingRepo.getPayablesOnboardingVariant()).thenReturn("TestGroup")
        whenever(mockLocaleManager.getLanguage()).thenReturn(LocaleManager.LANGUAGE_ENGLISH)

        val result = tabOrderingHelper.getHomeTabState()

        Truth.assertThat(result)
            .isEqualTo(HomeTabState(HomeTabOrderList(listOf(SUPPLIER_TAB, CUSTOMER_TAB)), true))
    }

    @Test
    fun `when onboardingRepo marks users as not a supplier then returns customer tab first`() = runBlocking {
        whenever(mockOnboardingRepo.getSuspectUserIsSupplier()).thenReturn(false)

        val result = tabOrderingHelper.getHomeTabState()

        Truth.assertThat(result)
            .isEqualTo(HomeTabState(HomeTabOrderList(listOf(CUSTOMER_TAB, SUPPLIER_TAB)), null))
    }

    @Test
    fun `when asked to save user is supplier then calls onboardingRepo`() = runBlocking {
        tabOrderingHelper.setSuspectUserIsSupplier(mapOf("campaign" to "xyz_B2B"))

        verify(mockOnboardingRepo, atLeastOnce()).setSuspectUserIsSupplier(true)
    }

    @Test
    fun `when asked to save user is not supplier then calls onboardingRepo`() = runBlocking {
        tabOrderingHelper.setSuspectUserIsSupplier(mapOf("campaign" to "fake_value"))

        verify(mockOnboardingRepo, atLeastOnce()).setSuspectUserIsSupplier(false)
    }
}
