package `in`.okcredit.onboarding.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.onboarding.BuildConfig
import `in`.okcredit.onboarding.utils.TrueCallerHelper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.scottyab.rootbeer.RootBeer
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.language.LocaleManager

class OnboardingRepoAnalyticsTest {

    private val mockAnalyticsProvider: AnalyticsProvider = mock()
    private val mockLocaleManager: LocaleManager = mock()
    private val mockTrueCallerHelper: TrueCallerHelper = mock()
    private val mockRootbeer: RootBeer = mock()

    private val onboardingAnalytics = OnboardingAnalytics(
        { mockAnalyticsProvider },
        { mockLocaleManager },
        { mockTrueCallerHelper },
        { mockRootbeer }
    )

    companion object {
        private const val fakeLocaleLanguageCode = "mr"
    }

    @Before
    fun setup() {
        whenever(mockLocaleManager.getLanguage()).thenReturn(fakeLocaleLanguageCode)
    }

    @Test
    fun `trackViewLanguageScreen() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackViewLanguageScreen()

        verify(mockAnalyticsProvider).trackEvents(
            "View Language",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Flow" to "Login",
                "Type" to "Tile"
            )
        )
    }

    @Test
    fun `trackViewMobileScreen() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackViewMobileScreen()

        verify(mockAnalyticsProvider).trackEvents(
            "View Mobile Screen",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Flow" to "Login"
            )
        )
    }

    @Test
    fun `trackSelectMobile() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackSelectMobile("OkCredit")

        verify(mockAnalyticsProvider).trackEvents(
            "Select Mobile",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Flow" to "Login",
                "Type" to "OkCredit"
            )
        )
    }

    @Test
    fun `trackSkip() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackSkip("OkCredit", "hurry")

        verify(mockAnalyticsProvider).trackEvents(
            "Skip",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Flow" to "Login",
                "Type" to "OkCredit",
                "Method" to "hurry"
            )
        )
    }

    @Test
    fun `trackVerifyMobile() should call trackEvents with correct event and properties when truecaller is installed`() {
        whenever(mockTrueCallerHelper.isTrueCallerInstalled()).thenReturn(Single.just(true))

        onboardingAnalytics.trackVerifyMobile(true)

        verify(mockAnalyticsProvider).trackEvents(
            "Verify Mobile",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Flow" to "Login",
                "Has Truecaller" to "True"
            )
        )
    }

    @Test
    fun `trackVerifyMobile() should call trackEvents with correct event and properties when truecaller is not installed`() {
        whenever(mockTrueCallerHelper.isTrueCallerInstalled()).thenReturn(Single.just(false))

        onboardingAnalytics.trackVerifyMobile(false)

        verify(mockAnalyticsProvider).trackEvents(
            "Verify Mobile",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Flow" to "Login",
                "Has Truecaller" to "False"
            )
        )
    }

    @Test
    fun `trackNumberReadDisplayed() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackNumberReadDisplayed("GooglePopup Displayed")

        verify(mockAnalyticsProvider).trackEvents(
            "GooglePopup Displayed",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Flow" to "Login"
            )
        )
    }

    @Test
    fun `trackTrueCallerFailure() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackTrueCallerFailure("Truecaller", 0)

        verify(mockAnalyticsProvider).trackEvents(
            "Failure",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Reason" to "ERROR_TYPE_INTERNAL",
                "Flow" to "Login",
                "Type" to "Truecaller"
            )
        )
    }

    @Test
    fun `trackWhatsAppFailure() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackWhatsAppFailure("WhatsApp", "Login")

        verify(mockAnalyticsProvider).trackEvents(
            "Failure",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Reason" to "Whatsapp Not Installed",
                "Flow" to "Login",
                "Type" to "WhatsApp"
            )
        )
    }

    @Test
    fun `trackMobileNumberHintNotFound() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackMobileNumberHintNotFound("Truecaller")

        verify(mockAnalyticsProvider).trackEvents(
            "Failure",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Reason" to "No Mobile Number Found",
                "Flow" to "Login",
                "Type" to "Truecaller"
            )
        )
    }

    @Test
    fun `trackRegistrationStarted() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackRegistrationStarted("Manual")

        verify(mockAnalyticsProvider).trackEvents(
            "Register: Started",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Type" to "Manual",
            )
        )
    }

    @Test
    fun `trackLoginStarted() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackLoginStarted("Manual")

        verify(mockAnalyticsProvider).trackEvents(
            "Login Started",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Type" to "Manual",
            )
        )
    }

    @Test
    fun `trackViewOnboardingScreen() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackViewOnboardingScreen()

        verify(mockAnalyticsProvider).trackEvents(
            "View Onboarding Screen",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Flow" to "Login"
            )
        )
    }

    @Test
    fun `setTruecallerUserProperty() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.setTruecallerUserProperty(
            "Ram",
            "Mohan",
            "+91",
            "Banaras",
            "rammohan@gmail.com",
            "pan wali gali",
            "",
            "male"
        )

        verify(mockAnalyticsProvider).setUserProperty(
            mutableMapOf(
                "TC First Name" to "Ram",
                "TC Last Name" to "Mohan",
                "TC Code Code" to "+91",
                "TC City" to "Banaras",
                "TC Email" to "rammohan@gmail.com",
                "TC Address" to "pan wali gali",
                "TC Profile Url" to "",
                "TC Gender" to "male"
            )
        )
    }

    @Test
    fun `setRegisterUserProperty() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.setRegisterUserProperty()

        verify(mockAnalyticsProvider).setUserProperty(
            mutableMapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Registered Version" to BuildConfig.VERSION_CODE
            )
        )
    }

    @Test
    fun `trackRegistrationSuccess() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackRegistrationSuccess("True Caller")

        verify(mockAnalyticsProvider).trackEvents(
            "Register: Successful",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Type" to "True Caller"
            )
        )
    }

    @Test
    fun `trackLoginSuccess() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackLoginSuccess("True Caller", "Old Variant", "New Registration")

        verify(mockAnalyticsProvider).trackEvents(
            "Login Success",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Flow" to "Old Variant",
                "Type" to "True Caller",
                "Register" to "New Registration",
                "Is Rooted Phone" to false
            )
        )
    }

    @Test
    fun `trackAppLockEnabled() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackAppLockEnabled("true")

        verify(mockAnalyticsProvider).trackEvents(
            "IsAppLock Enabled",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Type" to "true"
            )
        )
    }

    @Test
    fun `trackMobileNumberCleared() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackMobileNumberCleared()

        verify(mockAnalyticsProvider).trackEvents(
            "Cleared",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Type" to "Mobile",
                "Screen" to "Mobile Screen"
            )
        )
    }

    @Test
    fun `trackViewRequestOTP() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackViewRequestOTP("Manual")

        verify(mockAnalyticsProvider).trackEvents(
            "Request OTP",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Flow" to "Login",
                "Type" to "Manual"
            )
        )
    }

    @Test
    fun `trackOtpReceived() should call trackEvents with correct event and properties`() {
        onboardingAnalytics.trackOtpReceived("Login", "Manual")

        verify(mockAnalyticsProvider).trackEvents(
            "OTP Received",
            mapOf(
                "Auto lang current language" to fakeLocaleLanguageCode,
                "Flow" to "Login",
                "Type" to "Manual"
            )
        )
    }
}
