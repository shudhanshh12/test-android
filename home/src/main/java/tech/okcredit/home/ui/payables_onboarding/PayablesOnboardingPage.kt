package tech.okcredit.home.ui.payables_onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed class PayablesOnboardingPage {
    data class ImageBacked(@DrawableRes val res: Int, @StringRes val string: Int) : PayablesOnboardingPage()

    data class VideoBacked(val youtubeId: String, @StringRes val string: Int) : PayablesOnboardingPage()
}
