package `in`.okcredit.ui._base_v2

import `in`.okcredit.R
import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForAuthFailure
import `in`.okcredit.onboarding.otp_verification.OtpArgs.FLAG_AUTH_FAIL
import `in`.okcredit.shared.performance.PerformanceTracker
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import dagger.Lazy
import dagger.android.AndroidInjection
import tech.okcredit.android.base.LanguageABExperimentActivity
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.language.LocaleManager
import javax.inject.Inject

@Deprecated(message = "Use BaseFragment and create a fragment")
abstract class BaseActivity : LanguageABExperimentActivity() {

    @Inject
    lateinit var performanceTracker: Lazy<PerformanceTracker>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    fun showActionBar(showUp: Boolean) {
        findViewById<Toolbar>(R.id.toolbar)?.let {
            it.setTitleTextAppearance(this, R.style.RobotoBoldTextAppearance)
            it.visible()
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(showUp)
        }
    }

    override fun setTitle(titleId: Int) {
        supportActionBar?.setTitle(titleId)
    }

    override fun setTitle(title: CharSequence) {
        supportActionBar?.title = title
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Deprecated(message = "Use navigation component")
    open fun gotoLogin() {
        Analytics.track(AnalyticsEvents.AUTH_FAILURE)
        startActivity(
            startingIntentForAuthFailure(
                this,
                FLAG_AUTH_FAIL
            )
        )
        finishAffinity()
    }
}
