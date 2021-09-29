package tech.okcredit.applock.enterPin

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.auth.IncorrectPassword
import tech.okcredit.android.auth.usecases.VerifyPassword
import tech.okcredit.applock.changePin.ChangeSecurityPinFragment.Companion.Source
import tech.okcredit.applock.usecase.GetMerchantFingerprintPreference
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.contract.MerchantPrefSyncStatus
import javax.inject.Inject

class EnterPinViewModel @Inject constructor(
    initialState: Lazy<EnterPinContract.State>,
    @ViewModelParam(Source) val source: String,
    private val verifyPassword: Lazy<VerifyPassword>,
    private val getMerchantFingerprintPreference: Lazy<GetMerchantFingerprintPreference>,
    private val merchantPrefSyncStatus: Lazy<MerchantPrefSyncStatus>,
    private val checkInternetAvailable: Lazy<CheckNetworkHealth>,
) : BaseViewModel<EnterPinContract.State, EnterPinContract.PartialState, EnterPinContract.ViewEvent>(
    initialState.get()
) {
    private lateinit var pinValue: String
    private var isFingerprintEnrolledInDevice = false
    private var isFingerprintEnabled = false

    override fun handle(): Observable<out UiState.Partial<EnterPinContract.State>> {
        return Observable.mergeArray(
            checkFingerPrintEnableInDevice(),
            updateSource(),
            setPin(),
            verifyPin(),
            checkInputMode(),
            intent<EnterPinContract.Intent.ForgotPin>()
                .map {
                    if (checkInternetAvailable.get().isConnectedToInternet()) {
                        emitViewEvent(EnterPinContract.ViewEvent.GoToSetPinScreen)
                    } else {
                        emitViewEvent(EnterPinContract.ViewEvent.InternetError)
                    }
                    EnterPinContract.PartialState.NoChange
                },
            intent<EnterPinContract.Intent.Load>()
                .switchMap {
                    UseCase.wrapSingle(
                        getMerchantFingerprintPreference.get().execute().firstOrError()
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> EnterPinContract.PartialState.NoChange
                        is Result.Success -> {
                            isFingerprintEnabled = it.value
                            pushIntent(EnterPinContract.Intent.CheckInputMode)
                            EnterPinContract.PartialState.SetFingerprintEnabled(it.value)
                        }
                        is Result.Failure -> {
                            pushIntent(EnterPinContract.Intent.CheckInputMode)
                            EnterPinContract.PartialState.NoChange
                        }
                    }
                }
        )
    }

    private fun checkInputMode() = intent<EnterPinContract.Intent.CheckInputMode>()
        .map {
            emitViewEvent(EnterPinContract.ViewEvent.ShowInputMode(isFingerprintEnrolledInDevice && isFingerprintEnabled))
            EnterPinContract.PartialState.NoChange
        }

    private fun updateSource(): Observable<EnterPinContract.PartialState> {
        return intent<EnterPinContract.Intent.Load>()
            .map {
                EnterPinContract.PartialState.SetSource(source)
            }
    }

    private fun checkFingerPrintEnableInDevice(): Observable<EnterPinContract.PartialState> {
        // set fingerprint visible
        return intent<EnterPinContract.Intent.Load>()
            .switchMap {
                UseCase.wrapObservable(merchantPrefSyncStatus.get().checkFingerPrintAvailability())
            }
            .map {
                when (it) {
                    is Result.Success -> {
                        isFingerprintEnrolledInDevice = it.value
                        EnterPinContract.PartialState.SetFingerEnrolled(it.value)
                    }
                    is Result.Failure -> EnterPinContract.PartialState.NoChange
                    is Result.Progress -> EnterPinContract.PartialState.NoChange
                }
            }
    }

    private fun setPin(): Observable<EnterPinContract.PartialState> {
        return intent<EnterPinContract.Intent.VerifyPin>()
            .map {
                pinValue = it.pin
                EnterPinContract.PartialState.SetPin(pinValue)
            }
    }

    private fun verifyPin(): Observable<EnterPinContract.PartialState> {
        return intent<EnterPinContract.Intent.VerifyPin>()
            .switchMap {
                pinValue = it.pin
                UseCase.wrapCompletable(verifyPassword.get().execute(it.pin))
            }
            .map {
                when (it) {
                    is Result.Progress -> EnterPinContract.PartialState.NoChange
                    is Result.Success -> {
                        emitViewEvent(EnterPinContract.ViewEvent.Authenticated(pinValue))
                        EnterPinContract.PartialState.NoChange
                    }
                    is Result.Failure -> {
                        if (it.error is IncorrectPassword) {
                            EnterPinContract.PartialState.SetIncorrectPin(true)
                        } else if (isAuthenticationIssue(it.error)) {
                            emitViewEvent(EnterPinContract.ViewEvent.AuthError)
                            EnterPinContract.PartialState.NoChange
                        } else if (isInternetIssue(it.error)) {
                            emitViewEvent(EnterPinContract.ViewEvent.InternetError)
                            EnterPinContract.PartialState.NoChange
                        } else {
                            EnterPinContract.PartialState.NoChange
                        }
                    }
                }
            }
    }

    override fun reduce(
        currentState: EnterPinContract.State,
        partialState: EnterPinContract.PartialState
    ): EnterPinContract.State {
        return when (partialState) {
            is EnterPinContract.PartialState.NoChange -> currentState
            is EnterPinContract.PartialState.SetIncorrectPin -> currentState.copy(incorrectPin = partialState.incorrectPin)
            is EnterPinContract.PartialState.SetPin -> currentState.copy(pin = pinValue)
            is EnterPinContract.PartialState.SetFingerprintEnabled -> currentState.copy(isFingerPrintEnabled = partialState.enabledOrNot)
            is EnterPinContract.PartialState.SetFingerEnrolled -> currentState.copy(isFingerprintEnrolledInDevice = partialState.isFingerprintEnrolledInDevice)
            is EnterPinContract.PartialState.SetSource -> currentState.copy(source = source)
        }
    }
}
