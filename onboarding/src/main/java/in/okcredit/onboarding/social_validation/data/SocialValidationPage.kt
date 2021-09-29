package `in`.okcredit.onboarding.social_validation.data

sealed class SocialValidationPage {
    data class ImageBacked(val url: String, val duration: Long) : SocialValidationPage()

    data class VideoBacked(val url: String, val subtitle: String?) : SocialValidationPage()
}
