package `in`.okcredit.onboarding.otp_verification

import `in`.okcredit.backend.contract.Authenticate
import `in`.okcredit.onboarding.enterotp.usecase.RequestOtp
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.Credential
import tech.okcredit.android.auth.ExpiredOtp
import tech.okcredit.android.auth.InvalidOtp
import tech.okcredit.android.auth.OtpToken
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OtpVerificationViewModel @Inject constructor(
    initialState: OtpVerificationContract.State,
    private val authService: AuthService,
    @ViewModelParam("flag") val flag: Int,
    private val checkNetworkHealth: CheckNetworkHealth,
    private val requestOtp: RequestOtp,
    private val authenticate: Authenticate,
    private val navigator: OtpVerificationContract.Navigator
) : BasePresenter<OtpVerificationContract.State, OtpVerificationContract.PartialState>(initialState) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private val resendOtpVisibilityPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private val sendOtpPublishSubject: PublishSubject<Unit> = PublishSubject.create()

    private var otp: OtpToken? = null
    private var isSendSmsOnLoadSuccess = false
    private var isAutoReadSms = true
    private var mobile: String = ""

    override fun handle(): Observable<UiState.Partial<OtpVerificationContract.State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<OtpVerificationContract.Intent.Load>()
                .switchMap { checkNetworkHealth.execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        reload.onNext(Unit)
                        OtpVerificationContract.PartialState.ClearNetworkError
                    } else {
                        OtpVerificationContract.PartialState.NoChange
                    }
                },

            intent<OtpVerificationContract.Intent.Load>()
                .map {
                    mobile = authService.getMobile()!!

                    if (!isSendSmsOnLoadSuccess) {
                        sendOtpPublishSubject.onNext(Unit)
                    }
                    OtpVerificationContract.PartialState.SetMobile(mobile)
                },

            intent<OtpVerificationContract.Intent.Load>()
                .map {
                    OtpVerificationContract.PartialState.ClearOtpError
                },

            intent<OtpVerificationContract.Intent.Load>()
                .map {
                    OtpVerificationContract.PartialState.SetFlag(flag)
                },

            sendOtpPublishSubject
                .switchMap {
                    UseCase.wrapSingle(requestOtp.execute(mobile))
                }
                .map {
                    when (it) {
                        is Result.Progress -> OtpVerificationContract.PartialState.SetSendOtpLoadingStatus(true)
                        is Result.Success -> {
                            resendOtpVisibilityPublishSubject.onNext(Unit)
                            isSendSmsOnLoadSuccess = true
                            otp = it.value
                            OtpVerificationContract.PartialState.SetSendOtpLoadingStatus(false)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    OtpVerificationContract.PartialState.SetSendOtpLoadingStatus(false)
                                }
                                isInternetIssue(it.error) -> OtpVerificationContract.PartialState.SetNetworkErrorWithRetry(
                                    true
                                )
                                else -> OtpVerificationContract.PartialState.NoChange
                            }
                        }
                    }
                },

            intent<OtpVerificationContract.Intent.VerifyOtp>()
                .switchMap {
                    isAutoReadSms = it.isAutoRead
                    authenticate.execute(Credential.Otp(otp!!, it.otp))
                }
                .map {
                    when (it) {
                        is Result.Progress -> OtpVerificationContract.PartialState.SetInCorrectOtpStatus(false)
                        is Result.Success -> {
                            navigator.goBackWithSuccessResult()
                            OtpVerificationContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                it.error is InvalidOtp -> {
                                    OtpVerificationContract.PartialState.SetInCorrectOtpStatus(true)
                                }
                                it.error is ExpiredOtp -> {
                                    OtpVerificationContract.PartialState.SetInCorrectOtpStatus(true)
                                }
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    OtpVerificationContract.PartialState.HideLoading
                                }
                                isInternetIssue(it.error) -> OtpVerificationContract.PartialState.SetNetworkError(true)
                                else -> OtpVerificationContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            // handle `show alert` intent
            intent<OtpVerificationContract.Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<OtpVerificationContract.PartialState> { OtpVerificationContract.PartialState.HideAlert }
                        .startWith(OtpVerificationContract.PartialState.ShowAlert(it.message))
                },

            intent<OtpVerificationContract.Intent.ResendOtp>()
                .map {
                    sendOtpPublishSubject.onNext(Unit)
                    OtpVerificationContract.PartialState.ReSetDefaultOtpView
                },

            resendOtpVisibilityPublishSubject
                .map {
                    OtpVerificationContract.PartialState.SetResendOtpVisibility(false)
                },

            resendOtpVisibilityPublishSubject
                .switchMap { Observable.timer(30, TimeUnit.SECONDS) }
                .map {
                    OtpVerificationContract.PartialState.SetResendOtpVisibility(true)
                }
        )
    }

    override fun reduce(
        currentState: OtpVerificationContract.State,
        partialState: OtpVerificationContract.PartialState
    ): OtpVerificationContract.State {
        return when (partialState) {
            is OtpVerificationContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is OtpVerificationContract.PartialState.HideLoading -> currentState.copy(isLoading = false)
            is OtpVerificationContract.PartialState.ErrorState -> currentState.copy(
                isLoading = false,
                error = true,
                sendOtpLoader = false
            )
            is OtpVerificationContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is OtpVerificationContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is OtpVerificationContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false,
                sendOtpLoader = false
            )
            is OtpVerificationContract.PartialState.SetNetworkErrorWithRetry -> currentState.copy(
                networkErrorWithRetry = partialState.networkErrorWithRetry,
                isLoading = false,
                sendOtpLoader = false
            )
            is OtpVerificationContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is OtpVerificationContract.PartialState.SetMobile -> currentState.copy(mobile = partialState.mobile)
            is OtpVerificationContract.PartialState.SetFlag -> currentState.copy(flag = partialState.flag)
            is OtpVerificationContract.PartialState.SetInCorrectOtpStatus -> currentState.copy(
                otpError = partialState.status,
                isLoading = !partialState.status
            )
            is OtpVerificationContract.PartialState.SetResendOtpVisibility -> currentState.copy(isShowResendOtp = partialState.status)
            is OtpVerificationContract.PartialState.SetSendOtpLoadingStatus -> currentState.copy(
                sendOtpLoader = partialState.status,
                networkErrorWithRetry = false
            )
            is OtpVerificationContract.PartialState.ReSetDefaultOtpView -> currentState.copy(
                sendOtpLoader = true,
                otpError = false
            )
            is OtpVerificationContract.PartialState.ClearOtpError -> currentState.copy(otpError = false)
            is OtpVerificationContract.PartialState.NoChange -> currentState
        }
    }
}
