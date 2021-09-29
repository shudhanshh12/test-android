package tech.okcredit.home.ui.settings

sealed class SettingsClicks {
    object FINGERPRINT_CLICK : SettingsClicks()
    object SIGNOUT_CLICK : SettingsClicks()
}
