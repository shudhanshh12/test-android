package `in`.okcredit.onboarding.social_validation.data

import dagger.Lazy
import javax.inject.Inject

class SocialValidationRepo @Inject constructor(
    private val socialValidationService: Lazy<SocialValidationService>,
) {
    suspend fun getScreens(locale: String, deviceId: String): List<Screen> {
        return socialValidationService.get()
            .getScreens(SocialValidationRequest(locale, deviceId))
            .screens
    }
}
