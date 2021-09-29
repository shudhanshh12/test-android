package `in`.okcredit.onboarding.enterotp.v2

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.backend.contract.Authenticate
import `in`.okcredit.backend.contract.CheckMobileStatus
import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics
import `in`.okcredit.onboarding.enterotp.usecase.RequestOtp
import `in`.okcredit.onboarding.enterotp.v2.OtpContractV2.*
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
import tech.okcredit.base.network.NetworkError
import timber.log.Timber
import javax.inject.Inject

class OtpV2ViewModel @Inject constructor(
    initialState: State,
    private val args: OtpV2FragmentArgs,
    private val requestOtp: Lazy<RequestOtp>,
    private val authenticate: Lazy<Authenticate>,
    private val onboardingAnalytics: Lazy<OnboardingAnalytics>,
    private val checkMobileStatus: Lazy<CheckMobileStatus>,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    private var otpToken: OtpToken? = null

    private var flow = PropertyValue.LOGIN

    private var autoReadOtp = false

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            findFlow(),
            loadScreen(),
            verifyOtp(),
            resendOtp(),
            editMobile()
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            is PartialState.SendOtpFailure -> currentState.copy(
                otpSent = isOtpSent(),
                incorrectOtp = false,
                verificationInProgress = false,
                errorMessage = partialState.message
            )
            PartialState.SendOtpSuccess -> currentState.copy(
                otpSent = isOtpSent(),
                incorrectOtp = false,
                verificationInProgress = false,
                errorMessage = null
            )
            PartialState.Verifying -> currentState.copy(
                incorrectOtp = false,
                verificationInProgress = true,
                errorMessage = null
            )
            is PartialState.VerifyOtpFailure -> currentState.copy(
                incorrectOtp = true,
                verificationInProgress = false,
                errorMessage = partialState.message
            )
        }
    }

    private fun loadScreen() = Observable.just(Intent.Load).compose(sendOtp())

    private fun resendOtp(): Observable<PartialState>? {
        return intent<Intent.ResendOtp>()
            .doOnNext {
                onboardingAnalytics.get().trackResendOtp(flow, OnboardingAnalytics.OnboardingPropertyValue.MANUAL)
            }.compose(sendOtp())
    }

    private fun verifyOtp(): Observable<PartialState> {
        return intent<Intent.VerifyOtp>()
            .doOnNext {
                autoReadOtp = it.isAutoRead
                if (it.isAutoRead) {
                    onboardingAnalytics.get().trackOtpReceived(flow, OnboardingAnalytics.OnboardingPropertyValue.MANUAL)
                }
            }.switchMap {
                if (checkNetworkHealth.get().isConnectedToInternet()) {
                    authenticate.get().execute(Credential.Otp(otpToken!!, it.otp))
                } else {
                    Observable.just(Result.Failure(NetworkError(cause = RuntimeException("not connected to internet"))))
                }
            }.map {
                when (it) {
                    is Result.Progress -> PartialState.Verifying
                    is Result.Success -> {

                        onboardingAnalytics.get()
                            .otpVerified(flow, OnboardingAnalytics.OnboardingPropertyValue.MANUAL, autoReadOtp)

                        val isNewUser = it.value.first
                        val isAppLockEnabled = it.value.second
                        Timber.i("newlyRegisteredUser $isNewUser")

                        if (isNewUser) {
                            onboardingAnalytics.get().setRegisterUserProperty()
                            onboardingAnalytics.get()
                                .trackRegistrationSuccess(type = PropertyValue.SMS)
                            onboardingAnalytics.get().trackEnterNameScreen()
                            emitViewEvent(ViewEvent.GoToNameScreen)
                        } else {
                            if (isAppLockEnabled) {
                                onboardingAnalytics.get().trackAppLockEnabled(PropertyValue.TRUE)
                                emitViewEvent(ViewEvent.GoToAppLockScreen)
                            } else {
                                emitViewEvent(ViewEvent.GoToSyncDataScreen)
                            }
                        }
                        onboardingAnalytics.get().trackLoginSuccess(
                            type = PropertyValue.SMS,
                            flow = flow,
                            register = isNewUser.toString()
                        )
                        PartialState.NoChange
                    }
                    is Result.Failure -> {
                        when (it.error) {
                            is InvalidOtp -> {
                                onboardingAnalytics.get().trackOTPError(
                                    flow,
                                    OnboardingAnalytics.OnboardingPropertyValue.MANUAL,
                                    OnboardingAnalytics.OnboardingPropertyValue.INVALID_OTP
                                )
                                PartialState.VerifyOtpFailure(R.string.otp_incorrect)
                            }
                            is ExpiredOtp -> {
                                onboardingAnalytics.get().trackOTPError(
                                    flow,
                                    OnboardingAnalytics.OnboardingPropertyValue.MANUAL,
                                    OnboardingAnalytics.OnboardingPropertyValue.OTP_EXPIRED
                                )
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
                    UseCase.wrapSingle(requestOtp.get().execute(args.mobile))
                } else {
                    Observable.just(Result.Failure(NetworkError(cause = RuntimeException("not connected to internet"))))
                }
            }.map {
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

    private fun findFlow(): Observable<PartialState> {
        return UseCase.wrapSingle(checkMobileStatus.get().execute(args.mobile))
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        if (it.value) {
                            flow = PropertyValue.LOGIN
                            onboardingAnalytics.get()
                                .trackLoginStarted(OnboardingAnalytics.OnboardingPropertyValue.MANUAL)
                        } else {
                            flow = PropertyValue.REGISTER
                            onboardingAnalytics.get()
                                .trackRegistrationStarted(OnboardingAnalytics.OnboardingPropertyValue.MANUAL)
                        }
                        PartialState.NoChange
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun editMobile(): Observable<PartialState> {
        return intent<Intent.EditMobile>()
            .map {
                onboardingAnalytics.get().trackEditMobile(flow, OnboardingAnalytics.OnboardingPropertyValue.MANUAL)
                emitViewEvent(ViewEvent.GoToMobileScreen)
                PartialState.NoChange
            }
    }

    private fun isOtpSent() = otpToken != null
}
