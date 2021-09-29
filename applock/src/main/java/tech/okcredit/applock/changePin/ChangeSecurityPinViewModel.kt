package tech.okcredit.applock.changePin

import `in`.okcredit.backend.contract.Authenticate
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.onboarding.enterotp.usecase.RequestOtp
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import tech.okcredit.android.auth.Credential
import tech.okcredit.android.auth.ExpiredOtp
import tech.okcredit.android.auth.InvalidOtp
import tech.okcredit.android.auth.OtpToken
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.applock.AppLockActivityV2.Companion.ENTRY
import tech.okcredit.applock.R
import tech.okcredit.applock.changePin.ChangeSecurityPinContract.*
import tech.okcredit.applock.changePin.ChangeSecurityPinFragment.Companion.Source
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.network.NetworkError
import javax.inject.Inject

class ChangeSecurityPinViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam(Source) val source: String,
    @ViewModelParam(ENTRY) val entry: String,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val requestOtp: Lazy<RequestOtp>,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val authenticate: Lazy<Authenticate>,
    private val isPasswordSet: Lazy<IsPasswordSet>
) : BaseViewModel<State, PartialState, ViewEvent>(initialState.get()) {

    private var mobile: String = ""
    private var otpToken: OtpToken? = null

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.Load>()
                .take(1)
                .switchMap {
                    UseCase.wrapSingle(getActiveBusiness.get().execute().firstOrError())
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.SendOtp(it.value.mobile))
                            mobile = it.value.mobile
                            PartialState.SetMobilenumber(it.value.mobile)
                        }
                        is Result.Failure -> {
                            if (it.error is NetworkError) {
                                emitViewEvent(ViewEvent.Toast(R.string.err_network))
                            } else {
                                emitViewEvent(ViewEvent.Toast(R.string.err_default))
                            }
                            PartialState.NoChange
                        }
                    }
                },
            intent<Intent.Load>()
                .switchMap { UseCase.wrapSingle(isPasswordSet.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.SetUpdatePassword(it.value)
                        is Result.Failure -> PartialState.NoChange
                    }
                },
            setSource(),
            intent<Intent.SendOtpClick>()
                .compose(sendOtp()),
            intent<Intent.ResendOtp>()
                .compose(sendOtp()),
            verifyOtp()
        )
    }

    private fun setSource(): Observable<PartialState> {
        return intent<Intent.Load>()
            .take(1)
            .map {
                PartialState.SetSourceAndEntry(source, entry)
            }
    }

    private fun verifyOtp(): Observable<PartialState> {
        return intent<Intent.VerifyOtp>()
            .switchMap {
                if (checkNetworkHealth.get().isConnectedToInternet()) {
                    authenticate.get().execute(Credential.Otp(otpToken!!, it.otpString))
                } else {
                    Observable.just(Result.Failure(NetworkError(cause = RuntimeException("not connected to internet"))))
                }
            }.map {
                when (it) {
                    is Result.Progress -> PartialState.Verifying
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.GoToSetPinScreen)
                        PartialState.SendOtpSuccess
                    }
                    is Result.Failure -> {
                        when (it.error) {
                            is InvalidOtp -> {
                                PartialState.VerifyOtpFailure(R.string.otp_incorrect)
                            }
                            is ExpiredOtp -> {
                                PartialState.VerifyOtpFailure(R.string.otp_expired)
                            }
                            is NetworkError -> {
                                emitViewEvent(ViewEvent.VerifyNetworkError(R.string.no_internet_msg))
                                PartialState.NoChange
                            }
                            else -> {
                                emitViewEvent(ViewEvent.VerifyNetworkError(R.string.otp_verification_failure))
                                PartialState.NoChange
                            }
                        }
                    }
                }
            }
    }

    private fun sendOtp(): ObservableTransformer<Intent, PartialState> {
        return ObservableTransformer<Intent, PartialState> { upstream ->
            upstream.switchMap {
                if (checkNetworkHealth.get().isConnectedToInternet()) {
                    UseCase.wrapSingle(requestOtp.get().execute(mobile))
                } else {
                    Observable.just(Result.Failure(NetworkError(cause = RuntimeException("not connected to internet"))))
                }
            }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.SendOtpSuccess)
                            otpToken = it.value
                            PartialState.SendOtpSuccess
                        }
                        is Result.Failure -> {
                            if (it.error is NetworkError) {
                                if (!isOtpSent()) {
                                    emitViewEvent(ViewEvent.SendOtpError(R.string.send_otp_failure_no_internet))
                                } else {
                                    emitViewEvent(ViewEvent.Toast(R.string.send_otp_failure_no_internet))
                                }
                                PartialState.SendOtpFailure(R.string.send_otp_failure_no_internet)
                            } else {
                                if (!isOtpSent()) {
                                    emitViewEvent(ViewEvent.SendOtpError(R.string.send_otp_failure))
                                } else {
                                    emitViewEvent(ViewEvent.Toast(R.string.send_otp_failure))
                                }
                                PartialState.SendOtpFailure(R.string.send_otp_failure)
                            }
                        }
                    }
                }
        }
    }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.SetMobilenumber -> currentState.copy(mobile = partialState.mobile)
            is PartialState.SendOtpFailure -> currentState.copy(
                otpSent = isOtpSent(),
                incorrectOtp = false,
                verificationInProgress = false,
                errorMessage = partialState.message
            )
            is PartialState.SendOtpSuccess -> currentState.copy(
                otpSent = isOtpSent(),
                incorrectOtp = false,
                verificationInProgress = false,
                errorMessage = null
            )
            is PartialState.Verifying -> currentState.copy(
                incorrectOtp = false,
                verificationInProgress = true,
                errorMessage = null
            )
            is PartialState.VerifyOtpFailure -> currentState.copy(
                incorrectOtp = true,
                verificationInProgress = false,
                errorMessage = partialState.message
            )
            is PartialState.SetUpdatePassword -> currentState.copy(isUpdatePassword = partialState.isUpdatePassword)

            is PartialState.SetSourceAndEntry -> currentState.copy(source = source, entry = entry)
        }
    }

    private fun isOtpSent() = otpToken != null
}
