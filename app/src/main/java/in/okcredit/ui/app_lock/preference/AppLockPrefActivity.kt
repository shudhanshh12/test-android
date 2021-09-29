package `in`.okcredit.ui.app_lock.preference

import `in`.okcredit.R
import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.databinding.ScreenAppLockPrefBinding
import `in`.okcredit.ui._base_v2.BaseActivity
import `in`.okcredit.ui._dialog.NetworkErrorDialog
import `in`.okcredit.ui.app_lock.set.AppLockActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.firebase.perf.metrics.AddTrace
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.base.Traces
import javax.inject.Inject

class AppLockPrefActivity :
    BaseActivity(),
    AppLockPrefContract.View {

    companion object {
        fun startingIntent(context: Context?): Intent {
            return Intent(context, AppLockPrefActivity::class.java)
        }
    }

    @Inject
    lateinit var viewModel: AppLockPrefContract.Presenter

    private val binding: ScreenAppLockPrefBinding by viewLifecycleScoped(ScreenAppLockPrefBinding::inflate)

    @AddTrace(name = Traces.OnCreate_AppLockPref)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        showActionBar(true)
        setTitle(R.string.security_app_lock)

        binding.apply {
            appLockLayoutContainer.gone()
            appLockExplainationView.gone()
            loading.visible()
            appLockLayoutContainer.setOnClickListener {
                startActivity(AppLockActivity.startingIntent(this@AppLockPrefActivity))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.attachView(this)
    }

    override fun onPause() {
        super.onPause()
        viewModel.detachView()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun setAppLockStatus(isAppLockActive: Boolean) {
        Analytics.track(
            AnalyticsEvents.APP_LOCK_PREF_SCREEN,
            EventProperties
                .create()
                .with("isAppLockActive", isAppLockActive)
        )
        if (isAppLockActive) {
            binding.apply {
                appLockText.text = getString(R.string.app_lock_enabled)
                appLockTextSubtitle.setText(R.string.app_lock_disable)
                appLockTextSubtitle.setTextColor(ContextCompat.getColor(this@AppLockPrefActivity, R.color.tx_credit))
            }
        } else {
            binding.apply {
                appLockText.text = getString(R.string.app_lock_disabled)
                appLockTextSubtitle.setText(R.string.app_lock_enable)
                appLockTextSubtitle.setTextColor(ContextCompat.getColor(this@AppLockPrefActivity, R.color.tx_payment))
            }
        }
        binding.apply {
            appLockLayoutContainer.visible()
            appLockExplainationView.visible()
            loading.gone()
        }
    }

    override fun showError() {
        longToast(R.string.err_default)
        finish()
    }

    override fun showNoInternetMessage() {
        NetworkErrorDialog()
            .show(
                this,
                object : NetworkErrorDialog.Listener {
                    override fun onNetworkOk() {
                        viewModel.onInternetRestored()
                    }

                    override fun onCancel() {}
                }
            )
    }
}
