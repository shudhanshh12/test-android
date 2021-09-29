package `in`.okcredit.onboarding.marketing

import `in`.okcredit.merchant.device.Device
import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AppsFlyerHelperImplTest {

    private val mockMarketingRepository: MarketingRepository = mock()
    private val mockOnboardingPreferencesImpl: OnboardingPreferencesImpl = mock()
    private val mockDeviceRepository: DeviceRepository = mock()

    @Test
    fun `when setAuthData then saves in prefs without calling reporting`() = runBlocking {
        val appsFlyerHelperImpl = AppsFlyerHelperImpl(
            { mockMarketingRepository },
            { mockOnboardingPreferencesImpl },
            { mockDeviceRepository },
        )

        appsFlyerHelperImpl.setAuthSuccess(fakeIsAuthSignup)

        verify(mockOnboardingPreferencesImpl, atLeastOnce()).setMarketingIsSignUp(fakeIsAuthSignup)
        verify(mockOnboardingPreferencesImpl, atLeastOnce()).setMarketingAuthTime(any())
        verify(mockMarketingRepository, never()).reportMarketingData(any(), any(), any(), any(), any())
    }

    @Test
    fun `when setPreProcessedAppsflyerData then sets internal state without calling reporting`() = runBlocking {
        val appsFlyerHelperImpl = AppsFlyerHelperImpl(
            { mockMarketingRepository },
            { mockOnboardingPreferencesImpl },
            { mockDeviceRepository },
        )

        val mockDevice: Device = mock()

        whenever(mockDevice.aaid).thenReturn(fakeAaid)
        whenever(mockDeviceRepository.getDevice()).thenReturn(Observable.just(mockDevice))
        whenever(mockOnboardingPreferencesImpl.containsMarketingAuthTime()).thenReturn(false)
        whenever(mockOnboardingPreferencesImpl.containsMarketingIsSignUp()).thenReturn(false)

        appsFlyerHelperImpl.setPreProcessedAppsflyerData(
            mapOf(
                "media_source" to fakeMediaSource,
                "c" to fakeCampaign
            )
        )
        Truth.assertThat(appsFlyerHelperImpl.aaid).isEqualTo(fakeAaid)
        Truth.assertThat(appsFlyerHelperImpl.mediaSource).isEqualTo(fakeMediaSource)
        Truth.assertThat(appsFlyerHelperImpl.campaign).isEqualTo(fakeCampaign)
        verify(mockMarketingRepository, never()).reportMarketingData(any(), any(), any(), any(), any())
    }

    @Test
    fun `when setPreProcessedAppsflyerData is called with auth data set then calls report endpoint`() = runBlocking {
        val appsFlyerHelperImpl = AppsFlyerHelperImpl(
            { mockMarketingRepository },
            { mockOnboardingPreferencesImpl },
            { mockDeviceRepository },
        )

        val mockDevice: Device = mock()
        val fakeAuthTime = 123456789L

        whenever(mockDevice.aaid).thenReturn(fakeAaid)
        whenever(mockDeviceRepository.getDevice()).thenReturn(Observable.just(mockDevice))
        whenever(mockOnboardingPreferencesImpl.containsMarketingAuthTime()).thenReturn(true)
        whenever(mockOnboardingPreferencesImpl.containsMarketingIsSignUp()).thenReturn(true)
        whenever(mockOnboardingPreferencesImpl.getMarketingAuthTime()).thenReturn(fakeAuthTime)
        whenever(mockOnboardingPreferencesImpl.getMarketingIsSignUp()).thenReturn(fakeIsAuthSignup)

        appsFlyerHelperImpl.setPreProcessedAppsflyerData(mapOf("media_source" to fakeMediaSource, "c" to fakeCampaign))

        verify(mockOnboardingPreferencesImpl, times(1)).removeMarketingIsSignUp()
        verify(mockOnboardingPreferencesImpl, times(1)).removeMarketingAuthTime()
        verify(mockMarketingRepository, times(1))
            .reportMarketingData(fakeAaid, fakeIsAuthSignup, fakeAuthTime, fakeMediaSource, fakeCampaign)
    }

    @Test
    fun `when setAuthData is called with appsflyer data set then calls report endpoint`() = runBlocking {
        val appsFlyerHelperImpl = AppsFlyerHelperImpl(
            { mockMarketingRepository },
            { mockOnboardingPreferencesImpl },
            { mockDeviceRepository },
        )

        val mockDevice: Device = mock()
        val fakeAuthTime = 123456789L

        whenever(mockDevice.aaid).thenReturn(fakeAaid)
        whenever(mockDeviceRepository.getDevice()).thenReturn(Observable.just(mockDevice))

        whenever(mockOnboardingPreferencesImpl.containsMarketingAuthTime()).thenReturn(false)
        whenever(mockOnboardingPreferencesImpl.containsMarketingIsSignUp()).thenReturn(false)
        appsFlyerHelperImpl.setPreProcessedAppsflyerData(mapOf("media_source" to fakeMediaSource, "c" to fakeCampaign))

        verify(mockMarketingRepository, never()).reportMarketingData(any(), any(), any(), any(), any())

        whenever(mockOnboardingPreferencesImpl.getMarketingAuthTime()).thenReturn(fakeAuthTime)
        whenever(mockOnboardingPreferencesImpl.getMarketingIsSignUp()).thenReturn(fakeIsAuthSignup)
        whenever(mockOnboardingPreferencesImpl.containsMarketingAuthTime()).thenReturn(true)
        whenever(mockOnboardingPreferencesImpl.containsMarketingIsSignUp()).thenReturn(true)
        appsFlyerHelperImpl.setAuthSuccess(fakeIsAuthSignup)

        verify(mockOnboardingPreferencesImpl, times(1)).removeMarketingIsSignUp()
        verify(mockOnboardingPreferencesImpl, times(1)).removeMarketingAuthTime()
        verify(mockMarketingRepository, times(1))
            .reportMarketingData(fakeAaid, fakeIsAuthSignup, fakeAuthTime, fakeMediaSource, fakeCampaign)
    }

    companion object {
        private const val fakeIsAuthSignup = true
        private const val fakeAaid = "fake_aaid"
        private const val fakeMediaSource = "fake_media_source"
        private const val fakeCampaign = "fake_campaign"
    }
}
