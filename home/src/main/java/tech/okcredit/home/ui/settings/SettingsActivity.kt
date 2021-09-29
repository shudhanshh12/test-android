package tech.okcredit.home.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dagger.Lazy
import dagger.android.AndroidInjection
import dagger.android.HasAndroidInjector
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.home.R
import tech.okcredit.home.ui.settings.analytics.SettingsEventTracker
import tech.okcredit.home.ui.settings.dialogs.AppPinSetScreen
import javax.inject.Inject

class SettingsActivity : OkcActivity(), HasAndroidInjector {
    @Inject
    internal lateinit var settingsEventTracker: Lazy<SettingsEventTracker>

    companion object {
        val LAUNCH_APP_LOCK = 121
        const val NEW_PIN = "New Pin"
        const val UPDATE_PIN = "Pin Updated"
        const val PIN_STATUS = "PinStatus"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == LAUNCH_APP_LOCK)
            data?.let { intent ->
                val isAppPinConfirm = intent.getStringExtra(PIN_STATUS)
                isAppPinConfirm?.let {
                    if (it == NEW_PIN) {
                        settingsEventTracker.get().trackPinSet()
                        showBottomsheet(getString(R.string.app_security_pin))
                    }
                    if (it == UPDATE_PIN) {
                        settingsEventTracker.get().trackPinChanged()
                        showBottomsheet(getString(R.string.app_security_pin_updated))
                    }
                }
            }
    }

    fun showBottomsheet(title: String) {
        val bottomSheet = AppPinSetScreen.newInstance(title)
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }
}
