package `in`.okcredit.onboarding.enterotp.usecase

import `in`.okcredit.backend.contract.DeepLinkUrl
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.Lazy
import kotlinx.coroutines.withContext
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.OtpToken
import tech.okcredit.android.auth.server.AuthApiClient
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.mobile.mustParseMobile
import javax.inject.Inject

class WhatsAppLoginHelper @Inject constructor(
    private val authService: Lazy<AuthService>,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
    private val mixpanelAPI: Lazy<MixpanelAPI>,
    private val localeManager: Lazy<LocaleManager>,
) {

    suspend fun requestOtpCode(mobile: String): OtpToken = withContext(dispatcherProvider.get().io()) {
        val whatsAppAuthRequest = AuthApiClient.WhatsAppCodeRequest(
            mobile = mustParseMobile(mobile),
            distinct_id = mixpanelAPI.get().distinctId,
            redirect_url = DeepLinkUrl.HOME,
            lang = localeManager.get().getLanguage(),
            purpose = "login"
        )

        authService.get().whatsappRequestOtp(whatsAppAuthRequest)
    }
}
