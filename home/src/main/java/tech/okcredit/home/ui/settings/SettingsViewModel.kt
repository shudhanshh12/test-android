package tech.okcredit.home.ui.settings

import `in`.okcredit.backend.contract.GetMerchantPreference
import `in`.okcredit.backend.contract.Signout
import `in`.okcredit.individual.contract.PreferenceKey.FOUR_DIGIT_PIN
import `in`.okcredit.individual.contract.PreferenceKey.PAYMENT_PASSWORD
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.payment.contract.usecase.IsPspUpiFeatureEnabled
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.GetConnectionStatus
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.auth.IncorrectPassword
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.contract.MerchantPrefSyncStatus
import tech.okcredit.home.ui.settings.usecase.ActiveLanguage
import tech.okcredit.home.ui.settings.usecase.CheckAppLock
import tech.okcredit.home.ui.settings.usecase.SetFingerprintLockStatus
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val initialState: SettingsContract.State,
    private val checkNetworkHealth: GetConnectionStatus,
    private val activeLanguage: Lazy<ActiveLanguage>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val checkAppLock: Lazy<CheckAppLock>,
    private val isPasswordSet: Lazy<IsPasswordSet>,
    private val getMerchantPreference: Lazy<GetMerchantPreference>,
    private val signout: Lazy<Signout>,
    private val setFingerprintLockStatus: Lazy<SetFingerprintLockStatus>,
    private val merchantPrefSyncStatus: Lazy<MerchantPrefSyncStatus>,
    private val isPspUpiFeatureEnabled: Lazy<IsPspUpiFeatureEnabled>,
) : BaseViewModel<SettingsContract.State, SettingsContract.PartialState, SettingsContract.ViewEvent>(
    initialState
) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private var isSetPassword: Boolean = false
    private var isFingerPrintEnabled = false
    private lateinit var settingsClick: SettingsClicks
    private lateinit var isFourDigitPinFor: SettingsClicks

    override fun handle(): Observable<UiState.Partial<SettingsContract.State>> {
        return mergeArray(

            syncMerchantPref(),
            checkIsFourDigitPin(),
            setFingerprintinSharedPref(),
            loadFingerPrintEnableStatus(),

            // hide network error when network becomes available
            intent<SettingsContract.Intent.Load>()
                .switchMap { checkNetworkHealth.execute() }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        reload.onNext(Unit)
                        SettingsContract.PartialState.ClearNetworkError
                    } else {
                        SettingsContract.PartialState.NoChange
                    }
                },

            // check applock active or not
            intent<SettingsContract.Intent.Resume>()
                .switchMap { UseCase.wrapObservable(checkAppLock.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> SettingsContract.PartialState.NoChange
                        is Result.Success -> {
                            SettingsContract.PartialState.SetAppLockType(
                                it.value.first,
                                it.value.second
                            )
                        }
                        is Result.Failure -> SettingsContract.PartialState.NoChange
                    }
                },

            intent<SettingsContract.Intent.Resume>()
                .switchMap { UseCase.wrapSingle(merchantPrefSyncStatus.get().checkMerchantPrefSync()) }
                .map {
                    when (it) {
                        is Result.Progress -> SettingsContract.PartialState.NoChange
                        is Result.Success -> SettingsContract.PartialState.SetIsMerchantPrefSync(it.value)
                        is Result.Failure -> SettingsContract.PartialState.NoChange
                    }
                },
            intent<SettingsContract.Intent.Resume>()
                .switchMap { UseCase.wrapObservable(getMerchantPreference.get().execute(FOUR_DIGIT_PIN)) }
                .map {
                    when (it) {
                        is Result.Progress -> SettingsContract.PartialState.NoChange
                        is Result.Success -> SettingsContract.PartialState.SetIsFourDigitPin(it.value.toBoolean())
                        is Result.Failure -> SettingsContract.PartialState.NoChange
                    }
                },

            // check already password set
            intent<SettingsContract.Intent.Resume>()
                .switchMap { UseCase.wrapSingle(isPasswordSet.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> SettingsContract.PartialState.NoChange
                        is Result.Success -> SettingsContract.PartialState.SetIsPasswordEnabled(it.value)
                        is Result.Failure -> SettingsContract.PartialState.NoChange
                    }
                },

            // change number click
            intent<SettingsContract.Intent.ChangeNumberClick>()
                .map {
                    emitViewEvent(SettingsContract.ViewEvent.GoToChangeNumberScreen)
                    SettingsContract.PartialState.NoChange
                },

            // check the active Language
            intent<SettingsContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(activeLanguage.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> SettingsContract.PartialState.NoChange
                        is Result.Success -> SettingsContract.PartialState.SetActiveLanguage(it.value)
                        is Result.Failure -> SettingsContract.PartialState.NoChange
                    }
                },

            // check paymentPassword enabled
            intent<SettingsContract.Intent.Resume>()
                .switchMap { getMerchantPreference.get().execute(PAYMENT_PASSWORD) }
                .map {
                    it.toBoolean()
                }
                .onErrorReturn { false }
                .map {
                    SettingsContract.PartialState.SetPaymentPasswordEnabled(it)
                },

            // setnew pin
            intent<SettingsContract.Intent.SetNewPin>()
                .map {
                    emitViewEvent(SettingsContract.ViewEvent.GoToSetNewPinScreen(it.type))
                    SettingsContract.PartialState.NoChange
                },
            // load screen
            intent<SettingsContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(Observable.just("")) }
                .map {
                    when (it) {
                        is Result.Progress -> SettingsContract.PartialState.NoChange
                        is Result.Success -> {
                            SettingsContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(SettingsContract.ViewEvent.gotoLogin)
                                    SettingsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> SettingsContract.PartialState.SetNetworkError(true)
                                else -> SettingsContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            // handle updatepassword boolean
            intent<SettingsContract.Intent.UpdatePasswordClick>()
                .map {
                    isSetPassword = it.isSetPassword
                    SettingsContract.PartialState.NoChange
                },
            // handle signout from all devices
            intent<SettingsContract.Intent.SignOutFromAllDevices>()
                .map {
                    emitViewEvent(SettingsContract.ViewEvent.OpenLogoutConfirmationDialog)
                    SettingsContract.PartialState.NoChange
                },

            intent<SettingsContract.Intent.SignOut>()
                .switchMap { UseCase.wrapCompletable(signout.get().execute(null)) }
                .map {
                    when (it) {
                        is Result.Progress -> SettingsContract.PartialState.NoChange
                        is Result.Success -> SettingsContract.PartialState.SetSignout
                        is Result.Failure -> SettingsContract.PartialState.NoChange
                    }
                },

            // handle signout confirmation
            intent<SettingsContract.Intent.SignoutConfirmationClick>()
                .switchMap {
                    UseCase.wrapCompletable(signout.get().execute(null))
                }
                .map {
                    when (it) {
                        is Result.Progress -> SettingsContract.PartialState.NoChange
                        is Result.Success -> SettingsContract.PartialState.SetSignout
                        is Result.Failure -> {
                            if (it.error is IncorrectPassword) {
                                emitViewEvent(SettingsContract.ViewEvent.ShowInvalidPassword)
                                SettingsContract.PartialState.NoChange
                            }
                            if (isAuthenticationIssue(it.error)) {
                                emitViewEvent(SettingsContract.ViewEvent.gotoLogin)
                                SettingsContract.PartialState.NoChange
                            } else if (isInternetIssue(it.error)) {
                                SettingsContract.PartialState.SetNetworkError(true)
                            } else
                                SettingsContract.PartialState.ErrorState
                        }
                    }
                },
            // handle updatepassword
            intent<SettingsContract.Intent.UpdatePasswordClick>()
                .switchMap { UseCase.wrapSingle(getActiveBusiness.get().execute().firstOrError()) }
                .map {
                    when (it) {
                        is Result.Progress -> SettingsContract.PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(
                                SettingsContract.ViewEvent.GotoResetPasswordScreen(
                                    it.value.mobile,
                                    isSetPassword
                                )
                            )
                            SettingsContract.PartialState.NoChange
                        }
                        is Result.Failure -> SettingsContract.PartialState.NoChange
                    }
                },

            // handle `show alert` intent
            intent<SettingsContract.Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<SettingsContract.PartialState> { SettingsContract.PartialState.HideAlert }
                        .startWith(SettingsContract.PartialState.ShowAlert(it.message))
                },

            // set fingerprint visible
            intent<SettingsContract.Intent.Load>()
                .take(1)
                .switchMap {
                    UseCase.wrapObservable(merchantPrefSyncStatus.get().checkFingerPrintAvailability())
                }
                .map {
                    when (it) {
                        is Result.Success -> SettingsContract.PartialState.SetFingerPrintVisible(it.value)
                        is Result.Failure -> SettingsContract.PartialState.NoChange
                        is Result.Progress -> SettingsContract.PartialState.NoChange
                    }
                },

            // handle applock click
            intent<SettingsContract.Intent.AppLockClick>()
                .map {
                    emitViewEvent(SettingsContract.ViewEvent.OpenAppLock)
                    SettingsContract.PartialState.NoChange
                },

            // handle PaymentPasswordEnable click
            intent<SettingsContract.Intent.PaymentPasswordClick>()
                .map {
                    emitViewEvent(SettingsContract.ViewEvent.GoToPaymentPasswordEnableScreen)
                    SettingsContract.PartialState.NoChange
                },

            // handle profile click
            intent<SettingsContract.Intent.ProfileClick>()
                .map {
                    emitViewEvent(SettingsContract.ViewEvent.GoToProfileScreen)
                    SettingsContract.PartialState.NoChange
                },
            // handle profile click
            intent<SettingsContract.Intent.UpdatePin>()
                .map {
                    emitViewEvent(SettingsContract.ViewEvent.ShowUpdatePinDialog(it.type))
                    SettingsContract.PartialState.NoChange
                },

            // handle languageClick
            intent<SettingsContract.Intent.AppLanguageClick>()
                .map {
                    emitViewEvent(SettingsContract.ViewEvent.GoToLanguageScreen)
                    SettingsContract.PartialState.NoChange
                },

            isPspUpiFeatureEnabled()
        )
    }

    private fun loadFingerPrintEnableStatus(): Observable<SettingsContract.PartialState> {
        return intent<SettingsContract.Intent.Load>()
            .switchMap {
                UseCase.wrapSingle(merchantPrefSyncStatus.get().checkFingerPrintEnable().firstOrError())
            }.map {
                when (it) {
                    is Result.Progress -> SettingsContract.PartialState.NoChange
                    is Result.Success -> SettingsContract.PartialState.SetFingerprintEnabled(it.value)
                    is Result.Failure -> SettingsContract.PartialState.NoChange
                }
            }
    }

    private fun setFingerprintinSharedPref(): Observable<SettingsContract.PartialState> {
        return intent<SettingsContract.Intent.SetFingerPrintEnable>()
            .switchMap {
                isFingerPrintEnabled = it.fingerprintEnable
                wrap(setFingerprintLockStatus.get().execute(isFingerPrintEnabled))
            }
            .map {
                when (it) {
                    is Result.Progress -> SettingsContract.PartialState.NoChange
                    is Result.Success -> SettingsContract.PartialState.SetFingerprintEnabled(isFingerPrintEnabled)
                    is Result.Failure -> SettingsContract.PartialState.NoChange
                }
            }
    }

    private fun checkIsFourDigitPin(): Observable<SettingsContract.PartialState> {
        return intent<SettingsContract.Intent.CheckIsFourDigit>()
            .switchMap {
                isFourDigitPinFor = it.type
                UseCase.wrapSingle(getMerchantPreference.get().execute(FOUR_DIGIT_PIN).firstOrError())
            }
            .map {
                when (it) {
                    is Result.Progress -> SettingsContract.PartialState.NoChange
                    is Result.Success -> {
                        emitViewEvent(
                            SettingsContract.ViewEvent.CheckFourDigitPinDone(
                                isFourDigitPinFor,
                                it.value.toBoolean()
                            )
                        )
                        SettingsContract.PartialState.SetIsFourDigitPin(it.value.toBoolean())
                    }
                    is Result.Failure -> {
                        if (isInternetIssue(it.error)) {
                            SettingsContract.PartialState.SetNetworkError(true)
                        } else
                            SettingsContract.PartialState.ErrorState
                    }
                }
            }
    }

    private fun syncMerchantPref(): Observable<SettingsContract.PartialState> {
        return intent<SettingsContract.Intent.SyncMerchantPref>()
            .switchMap {
                settingsClick = it.type
                UseCase.wrapCompletable(merchantPrefSyncStatus.get().execute())
            }
            .map {
                when (it) {
                    is Result.Progress -> SettingsContract.PartialState.NoChange
                    is Result.Success -> {
                        emitViewEvent(SettingsContract.ViewEvent.SyncDone(settingsClick))
                        SettingsContract.PartialState.SetIsMerchantPrefSync(true)
                    }
                    is Result.Failure -> {
                        if (isInternetIssue(it.error)) {
                            SettingsContract.PartialState.SetNetworkError(true)
                        } else
                            SettingsContract.PartialState.ErrorState
                    }
                }
            }
    }

    private fun isPspUpiFeatureEnabled(): Observable<SettingsContract.PartialState> {
        return intent<SettingsContract.Intent.Load>()
            .switchMap {
                wrap(isPspUpiFeatureEnabled.get().execute())
            }
            .filter { it is Result.Success }
            .map {
                SettingsContract.PartialState.SetIsPspUpiEnabled((it as Result.Success).value)
            }
    }

    override fun reduce(
        currentState: SettingsContract.State,
        partialState: SettingsContract.PartialState
    ): SettingsContract.State {
        return when (partialState) {
            is SettingsContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is SettingsContract.PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is SettingsContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is SettingsContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is SettingsContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is SettingsContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is SettingsContract.PartialState.NoChange -> currentState
            is SettingsContract.PartialState.SetActiveLanguage -> currentState.copy(activeLangugeStringId = partialState.activeLangugeStringId)
            is SettingsContract.PartialState.SetAppLockType -> currentState.copy(
                isAppLockActive = partialState.isAppLockActive,
                appLockType = partialState.appLockType
            )
            is SettingsContract.PartialState.SetIsPasswordEnabled -> currentState.copy(isSetPassword = partialState.isSetPassword)
            is SettingsContract.PartialState.SetPaymentPasswordEnabled -> currentState.copy(isPaymentPasswordEnabled = partialState.isPaymentPasswordEnabled)
            is SettingsContract.PartialState.SetSignout -> currentState.copy(signOut = true)
            is SettingsContract.PartialState.SetFingerPrintVisible -> currentState.copy(isFingerPrintLockVisible = partialState.isFingerPrintLockVisible)
            is SettingsContract.PartialState.SetFingerprintEnabled -> currentState.copy(isFingerPrintEnabled = partialState.isFingerPrintEnabled)
            is SettingsContract.PartialState.SetIsMerchantPrefSync -> currentState.copy(isMerchantPrefSync = partialState.isMerchantPrefSync)
            is SettingsContract.PartialState.SetIsFourDigitPin -> currentState.copy(isFourDigitPinSet = partialState.isFourDigitPinSet)
            is SettingsContract.PartialState.SetIsPspUpiEnabled -> currentState.copy(isPspUpiFeatureEnabled = partialState.isEnabled)
        }
    }
}
