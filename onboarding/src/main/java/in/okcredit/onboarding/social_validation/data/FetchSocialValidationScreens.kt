package `in`.okcredit.onboarding.social_validation.data

import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.onboarding.social_validation.data.SocialValidationPage.ImageBacked
import `in`.okcredit.onboarding.social_validation.data.SocialValidationPage.VideoBacked
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.awaitFirst
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.language.LocaleManager
import javax.inject.Inject

class FetchSocialValidationScreens @Inject constructor(
    private val socialValidationRepo: Lazy<SocialValidationRepo>,
    private val localeManager: Lazy<LocaleManager>,
    private val deviceRepository: Lazy<DeviceRepository>,
) {

    suspend fun execute(): List<SocialValidationPage> = withContext(Dispatchers.IO) {
        val locale = localeManager.get().getLanguage()
        val deviceId = deviceRepository.get().getDevice().awaitFirst().id

        socialValidationRepo.get().getScreens(locale, deviceId).mapNotNull {
            when (it.type) {
                "infographic" -> ImageBacked(it.url, it.duration ?: return@mapNotNull null)
                "video" -> VideoBacked(it.url, it.subtitle)
                else -> null
            }
        }
    }
}
