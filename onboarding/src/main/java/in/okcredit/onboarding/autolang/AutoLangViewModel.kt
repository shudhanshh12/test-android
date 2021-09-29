package `in`.okcredit.onboarding.autolang

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.backend.contract.Authenticate
import `in`.okcredit.backend.contract.CheckMobileStatus
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics
import `in`.okcredit.onboarding.autolang.AutoLangContract.*
import `in`.okcredit.onboarding.contract.OnboardingConstants
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.onboarding.language.usecase.GetLanguages
import `in`.okcredit.onboarding.language.usecase.SelectLanguage
import `in`.okcredit.onboarding.utils.TrueCallerHelper
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import tech.okcredit.android.auth.Credential
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import timber.log.Timber
import javax.inject.Inject

class AutoLangViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam(OnboardingConstants.ARG_MOBILE) var mobileNumber: String,
    private val authenticate: Lazy<Authenticate>,
    private val checkMobileStatus: Lazy<CheckMobileStatus>,
    private val onboardingAnalytics: Lazy<OnboardingAnalytics>,
    private val getLanguages: Lazy<GetLanguages>,
    private val selectLanguage: Lazy<SelectLanguage>,
    private val onboardingPreferences: Lazy<OnboardingPreferences>,
    private val isTrueCallerHelper: Lazy<TrueCallerHelper>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    private var flow = PropertyValue.LOGIN

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(
            intent<Intent.Load>()
                .map {
                    PartialState.SetMobileNumber(mobileNumber)
                },

            intent<Intent.Load>()
                .switchMap { getLanguages.get().execute(Unit) }
                .map {
                    when (it) {
                        is Result.Progress -> {
                            PartialState.NoChange
                        }
                        is Result.Success -> {
                            PartialState.SetLanguages(it.value)
                        }
                        is Result.Failure -> {
                            PartialState.StopLoading
                        }
                    }
                },

            intent<Intent.Load>()
                .map {
                    pushIntent(Intent.NumberReadPopUp)
                    PartialState.NoChange
                },

            intent<Intent.LanguageSelected>()
                .switchMapCompletable {
                    onboardingPreferences.get().setUserSelectedLanguage(it.selectedLanguage)
                    selectLanguage.get().execute(it.selectedLanguage).onErrorComplete()
                }
                .toSingleDefault(PartialState.NoChange)
                .toObservable(),

            // checking mobile is registered for analytics
            intent<Intent.CheckMobileStatus>()
                .switchMap {
                    mobileNumber = it.mobileNumber
                    UseCase.wrapSingle(checkMobileStatus.get().execute(mobileNumber))
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            flow = if (it.value) {
                                PropertyValue.LOGIN
                            } else {
                                PropertyValue.REGISTER
                            }
                            PartialState.NoChange
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            intent<Intent.NumberReadPopUp>()
                .switchMap { wrap(isTrueCallerHelper.get().isTrueCallerInstalled()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.ShowTrueCallerDialog(it.value))
                            PartialState.SetTrueCallerInstalled(it.value)
                        }
                        is Result.Failure -> {
                            PartialState.StopLoading
                        }
                    }
                },

            // submit mobile
            intent<Intent.SubmitMobile>()
                .map {
                    emitViewEvent(ViewEvent.GoToOtpScreen(it.text))
                    PartialState.SetMobileNumber(it.text)
                },

            intent<Intent.LoadingState>()
                .map {
                    if (it.isShown) {
                        PartialState.ShowLoading
                    } else {
                        PartialState.StopLoading
                    }
                },

            intent<Intent.TrueCallerLogin>()
                .switchMap {
                    onboardingAnalytics.get().logBreadcrumb("Truecaller login intent received")
                    authenticate.get().execute(Credential.Truecaller(it.payload, it.signature))
                }
                .map {
                    when (it) {
                        is Result.Progress -> {
                            onboardingAnalytics.get().logBreadcrumb("Truecaller login in progress")
                            PartialState.ShowLoading
                        }
                        is Result.Success -> {
                            onboardingAnalytics.get().logBreadcrumb("Truecaller login in successful")
                            val newlyRegisteredUser = it.value.first
                            val exitingUserEnabledAppLock = it.value.second

                            Timber.i("newlyRegisteredUser $newlyRegisteredUser")

                            onboardingAnalytics.get().trackFlow(flow, mobileNumber)

                            if (newlyRegisteredUser) { // new user successfully registered
                                onboardingAnalytics.get().setRegisterUserProperty()
                                onboardingAnalytics.get()
                                    .trackRegistrationSuccess(type = OnboardingAnalytics.OnboardingPropertyValue.TRUE_CALLER)
                                emitViewEvent(ViewEvent.GoToEnterNameScreen)
                            } else {
                                if (exitingUserEnabledAppLock) {
                                    onboardingAnalytics.get().trackAppLockEnabled(PropertyValue.TRUE)
                                    emitViewEvent(ViewEvent.GoToAppLockAuthentication)
                                } else {
                                    onboardingAnalytics.get().trackAppLockEnabled(PropertyValue.FALSE)
                                    emitViewEvent(ViewEvent.GoToSyncDataScreen)
                                }
                            }
                            onboardingAnalytics.get().trackLoginSuccess(
                                type = OnboardingAnalytics.OnboardingPropertyValue.TRUE_CALLER,
                                flow = flow,
                                register = newlyRegisteredUser.toString()
                            )
                            PartialState.VerifySuccess
                        }
                        is Result.Failure -> {
                            onboardingAnalytics.get().logBreadcrumb("Truecaller login in failure")
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    onboardingAnalytics.get()
                                        .logBreadcrumb(
                                            "Truecaller login in authentication failure",
                                            it.error.message
                                        )
                                    emitViewEvent(ViewEvent.GoToLogin)
                                    PartialState.StopLoading
                                }
                                isInternetIssue(it.error) -> {
                                    onboardingAnalytics.get()
                                        .logBreadcrumb("Truecaller login in network failure", it.error.message)
                                    PartialState.SetNetworkError(true)
                                }
                                else -> {
                                    onboardingAnalytics.get()
                                        .logBreadcrumb("Truecaller login in unknown failure", it.error.message)
                                    PartialState.ErrorState
                                }
                            }
                        }
                    }
                },
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is PartialState.StopLoading -> currentState.copy(isLoading = false)
            is PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is PartialState.VerifySuccess -> currentState.copy(isLoading = false, verifySuccess = true)
            is PartialState.SetAppLockABVariant -> currentState.copy(appLockAbUIVariant = partialState.uiAbVariant)
            is PartialState.SetMobileNumber -> currentState.copy(mobileNumber = partialState.mobileNumber)
            is PartialState.SetLanguages -> currentState.copy(isLoading = false, languages = partialState.languages)
            is PartialState.SetTrueCallerInstalled -> currentState.copy(
                isLoading = false,
                isTrueCallerInstalled = partialState.isTrueCallerInstalled
            )
            is PartialState.NoChange -> currentState
        }
    }
}
