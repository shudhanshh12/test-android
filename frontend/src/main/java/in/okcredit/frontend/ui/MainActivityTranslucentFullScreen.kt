package `in`.okcredit.frontend.ui

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.R
import `in`.okcredit.shared.base.BaseScreen
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.className
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivityTranslucentFullScreen : OkcActivity() {

    @Inject
    lateinit var tracker: Tracker

    companion object {
        const val ARG_SCREEN = "starting_screen"
        const val ARG_MERCHANT_INPUT_TYPE = "merchant input type"
        const val ARG_MERCHANT_INPUT_TITLE = "merchant input title"
        const val ARG_MERCHANT_INPUT_VALUE = "merchant input value"
        const val ARG_CATEGORY_ID = "merchant category id"
        const val ARG_LATITUDE = "merchant latitude"
        const val ARG_LONGITUDE = "merchant longitude"
        const val ARG_GPS = "merchant gps"
        const val ARG_IS_SOURCE_IN_APP_NOTIFICATION = "is source in app notification"
        const val ARG_SOURCE = "source"

        // Supplier Tutorial UI state (show full UI OR directly play video)
        const val ARG_SUPPLIER_TUTORIAL_STATE = "supplier tutorial state"
        const val ARG_SUPPLIER_TUTORIAL_SOURCE_SCREEN = "supplier tutorial source screen"

        const val APP_LOCK = 5

        const val MERCHANT_INPUT_SCREEN = 34

        @JvmStatic
        fun startingIntentForMerchantScreenInput(
            context: Context,
            inputType: Int,
            inputTitle: String,
            inputValue: String? = null,
            selectedCategoryId: String = "",
            latitude: Double = 0.0,
            longitude: Double = 0.0,
            gps: Boolean = false,
            isSourceInAppNotification: Boolean = false
        ): Intent {
            val intent = Intent(context, MainActivityTranslucentFullScreen::class.java)
            intent.putExtra(ARG_SCREEN, MERCHANT_INPUT_SCREEN)
            intent.putExtra(ARG_MERCHANT_INPUT_TYPE, inputType)
            intent.putExtra(ARG_MERCHANT_INPUT_TYPE, inputType)
            intent.putExtra(ARG_MERCHANT_INPUT_TITLE, inputTitle)
            intent.putExtra(ARG_MERCHANT_INPUT_VALUE, inputValue)
            intent.putExtra(ARG_CATEGORY_ID, selectedCategoryId)
            intent.putExtra(ARG_LATITUDE, latitude)
            intent.putExtra(ARG_LONGITUDE, longitude)
            intent.putExtra(ARG_GPS, gps)
            intent.putExtra(ARG_IS_SOURCE_IN_APP_NOTIFICATION, isSourceInAppNotification)
            return intent
        }

        @JvmStatic
        fun startingIntentForAppLockScreen(context: Context, source: String): Intent {
            val intent = Intent(context, MainActivityTranslucentFullScreen::class.java)
            intent.putExtra(ARG_SCREEN, APP_LOCK)
            intent.putExtra(ARG_SOURCE, source)
            return intent
        }
    }

    @Inject
    internal lateinit var mixpanelAPI: MixpanelAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        var finalHost: NavHostFragment = NavHostFragment.create(R.navigation.customer_flow_v2)
        when (intent.getIntExtra(ARG_SCREEN, MERCHANT_INPUT_SCREEN)) {
            MERCHANT_INPUT_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.merchant_input_flow)
            }

            APP_LOCK -> {
                finalHost = NavHostFragment.create(
                    R.navigation.applock_flow,
                    Bundle().apply {
                        val source = intent.getStringExtra(ARG_SOURCE)
                        putString(ARG_SOURCE, source)
                    }
                )
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_content, finalHost)
            .setPrimaryNavigationFragment(finalHost) // this is the equivalent to app:defaultNavHost="true"
            .commit()

        disableAutoFill()
        Timber.d("$className initialized")
    }

    private fun disableAutoFill() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            }
        } catch (e: Exception) {
        }
    }

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_content)
        if (navHostFragment?.childFragmentManager != null) {
            val fr: Fragment = navHostFragment.childFragmentManager.fragments[0]
            if (fr is BaseScreen<*>) {
                if (!fr.onBackPressed()) {
                    super.onBackPressed()
                }
            } else {
                super.onBackPressed()
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Completable
            .timer(300, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_content)
                if (navHostFragment?.childFragmentManager != null) {
                    val fr: Fragment = navHostFragment.childFragmentManager.fragments[0]
                    fr.onActivityResult(requestCode, resultCode, data)
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(this, R.id.main_content).navigateUp()
    }
}
