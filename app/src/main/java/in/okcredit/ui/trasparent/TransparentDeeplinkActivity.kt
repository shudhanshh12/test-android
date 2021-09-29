package `in`.okcredit.ui.trasparent

import `in`.okcredit.R
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.frontend.contract.data.AppResume
import `in`.okcredit.frontend.ui.MainActivityTranslucentFullScreen
import `in`.okcredit.frontend.ui.applock.AppLockFragment
import `in`.okcredit.frontend.usecase.onboarding.applock.CheckAppLockAuthenticationImpl
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import `in`.okcredit.ui.app_lock.prompt.AppLockPromptActivity
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.Lazy
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import tech.okcredit.android.base.rxjava.SchedulerProvider
import javax.inject.Inject

class TransparentDeeplinkActivity : AppCompatActivity() {

    @Inject
    lateinit var collectionRepository: CollectionRepository
    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var checkAppLockAuthenticationImpl: Lazy<CheckAppLockAuthenticationImpl>

    @Inject
    lateinit var schedulerProvider: Lazy<SchedulerProvider>

    @Inject
    lateinit var onboardingPreferences: Lazy<OnboardingPreferencesImpl>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transparent_activity)

        when (intent.getStringExtra(TYPE)) {
            APP_LOCK -> authenticateUser()
        }
    }

    private fun authenticateUser() {
        compositeDisposable.add(
            checkAppLockAuthenticationImpl.get().execute()
                .subscribeOn(schedulerProvider.get().io())
                .observeOn(schedulerProvider.get().ui())

                .subscribe(
                    { type ->
                        when (type) {
                            AppResume.OLD_APP_LOCK_RESUME -> openOldAppLockScreen()
                            AppResume.NEW_APP_LOCK_RESUME -> openNewAppLockScreen()
                            else -> closeScreen()
                        }
                    },
                    {
                        finish()
                    }
                )
        )
    }

    private fun closeScreen() {
        finish()
        if (intent.getBooleanExtra(DO_NOT_ANIMATE_ACTIVITY_EXIT, false)) {
            overridePendingTransition(0, 0)
        }
    }

    private fun openOldAppLockScreen() {
        startActivityForResult(
            AppLockPromptActivity.startingIntent(this),
            OLD_APP_LOCK_AUTHENTICATED
        )
    }

    private fun openNewAppLockScreen() {
        val intent = Intent(this, MainActivityTranslucentFullScreen::class.java)
        intent.putExtra(
            MainActivityTranslucentFullScreen.ARG_SCREEN,
            MainActivityTranslucentFullScreen.APP_LOCK
        )
        intent.putExtra(
            MainActivityTranslucentFullScreen.ARG_SOURCE,
            AppLockFragment.AUTHENTICATE_APP_RESUME_SESSION_DEEPLINK
        )
        intent.action = `in`.okcredit.notification.DeepLinkActivity.ACTION_DEEP_LINK_COMPLEX
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OLD_APP_LOCK_AUTHENTICATED) {
                onboardingPreferences.get().setAppWasInBackgroundFor20Minutes(false)
                finish()
            } else {
                finishAffinity()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    companion object {
        const val TYPE = "TYPE"
        const val DO_NOT_ANIMATE_ACTIVITY_EXIT = "DO_NOT_ANIMATE_ACTIVITY_EXIT"
        const val APP_LOCK = "APP_LOCK"
        private const val OLD_APP_LOCK_AUTHENTICATED = 111

        @JvmStatic
        fun getIntent(context: Context, type: String): Intent {
            val intent = Intent(
                context,
                TransparentDeeplinkActivity::class.java
            )
            intent.action = `in`.okcredit.notification.DeepLinkActivity.ACTION_DEEP_LINK_COMPLEX
            intent.putExtra(TYPE, type)
            return intent
        }
    }
}
