package `in`.okcredit.frontend.ui.applock

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.ui.MainActivityTranslucentFullScreen
import `in`.okcredit.frontend.usecase.onboarding.applock.SetAppLockStatus
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import timber.log.Timber
import javax.inject.Inject

class AppLockViewModel @Inject constructor(
    initialState: AppLockContract.State,
    private val setAppLockStatus: Lazy<SetAppLockStatus>,
    private val tracker: Lazy<Tracker>,
    @ViewModelParam(MainActivityTranslucentFullScreen.ARG_SOURCE) val source: String,
    private val onboardingPreferences: Lazy<OnboardingPreferencesImpl>,
) : BaseViewModel<AppLockContract.State, AppLockContract.PartialState, AppLockContract.ViewEvent>(
    initialState
) {

    private var authenticateSubject = PublishSubject.create<Unit>()
    private var authenticateDeeplinkSubject = PublishSubject.create<Unit>()

    // from setting screen

    private var lockSetupSettingScreenSubject = PublishSubject.create<Unit>()
    private var lockSetupMixPanelSubject = PublishSubject.create<Unit>()
    private var lockSetupExitingUser = PublishSubject.create<Unit>()

    private var setupCalled = false
    private var settingSetupCalled = false
    lateinit var exitType: String
    lateinit var userFlow: String

    override fun handle(): Observable<UiState.Partial<AppLockContract.State>> {
        return mergeArray(

            intent<AppLockContract.Intent.Load>()
                .map {
                    if (setupCalled.not()) {
                        when (source) {

                            AppLockFragment.AUTHENTICATE_APP_RESUME_SESSION -> authenticateSubject.onNext(Unit)
                            AppLockFragment.AUTHENTICATE_APP_RESUME_SESSION_DEEPLINK -> authenticateDeeplinkSubject.onNext(
                                Unit
                            )
                            AppLockFragment.LOCK_SETUP_SETTING_SCREEN -> lockSetupSettingScreenSubject.onNext(Unit)
                            AppLockFragment.LOCK_SETUP_LOGIN_FLOW -> lockSetupExitingUser.onNext(Unit)
                            AppLockFragment.LOCK_SETUP_INAPP_CARD -> lockSetupMixPanelSubject.onNext(Unit)
                        }
                        setupCalled = true
                    }
                    AppLockContract.PartialState.CurrentSource(source)
                },

            authenticateSubject
                .map {
                    emitViewEvent(AppLockContract.ViewEvent.AuthenticateAppResume)
                    AppLockContract.PartialState.NoChange
                },

            authenticateDeeplinkSubject
                .map {
                    emitViewEvent(AppLockContract.ViewEvent.AuthenticateAppResumeDeeplink)
                    AppLockContract.PartialState.NoChange
                },

            lockSetupExitingUser
                .map {
                    emitViewEvent(AppLockContract.ViewEvent.SetupLockForExistingUserLoginFlow)
                    AppLockContract.PartialState.NoChange
                },

            lockSetupMixPanelSubject
                .map {
                    emitViewEvent(AppLockContract.ViewEvent.SetupLockMixpanelInAppNotiFlow)
                    AppLockContract.PartialState.NoChange
                },

            lockSetupSettingScreenSubject
                .map {
                    if (settingSetupCalled.not()) {
                        if (onboardingPreferences.get().isAppLockEnabled()
                        ) { // lock screen already enabled , user turning off now , so authenticate him before turning it off
                            tracker.get().trackSecurityScreenAppLockEnableClick(PropertyValue.DISABLE)
                            emitViewEvent(AppLockContract.ViewEvent.AuthenticateAndTurnOffLockFromSettingScreen)
                        } else {
                            tracker.get().trackSecurityScreenAppLockEnableClick(PropertyValue.ENABLE)
                            emitViewEvent(AppLockContract.ViewEvent.SetupLockFromSettingScreen)
                        }
                        settingSetupCalled = true
                    }
                    AppLockContract.PartialState.NoChange
                },

            intent<AppLockContract.Intent.ExitScreen>()
                .map {
                    emitViewEvent(AppLockContract.ViewEvent.EXIT(exitType))
                    AppLockContract.PartialState.NoChange
                },

            intent<AppLockContract.Intent.AppLockEnabled>()
                .switchMap {
                    exitType = it.exit
                    setAppLockStatus.get().execute(true)
                }
                .map {
                    when (it) {
                        is Result.Progress -> AppLockContract.PartialState.NoChange
                        is Result.Success -> {

                            when (source) {
                                AppLockFragment.LOCK_SETUP_INAPP_CARD -> tracker.get().trackAppLockEnabled(
                                    screen = PropertyValue.HOME_PAGE,
                                    flow = PropertyValue.CARD
                                )
                                AppLockFragment.LOCK_SETUP_LOGIN_FLOW -> tracker.get().trackAppLockEnabled(
                                    screen = PropertyValue.LOGIN,
                                    flow = PropertyValue.LOGIN
                                )
                                AppLockFragment.LOCK_SETUP_SETTING_SCREEN -> tracker.get().trackAppLockEnabled(
                                    screen = PropertyValue.SETTINGS,
                                    flow = PropertyValue.SETTINGS
                                )
                            }
                            Timber.i("onActivityResult 4")
                            emitViewEvent(AppLockContract.ViewEvent.EXIT(exitType))
                            AppLockContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            AppLockContract.PartialState.NoChange
                        }
                    }
                },

            intent<AppLockContract.Intent.AppLockAuthenticated>()
                .map {
                    onboardingPreferences.get().setAppWasInBackgroundFor20Minutes(false)
                    if (it.userFlow == AppLockFragment.LAUNCHER_FLOW) {
                        emitViewEvent(AppLockContract.ViewEvent.EXIT(AppLockFragment.LOCK_RESUME_EXIT))
                    } else {
                        emitViewEvent(AppLockContract.ViewEvent.EXIT(AppLockFragment.LOCK_RESUME_DEEPLINK_EXIT))
                    }
                    AppLockContract.PartialState.NoChange
                },

            intent<AppLockContract.Intent.TurnOffLock>()
                .switchMap { setAppLockStatus.get().execute(false) }
                .map {
                    when (it) {
                        is Result.Success -> {
                            Timber.i("onActivityResult 4 TurnOffLock")
                            tracker.get().trackAppLockDisabled(PropertyValue.SETTINGS, PropertyValue.SETTINGS)
                            emitViewEvent(AppLockContract.ViewEvent.EXIT(AppLockFragment.ONLY_SCREEN_EXIT))
                            AppLockContract.PartialState.NoChange
                        }
                        else -> {
                            AppLockContract.PartialState.NoChange
                        }
                    }
                },
        )
    }

    override fun reduce(
        currentState: AppLockContract.State,
        partialState: AppLockContract.PartialState,
    ): AppLockContract.State {
        return when (partialState) {
            is AppLockContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is AppLockContract.PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is AppLockContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is AppLockContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is AppLockContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is AppLockContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is AppLockContract.PartialState.CurrentSource -> currentState.copy(source = partialState.source)
            is AppLockContract.PartialState.NoChange -> currentState
        }
    }
}
