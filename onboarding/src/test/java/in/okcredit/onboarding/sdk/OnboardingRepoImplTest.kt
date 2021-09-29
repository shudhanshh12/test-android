package `in`.okcredit.onboarding.sdk

import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test

class OnboardingRepoImplTest {

    private val mockOnboardingPreferences: OnboardingPreferencesImpl = mock()
    private val mockDeviceRepository: DeviceRepository = mock()

    private val onboarding = OnboardingRepoImpl(
        { mockOnboardingPreferences },
        { mockDeviceRepository }
    )

    @Test
    fun `should clear method of preferences when clearPreferences method is called`() {
        runBlocking {
            onboarding.clearPreferences()

            verify(mockOnboardingPreferences).clearData()
        }
    }

    @Test
    fun `should call setIsFreshLogin method of preferences when setIsFreshLogin is called`() {
        val fakeBoolean = false
        onboarding.setIsFreshLogin(fakeBoolean)

        verify(mockOnboardingPreferences).setIsFreshLogin(fakeBoolean)
    }
}
