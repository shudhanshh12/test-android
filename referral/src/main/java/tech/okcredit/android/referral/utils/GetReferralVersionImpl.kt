package tech.okcredit.android.referral.utils

import `in`.okcredit.referral.contract.usecase.GetReferralVersion
import `in`.okcredit.referral.contract.utils.ReferralVersion
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.language.LocaleManager
import javax.inject.Inject

class GetReferralVersionImpl @Inject constructor(
    private val ab: Lazy<tech.okcredit.android.ab.AbRepository>,
    private val localeManager: Lazy<LocaleManager>,
) : GetReferralVersion {

    companion object {
        const val FEATURE_REFERRAL_REWARD = "referrer_reward"
        const val FEATURE_TARGETED_REFERRAL = "targeted_referral"

        private const val SHARE_APP_IMAGE_URL_MALAYALAM =
            "https://s3.ap-south-1.amazonaws.com/okcredit.app-assets/share_okc_ml.png"
        private const val SHARE_APP_IMAGE_URL_HINDI =
            "https://storage.googleapis.com/okcredit-referral-images/images/android_images/Hindi%20Info.png"
        private const val SHARE_APP_IMAGE_URL_ENGLISH =
            "https://storage.googleapis.com/okcredit-referral-images/images/android_images/English%20Info.png"
        private const val SHARE_APP_IMAGE_URL_MARATHI =
            "https://storage.googleapis.com/okcredit-referral-images/images/android_images/Marathi%20Info.png"
        private const val SHARE_APP_IMAGE_URL_DEFAULT = SHARE_APP_IMAGE_URL_ENGLISH
    }

    override fun execute(): Observable<ReferralVersion> {
        val observables = listOf(isReferralEnabled(), isTargetedReferralEnabled())
        return Observable.combineLatest(observables) {
            val isReferralEnabled = it[0] as Boolean
            val isTargetReferralVersion = it[1] as Boolean
            return@combineLatest when {
                isTargetReferralVersion && isReferralEnabled -> ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION
                isReferralEnabled -> ReferralVersion.REWARDS_ON_ACTIVATION
                isTargetReferralVersion -> ReferralVersion.TARGETED_REFERRAL
                else -> ReferralVersion.NO_REWARD
            }
        }
    }

    private fun isReferralEnabled() = ab.get().isFeatureEnabled(FEATURE_REFERRAL_REWARD)

    private fun isTargetedReferralEnabled() = ab.get().isFeatureEnabled(FEATURE_TARGETED_REFERRAL)

    fun getShareAppImagePath(): String {
        return when {
            localeManager.get().isEnglishLocale() -> SHARE_APP_IMAGE_URL_ENGLISH
            localeManager.get().isHindiLocale() -> SHARE_APP_IMAGE_URL_HINDI
            localeManager.get().isMarathiLocale() -> SHARE_APP_IMAGE_URL_MARATHI
            localeManager.get().isMalayalamLocale() -> SHARE_APP_IMAGE_URL_MALAYALAM
            else -> SHARE_APP_IMAGE_URL_DEFAULT
        }
    }

    fun getShareAppImageName() = "share_app_image_${localeManager.get().getLanguage()}.jpg"
}
