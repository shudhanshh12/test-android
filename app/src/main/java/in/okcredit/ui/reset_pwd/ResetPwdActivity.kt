package `in`.okcredit.ui.reset_pwd

import `in`.okcredit.R
import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.databinding.ResetpwdActivityBinding
import `in`.okcredit.ui._base_v2.BaseActivity
import `in`.okcredit.ui.reset_pwd.otp.OtpFragment
import `in`.okcredit.ui.reset_pwd.password.PasswordFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import tech.okcredit.android.base.extensions.disableScreanCapture
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class ResetPwdActivity : BaseActivity() {

    private val binding: ResetpwdActivityBinding by viewLifecycleScoped(ResetpwdActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.disableScreanCapture()

        setContentView(binding.root)

        showActionBar(false)
        title = getString(R.string.reset_pwd_title)
        showOtpFragment()
        Analytics.track(AnalyticsEvents.RESET_PWD_STARTED)
    }

    private fun showOtpFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                OtpFragment.newInstance(
                    intent.getStringExtra(
                        EXTRA_MOBILE
                    )
                )
            )
            .commitAllowingStateLoss()
    }

    fun showPasswordFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                PasswordFragment.newInstance(
                    intent.getStringExtra(
                        EXTRA_REQUESTED_SCREEN
                    )
                )
            )
            .commitAllowingStateLoss()
    }

    /****************************************************************
     * Dependency Injection
     */

    companion object {
        const val EXTRA_MOBILE = "ResetPwdActivity.mobile"
        const val EXTRA_REQUESTED_SCREEN = "ResetPwdActivity.requested_screen"
        const val REQUESTED_SCREEN_SECURITY = "SECURITY_SCREEN"
        const val REQUESTED_SCREEN_TX = "TX_SCREEN"

        @JvmStatic
        fun startingIntent(
            context: Context?,
            mobile: String,
            requestedScreen: String?
        ): Intent {
            val intent = Intent(context, ResetPwdActivity::class.java)
            intent.putExtra(EXTRA_MOBILE, mobile)
            intent.putExtra(EXTRA_REQUESTED_SCREEN, requestedScreen)
            return intent
        }
    }
}
