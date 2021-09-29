package `in`.okcredit.onboarding.enterotp.usecase

import `in`.okcredit.merchant.device.DeviceUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.server.AuthApiClient

class FetchFallbackOptionsOtpTest {
    private val authService: AuthService = mock()
    private val deviceUtils: DeviceUtils = mock()

    private val primaryRetryDestination = AuthApiClient.FallbackOption(
        destination = AuthApiClient.RetryDestination.PRIMARY.key,
        intents = arrayListOf()
    )

    private val secondaryRetryDestination = AuthApiClient.FallbackOption(
        destination = AuthApiClient.RetryDestination.SECONDARY.key,
        intents = arrayListOf()
    )

    private val smsFallbackOption = AuthApiClient.RequestOtpMedium.SMS.key
    private val whatsAppFallbackOption = AuthApiClient.RequestOtpMedium.WHATSAPP.key
    private val callFallbackOption = AuthApiClient.RequestOtpMedium.CALL.key
    private val mobileNumber = "1234567890"

    private val fallbackOptionsOtp = FetchFallbackOptionsOtp({ authService }, { deviceUtils })

    @Test
    fun `when WhatsApp is not installed then it should be removed from the list`() {

        val unfilteredList = arrayListOf(smsFallbackOption, whatsAppFallbackOption, callFallbackOption)
        val filteredList = arrayListOf(smsFallbackOption, callFallbackOption)

        whenever(deviceUtils.isWhatsAppInstalled()).thenReturn(false)
        whenever(deviceUtils.isWhatsAppBusinessInstalled()).thenReturn(false)

        whenever(authService.requestFallbackOptions(mobileNumber)).thenReturn(
            Single.just(
                AuthApiClient.FallbackOptionResponse(
                    arrayListOf(primaryRetryDestination.copy(intents = unfilteredList), secondaryRetryDestination)
                )
            )
        )

        val testObserver = fallbackOptionsOtp.execute(mobileNumber).test()

        testObserver.assertValue(filteredList)

        verify(authService).requestFallbackOptions(mobileNumber)
        verify(deviceUtils).isWhatsAppInstalled()
        verify(deviceUtils).isWhatsAppBusinessInstalled()
    }

    @Test
    fun `when only personal WhatsApp is installed then it should not be removed from the list`() {

        val unfilteredList = arrayListOf(smsFallbackOption, whatsAppFallbackOption, callFallbackOption)
        val filteredList = arrayListOf(smsFallbackOption, whatsAppFallbackOption, callFallbackOption)

        whenever(deviceUtils.isWhatsAppInstalled()).thenReturn(true)
        whenever(deviceUtils.isWhatsAppBusinessInstalled()).thenReturn(false)

        whenever(authService.requestFallbackOptions(mobileNumber)).thenReturn(
            Single.just(
                AuthApiClient.FallbackOptionResponse(
                    arrayListOf(primaryRetryDestination.copy(intents = unfilteredList), secondaryRetryDestination)
                )
            )
        )

        val testObserver = fallbackOptionsOtp.execute(mobileNumber).test()

        testObserver.assertValue(filteredList)

        verify(authService).requestFallbackOptions(mobileNumber)
        verify(deviceUtils).isWhatsAppInstalled()
    }

    @Test
    fun `when only business WhatsApp is installed then it should not be removed from the list`() {

        val unfilteredList = arrayListOf(smsFallbackOption, whatsAppFallbackOption, callFallbackOption)
        val filteredList = arrayListOf(smsFallbackOption, whatsAppFallbackOption, callFallbackOption)

        whenever(deviceUtils.isWhatsAppInstalled()).thenReturn(false)
        whenever(deviceUtils.isWhatsAppBusinessInstalled()).thenReturn(true)

        whenever(authService.requestFallbackOptions(mobileNumber)).thenReturn(
            Single.just(
                AuthApiClient.FallbackOptionResponse(
                    arrayListOf(primaryRetryDestination.copy(intents = unfilteredList), secondaryRetryDestination)
                )
            )
        )

        val testObserver = fallbackOptionsOtp.execute(mobileNumber).test()

        testObserver.assertValue(filteredList)

        verify(authService).requestFallbackOptions(mobileNumber)
        verify(deviceUtils).isWhatsAppInstalled()
        verify(deviceUtils).isWhatsAppBusinessInstalled()
    }

    @Test
    fun `when both WhatsApps are installed then it should not be from removed the list`() {

        val unfilteredList = arrayListOf(smsFallbackOption, whatsAppFallbackOption, callFallbackOption)
        val filteredList = arrayListOf(smsFallbackOption, whatsAppFallbackOption, callFallbackOption)

        whenever(deviceUtils.isWhatsAppInstalled()).thenReturn(true)
        whenever(deviceUtils.isWhatsAppBusinessInstalled()).thenReturn(true)

        whenever(authService.requestFallbackOptions(mobileNumber)).thenReturn(
            Single.just(
                AuthApiClient.FallbackOptionResponse(
                    arrayListOf(primaryRetryDestination.copy(intents = unfilteredList), secondaryRetryDestination)
                )
            )
        )

        val testObserver = fallbackOptionsOtp.execute(mobileNumber).test()

        testObserver.assertValue(filteredList)

        verify(authService).requestFallbackOptions(mobileNumber)
        verify(deviceUtils).isWhatsAppInstalled()
    }
}
