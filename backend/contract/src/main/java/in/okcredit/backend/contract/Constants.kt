package `in`.okcredit.backend.contract

import tech.okcredit.android.base.AppConfig
import tech.okcredit.secure_keys.KeyProvider

object Constants {

    // Mixpanel
    val MIXPANEL_TOKEN = mixpanelToken

    // Miscellaneous
    const val ABOUT_URL = "https://www.okcredit.in"
    const val PRIVACY_POLICY_URL = "https://www.okcredit.in/privacy"

    // NotificationData
    const val DEEPLINK_BASE_URL = BuildConfig.DEEPLINK_URL
    const val DEEPLINK_V2_BASE_URL = "okcredit://merchant/v1"
    const val HELP_EMAIL = "help@okcredit.in"

    // Tutorial videos
    const val DEFAULT_SUPPLIER_TUTORIAL_INTRO_VIDEO = "19srF4nRX3w"
    private val mixpanelToken: String
        get() = when (BuildConfig.FLAVOR) {
            AppConfig.FLAVOR_SERVER_STAGING -> {
                KeyProvider.getMixpanelStagingToken()
            }
            AppConfig.FLAVOR_SERVER_ALPHA -> {
                KeyProvider.getMixpanelAlphaToken()
            }
            else -> {
                KeyProvider.getMixpanelProdToken()
            }
        }
}
