package `in`.okcredit.frontend.ui.payment_password

import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.backend._offline.usecase.GetMerchantPreferenceImpl
import `in`.okcredit.frontend.usecase.SetPaymentPassword
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import tech.okcredit.android.auth.IncorrectPassword
import tech.okcredit.android.auth.InvalidPassword
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.contract.MerchantPrefSyncStatus
import javax.inject.Inject

class PasswordEnableViewModel @Inject constructor(
    private val setPaymentPassword: Lazy<SetPaymentPassword>,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val getMerchantPreference: Lazy<GetMerchantPreferenceImpl>,
    private val getActiveBusiness: GetActiveBusiness,
    private val isPasswordSet: Lazy<IsPasswordSet>,
    private val navigator: PasswordEnableContract.Navigator,
    val merchantPrefSyncStatus: Lazy<MerchantPrefSyncStatus>
) : BasePresenter<PasswordEnableContract.State, PasswordEnableContract.PartialState>(PasswordEnableContract.State()) {

    override fun handle(): Observable<UiState.Partial<PasswordEnableContract.State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<PasswordEnableContract.Intent.Load>()
                .switchMap { checkNetworkHealth.get().execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        PasswordEnableContract.PartialState.SetNetworkError(false)
                    } else {
                        PasswordEnableContract.PartialState.NoChange
                    }
                },
            syncMerchantPref(),
            checkIsFourDigitPin(),

            // load page
            intent<PasswordEnableContract.Intent.Load>()
                .switchMap {
                    getMerchantPreference.get()
                        .execute(PreferenceKey.PAYMENT_PASSWORD)
                }
                .map {
                    PasswordEnableContract.PartialState.SetPasswordEnableStatus(java.lang.Boolean.parseBoolean(it))
                },

            // load page
            intent<PasswordEnableContract.Intent.Load>()
                .take(1)
                .switchMap { UseCase.wrapSingle(isPasswordSet.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PasswordEnableContract.PartialState.NoChange
                        is Result.Failure -> PasswordEnableContract.PartialState.NoChange
                        is Result.Success -> PasswordEnableContract.PartialState.SetIsPasswordEnabled(it.value)
                    }
                },

            // load page
            intent<PasswordEnableContract.Intent.Load>()
                .take(1)
                .switchMap { UseCase.wrapSingle(merchantPrefSyncStatus.get().checkMerchantPrefSync()) }
                .map {
                    when (it) {
                        is Result.Progress -> PasswordEnableContract.PartialState.NoChange
                        is Result.Failure -> {
                            if (isInternetIssue(it.error)) PasswordEnableContract.PartialState.SetNetworkError(true)
                            else PasswordEnableContract.PartialState.NoChange
                        }
                        is Result.Success -> PasswordEnableContract.PartialState.SetIsMerchantSync(it.value)
                    }
                },
            intent<PasswordEnableContract.Intent.SyncMerchantPref>()
                .map {
                    navigator.syncDone()
                    PasswordEnableContract.PartialState.NoChange
                },
            intent<PasswordEnableContract.Intent.UpdatePinClicked>()
                .map {
                    navigator.showUpdatePinDialog()
                    PasswordEnableContract.PartialState.NoChange
                },
            intent<PasswordEnableContract.Intent.SetNewPinClicked>()
                .map {
                    navigator.showSetNewPinDialog()
                    PasswordEnableContract.PartialState.NoChange
                },
            // enable / disable password
            intent<PasswordEnableContract.Intent.ChangePasswordEnableStatus>()
                .map {
                    if (it.isEnterPasswordMode.not())
                        navigator.goBack()
                    else
                        navigator.gotoEnterPinScreen()
                    PasswordEnableContract.PartialState.ChangeScreenPassword(it.isEnterPasswordMode)
                },

            // forgot password
            intent<PasswordEnableContract.Intent.OnForgotPasswordClicked>()
                .switchMap { getActiveBusiness.execute() }
                .map {
                    navigator.gotoForgotPasswordScreen(it.mobile)
                    PasswordEnableContract.PartialState.NoChange
                },

            // submit password
            intent<PasswordEnableContract.Intent.SubmitPassword>()
                .switchMap {
                    setPaymentPassword.get().execute(SetPaymentPassword.Request(it.status))
                }
                .map {
                    when (it) {
                        is Result.Success -> {
                            Analytics.track(
                                AnalyticsEvents.CONFIRM_PASSWORD,
                                EventProperties
                                    .create()
                                    .with(PropertyKey.SOURCE, "payment_password_settings")
                            )
                            PasswordEnableContract.PartialState.ChangeScreenPassword(false)
                        }
                        is Result.Progress -> PasswordEnableContract.PartialState.ShowLoader
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    PasswordEnableContract.PartialState.NoChange
                                }
                                (it.error is IncorrectPassword || it.error.cause is IncorrectPassword) -> PasswordEnableContract.PartialState.SetPasswordEnableErrorStatus(
                                    true
                                )
                                (it.error is InvalidPassword || it.error.cause is InvalidPassword) -> PasswordEnableContract.PartialState.SetPasswordEnableErrorStatus(
                                    true
                                )
                                isInternetIssue(it.error) -> PasswordEnableContract.PartialState.SetNetworkError(true)
                                else -> throw RuntimeException("unexpected error", it.error)
                            }
                        }
                    }
                }
        )
    }

    private fun checkIsFourDigitPin(): Observable<PasswordEnableContract.PartialState> {
        return intent<PasswordEnableContract.Intent.CheckIsFourDigit>()
            .switchMap {
                UseCase.wrapSingle(
                    getMerchantPreference.get().execute(PreferenceKey.FOUR_DIGIT_PIN).firstOrError()
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> PasswordEnableContract.PartialState.NoChange
                    is Result.Success -> {
                        navigator.checkFourDigitPin(it.value.toBoolean())
                        PasswordEnableContract.PartialState.SetIsFourdigitPin(it.value.toBoolean())
                    }
                    is Result.Failure -> {
                        if (isInternetIssue(it.error)) {
                            PasswordEnableContract.PartialState.SetNetworkError(true)
                        } else
                            PasswordEnableContract.PartialState.ErrorState
                    }
                }
            }
    }

    private fun syncMerchantPref(): Observable<PasswordEnableContract.PartialState> {
        return intent<PasswordEnableContract.Intent.SyncMerchantPref>()
            .switchMap {
                UseCase.wrapCompletable(merchantPrefSyncStatus.get().execute())
            }
            .map {
                when (it) {
                    is Result.Progress -> PasswordEnableContract.PartialState.NoChange
                    is Result.Success -> {
                        navigator.syncDone()
                        PasswordEnableContract.PartialState.SetIsMerchantSync(true)
                    }
                    is Result.Failure -> {
                        if (isInternetIssue(it.error)) {
                            PasswordEnableContract.PartialState.SetNetworkError(true)
                        } else
                            PasswordEnableContract.PartialState.ErrorState
                    }
                }
            }
    }

    override fun reduce(
        currentState: PasswordEnableContract.State,
        partialState: PasswordEnableContract.PartialState
    ): PasswordEnableContract.State {
        return when (partialState) {
            is PasswordEnableContract.PartialState.SetPasswordEnableStatus -> currentState.copy(isPasswordEnable = partialState.value)
            is PasswordEnableContract.PartialState.ChangeScreenPassword -> currentState.copy(
                isEnterPasswordMode = partialState.isPasswordMode,
                isIncorrectPassword = false,
                loader = false
            )
            is PasswordEnableContract.PartialState.SetPasswordEnableErrorStatus -> currentState.copy(
                isIncorrectPassword = partialState.status,
                loader = false
            )
            is PasswordEnableContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.status,
                loader = false
            )
            is PasswordEnableContract.PartialState.ShowLoader -> currentState.copy(loader = true)
            is PasswordEnableContract.PartialState.HideLoader -> currentState.copy(loader = false)
            is PasswordEnableContract.PartialState.NoChange -> currentState
            is PasswordEnableContract.PartialState.SetIsPasswordEnabled -> currentState.copy(isPasswordSet = partialState.isPasswordSet)
            is PasswordEnableContract.PartialState.SetIsFourdigitPin -> currentState.copy(isFourDigitPin = partialState.isFourDigitPin)
            is PasswordEnableContract.PartialState.SetIsMerchantSync -> currentState.copy(isMerchantSync = partialState.isMerchantSync)
            is PasswordEnableContract.PartialState.ErrorState -> currentState.copy(error = true)
        }
    }
}
