package `in`.okcredit.onboarding.enterotp.usecase

import `in`.okcredit.merchant.device.DeviceUtils
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.server.AuthApiClient
import tech.okcredit.android.auth.server.AuthApiClient.RequestOtpMedium
import javax.inject.Inject

class FetchFallbackOptionsOtp @Inject constructor(
    private val authService: Lazy<AuthService>,
    private val deviceUtils: Lazy<DeviceUtils>,
) {

    fun execute(mobileNumber: String): Single<ArrayList<Int>> {
        return authService.get().requestFallbackOptions(mobileNumber)
            .map {
                var filteredIntentList = ArrayList<Int>()

                for (fallbackOption in it.retry_options) {
                    if (fallbackOption.destination == AuthApiClient.RetryDestination.PRIMARY.key) {
                        filteredIntentList = fallbackOption.intents.filter { medium ->

                            // Either the Fallback option should not be WhatsApp or if it is,
                            // then WhatsApp should be installed on user's phone
                            (
                                !isFallbackOptionWhatsApp(medium) ||
                                    (
                                        isFallbackOptionWhatsApp(medium) &&
                                            (
                                                deviceUtils.get().isWhatsAppInstalled() ||
                                                    deviceUtils.get().isWhatsAppBusinessInstalled()
                                                )
                                        )
                                )
                        } as ArrayList
                    }
                }
                filteredIntentList
            }
    }

    private fun isFallbackOptionWhatsApp(medium: Int) =
        (RequestOtpMedium.getMedium(medium) == RequestOtpMedium.WHATSAPP)
}
