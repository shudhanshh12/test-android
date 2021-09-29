package `in`.okcredit.ui.app_lock.forgot

import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.databinding.ScreenForgotAppLockBinding
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForEnterOtpScreen
import `in`.okcredit.onboarding.contract.OnboardingConstants
import `in`.okcredit.ui._base_v2.BaseActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class ForgotAppLockActivity : BaseActivity() {

    companion object {
        @JvmStatic
        fun startingIntent(context: Context?): Intent {
            return Intent(context, ForgotAppLockActivity::class.java)
        }
    }

    private val binding: ScreenForgotAppLockBinding by viewLifecycleScoped(ScreenForgotAppLockBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Analytics.track(AnalyticsEvents.FORGOT_APP_LOCK_SCREEN)

        binding.ok.setOnClickListener {
            startActivity(
                startingIntentForEnterOtpScreen(
                    this,
                    "",
                    OnboardingConstants.FLAG_FORGOT_PATTERN,
                    signOutAllDevices = false,
                    isGooglePopupSelected = false
                )
            )
        }
    }
}
