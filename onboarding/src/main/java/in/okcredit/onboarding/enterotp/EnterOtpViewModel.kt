package `in`.okcredit.onboarding.enterotp

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.*
import `in`.okcredit.merchant.contract.BusinessErrors
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.AUTHENTICATION_ISSUE
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.AUTO
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.DEFAULT_SOURCE
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.INVALID_OTP
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.MANUAL
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.NO_INTERNET
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.NO_INTERNET_SNACKBAR_SOURCE
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.OTP_EXPIRED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.TOO_MANY_REQUESTS
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.UNKNOWN_ERROR
import `in`.okcredit.onboarding.contract.OnboardingConstants
import `in`.okcredit.onboarding.enterotp.usecase.*
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.Credential
import tech.okcredit.android.auth.ExpiredOtp
import tech.okcredit.android.auth.InvalidOtp
import tech.okcredit.android.auth.OtpToken
import tech.okcredit.android.auth.TooManyRequests
import tech.okcredit.android.auth.server.AuthApiClient.Companion.OTP_FLOW_EXPIRY_TIME
import tech.okcredit.android.auth.server.AuthApiClient.Companion.OTP_RETRY_TIME
import tech.okcredit.android.auth.server.AuthApiClient.RequestOtpMedium
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.feature_help.contract.GetSupportNumber
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EnterOtpViewModel @Inject constructor(
    initialState: EnterOtpContract.State,
    @ViewModelParam("flag") val flag: Int,
    @ViewModelParam("arg_sign_out_from_all_devices") val signOutFromAllDevices: Boolean,
    @ViewModelParam(OnboardingConstants.ARG_GOOGLE_AUTO_READ_MOBILE_NUMBER) val isGooglePopupSelected: Boolean,
    @ViewModelParam(OnboardingConstants.ARG_MOBILE) var mobile: String,
    private val authService: Lazy<AuthService>,
    private val checkMobileStatus: Lazy<CheckMobileStatus>,
    private val authenticate: Lazy<Authenticate>,
    private val authenticateOtp: Lazy<AuthenticateOtp>,
    private val authenticateNewOtp: Lazy<AuthenticateNewOtp>,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val requestOtp: Lazy<RequestOtp>,
    private val resendOtp: Lazy<ResendOtp>,
    private val fetchFallbackOptionsOtp: Lazy<FetchFallbackOptionsOtp>,
    private val whatsAppLoginHelper: Lazy<WhatsAppLoginHelper>,
    private val updateIndividualMobile: Lazy<UpdateIndividualMobile>,
    private val signout: Lazy<Signout>,
    private val tracker: Lazy<Tracker>,
    private val onboardingAnalytics: Lazy<OnboardingAnalytics>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val appLockManager: Lazy<AppLockManager>,
    private val getSupportNumber: Lazy<GetSupportNumber>,
) : BaseViewModel<EnterOtpContract.State, EnterOtpContract.PartialState, EnterOtpContract.ViewEvent>(
    initialState
) {

    private var merchantId: String = ""
    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private val updateMerchantSubject: PublishSubject<Unit> = PublishSubject.create()
    private val signOutCurrent: PublishSubject<Unit> = PublishSubject.create()
    private val signOutAllSubject: PublishSubject<Unit> = PublishSubject.create()
    private val successfulGratificationTimerSubject: PublishSubject<Unit> = PublishSubject.create()
    private val canNavigateToEnterNewNumberScreen: PublishSubject<Boolean> = PublishSubject.create()
    private val sendOtpPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private val resendOtpPublishSubject: PublishSubject<RequestOtpMedium> = PublishSubject.create()
    private val fetchRetryOptionsPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private val verifyOtpPublishSubject: PublishSubject<String> = PublishSubject.create()
    private val setupLockLoginFlowSubject: PublishSubject<Boolean> =
        PublishSubject.create() // true->existing user enabled app lock
    private val normalRegistrationOrLogin: PublishSubject<String> = PublishSubject.create()

    private var otp: OtpToken? = null
    private var isAutoReadSms = true
    private var verifiedSuccessfully = false
    private var flow = PropertyValue.LOGIN
    private var type = ""
    private var otpResendSource = DEFAULT_SOURCE

    override fun handle(): Observable<UiState.Partial<EnterOtpContract.State>> {

        return mergeArray(
            // hide network error when network becomes available
            intent<EnterOtpContract.Intent.Load>()
                .doOnNext {
                    type =
                        if (isGooglePopupSelected) OnboardingAnalytics.OnboardingPropertyValue.GOOGLE_POPUP else OnboardingAnalytics.OnboardingPropertyValue.MANUAL
                }
                .switchMap { checkNetworkHealth.get().execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        reload.onNext(Unit)
                        EnterOtpContract.PartialState.ClearNetworkError
                    } else {
                        EnterOtpContract.PartialState.NoChange
                    }
                },

            intent<EnterOtpContract.Intent.Load>()
                .map {
                    EnterOtpContract.PartialState.Flag(flag)
                },

            intent<EnterOtpContract.Intent.Load>()
                .map {
                    EnterOtpContract.PartialState.SetGooglePopUp(isGooglePopupSelected)
                },

            UseCase.wrapSingle(checkMobileStatus.get().execute(mobile))
                .map
                {
                    when (it) {
                        is Result.Progress -> EnterOtpContract.PartialState.NoChange
                        is Result.Success -> {
                            if (!(flag == OnboardingConstants.FLAG_VERIFY_NEW_NUMBER_AND_CHANGE_NUMBER || flag == OnboardingConstants.FLAG_NUMBER_CHANGE)) {
                                if (it.value) {
                                    flow = PropertyValue.LOGIN
                                    onboardingAnalytics.get().trackLoginStarted(type)
                                } else {
                                    flow = PropertyValue.REGISTER
                                    onboardingAnalytics.get().trackRegistrationStarted(type)
                                }
                            }
                            EnterOtpContract.PartialState.MobileStatus(it.value)
                        }
                        is Result.Failure -> {
                            EnterOtpContract.PartialState.NoChange
                        }
                    }
                },

            intent<EnterOtpContract.Intent.Load>()
                .map {
                    if (flag == OnboardingConstants.FLAG_FORGOT_PATTERN) {
                        if (authService.get().getMobile() != null) {
                            mobile = authService.get().getMobile()!!
                        }
                    }

                    if (otp == null) {
                        sendOtpPublishSubject.onNext(Unit)
                    }
                    EnterOtpContract.PartialState.SetMobile(mobile)
                },

            intent<EnterOtpContract.Intent.Load>()
                .map {
                    fetchRetryOptionsPublishSubject.onNext(Unit)
                    EnterOtpContract.PartialState.NoChange
                },

            intent<EnterOtpContract.Intent.Load>()
                .map {
                    EnterOtpContract.PartialState.ClearOtpError
                },

            intent<EnterOtpContract.Intent.SendOtpViaSms>()
                .map {
                    otpResendSource = DEFAULT_SOURCE
                    onboardingAnalytics.get().trackResendOtp(flow, type, RequestOtpMedium.SMS, otpResendSource)
                    resendOtpPublishSubject.onNext(RequestOtpMedium.SMS)
                    fetchRetryOptionsPublishSubject.onNext(Unit)
                    EnterOtpContract.PartialState.LastRetryMedium(RequestOtpMedium.SMS)
                },
            intent<EnterOtpContract.Intent.SendOtpViaWhatsApp>()
                .map {
                    otpResendSource = DEFAULT_SOURCE
                    onboardingAnalytics.get().trackResendOtp(flow, type, RequestOtpMedium.WHATSAPP, otpResendSource)
                    resendOtpPublishSubject.onNext(RequestOtpMedium.WHATSAPP)
                    fetchRetryOptionsPublishSubject.onNext(Unit)
                    EnterOtpContract.PartialState.LastRetryMedium(RequestOtpMedium.WHATSAPP)
                },

            intent<EnterOtpContract.Intent.SendOtpViaIvr>()
                .map {
                    otpResendSource = DEFAULT_SOURCE
                    onboardingAnalytics.get().trackResendOtp(flow, type, RequestOtpMedium.CALL, otpResendSource)
                    resendOtpPublishSubject.onNext(RequestOtpMedium.CALL)
                    fetchRetryOptionsPublishSubject.onNext(Unit)
                    EnterOtpContract.PartialState.LastRetryMedium(RequestOtpMedium.CALL)
                },

            intent<EnterOtpContract.Intent.WaitingTimeFinished>()
                .map {
                    EnterOtpContract.PartialState.ShowFallbackOptions
                },

            intent<EnterOtpContract.Intent.EndOtpProcess>()
                .map {
                    emitViewEvent(EnterOtpContract.ViewEvent.ResetCompleteOtpFlow)
                    EnterOtpContract.PartialState.NoChange
                },

            fetchRetryOptionsPublishSubject
                .switchMap {
                    if (mobile.isEmpty()) {
                        val savedMobileNumber = authService.get().getMobile()
                        if (!savedMobileNumber.isNullOrEmpty()) {
                            mobile = savedMobileNumber
                        }
                    }

                    UseCase.wrapSingle(fetchFallbackOptionsOtp.get().execute(mobile))
                }.map {
                    when (it) {
                        is Result.Progress -> EnterOtpContract.PartialState.NoChange
                        is Result.Success -> EnterOtpContract.PartialState.FallbackOptions(it.value)
                        is Result.Failure -> EnterOtpContract.PartialState.NoChange
                    }
                },

            resendOtpPublishSubject
                .switchMap { requestOtpMedium ->
                    UseCase.wrapSingle(resendOtp.get().execute(mobile, requestOtpMedium, otp?.id ?: ""))
                }
                .map {
                    when (it) {
                        is Result.Progress -> EnterOtpContract.PartialState.SetSendOtpLoadingStatus(true)
                        is Result.Success -> {
                            onboardingAnalytics.get()
                                .trackResendOtpSuccess(flow, type, getCurrentState().lastRetryMedium, otpResendSource)

                            // Start timer
                            emitViewEvent(
                                EnterOtpContract.ViewEvent.StartTimer(
                                    (it.value.retry_option_timeout ?: OTP_RETRY_TIME).toLong()
                                )
                            )

                            emitViewEvent(EnterOtpContract.ViewEvent.SendOtpSuccess(getCurrentState().lastRetryMedium))

                            // Hide fallback options
                            EnterOtpContract.PartialState.HideFallbackOptions
                        }
                        is Result.Failure -> {
                            when {
                                it.error is TooManyRequests -> {
                                    emitViewEvent(EnterOtpContract.ViewEvent.TooManyRequests)
                                    emitViewEvent(EnterOtpContract.ViewEvent.ResetCompleteOtpFlow)
                                    EnterOtpContract.PartialState.SetInCorrectOtpStatus(true)
                                }
                                it.error is ExpiredOtp -> {
                                    emitViewEvent(EnterOtpContract.ViewEvent.ResetCompleteOtpFlow)
                                    EnterOtpContract.PartialState.SetInCorrectOtpStatus(true)
                                }
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(EnterOtpContract.ViewEvent.GoToLogin)
                                    EnterOtpContract.PartialState.SetSendOtpLoadingStatus(false)
                                }
                                isInternetIssue(it.error) ->
                                    EnterOtpContract.PartialState.SetNetworkErrorWithRetry(true)
                                else -> EnterOtpContract.PartialState.NoChange
                            }
                        }
                    }
                },

            sendOtpPublishSubject
                .switchMap {
                    UseCase.wrapSingle(requestOtp.get().execute(mobile))
                }
                .map {
                    when (it) {
                        is Result.Progress -> EnterOtpContract.PartialState.SetSendOtpLoadingStatus(true)
                        is Result.Success -> {
                            onboardingAnalytics.get().trackRequestOTPSuccessful(type, flow)

                            otp = it.value

                            // Start timer
                            emitViewEvent(
                                EnterOtpContract.ViewEvent.StartTimer(
                                    (otp?.fallbackOptionsShowTime ?: OTP_RETRY_TIME).toLong()
                                )
                            )

                            // Start overall process timer
                            pushIntent(
                                EnterOtpContract.Intent.StartOtpFlowTimer(
                                    (
                                        otp?.overallExpiryTime
                                            ?: OTP_FLOW_EXPIRY_TIME
                                        ).toLong()
                                )
                            )

                            emitViewEvent(EnterOtpContract.ViewEvent.SendOtpSuccess(getCurrentState().lastRetryMedium))

                            // Hide fallback options
                            EnterOtpContract.PartialState.HideFallbackOptions
                        }
                        is Result.Failure -> {
                            when {
                                it.error is TooManyRequests -> {
                                    emitViewEvent(EnterOtpContract.ViewEvent.TooManyRequests)
                                    emitViewEvent(EnterOtpContract.ViewEvent.ResetCompleteOtpFlow)
                                    EnterOtpContract.PartialState.SetInCorrectOtpStatus(true)
                                }
                                it.error is ExpiredOtp -> {
                                    emitViewEvent(EnterOtpContract.ViewEvent.ResetCompleteOtpFlow)
                                    EnterOtpContract.PartialState.SetInCorrectOtpStatus(true)
                                }
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(EnterOtpContract.ViewEvent.GoToLogin)
                                    EnterOtpContract.PartialState.SetSendOtpLoadingStatus(false)
                                }
                                isInternetIssue(it.error) -> EnterOtpContract.PartialState.SetNetworkErrorWithRetry(true)
                                else -> EnterOtpContract.PartialState.NoChange
                            }
                        }
                    }
                },

            intent<EnterOtpContract.Intent.StartOtpFlowTimer>()
                .delay {
                    Observable.timer(it.overallProcessTime, TimeUnit.SECONDS)
                }
                .map {
                    emitViewEvent(EnterOtpContract.ViewEvent.ResetCompleteOtpFlow)
                    EnterOtpContract.PartialState.NoChange
                },

            intent<EnterOtpContract.Intent.VerifyOtp>()
                .map {
                    isAutoReadSms = it.isAutoRead
                    onboardingAnalytics.get()
                        .trackOtpEntered(flow, if (isAutoReadSms) AUTO else MANUAL, getCurrentState().lastRetryMedium)
                    if (isAutoReadSms) {
                        onboardingAnalytics.get().trackOtpReceived(flow, type)
                    }
                    if (otp == null) {
                        sendOtpPublishSubject.onNext(Unit)
                    } else {
                        verifyOtpPublishSubject.onNext(it.otp)
                    }
                    EnterOtpContract.PartialState.NoChange
                },

            verifyOtpPublishSubject
                .switchMap {
                    when (flag) {
                        OnboardingConstants.FLAG_NUMBER_CHANGE -> authenticateOtp.get()
                            .execute(Credential.Otp(otp!!, it))
                        OnboardingConstants.FLAG_VERIFY_NEW_NUMBER_AND_CHANGE_NUMBER -> authenticateNewOtp.get()
                            .execute(
                                Credential.Otp(
                                    otp!!,
                                    it
                                )
                            )
                        else -> {
                            normalRegistrationOrLogin.onNext(it)
                            UseCase.wrapObservable(just(false))
                        }
                    }
                }
                .map {
                    when (it) {
                        is Result.Progress -> EnterOtpContract.PartialState.SetInCorrectOtpStatus(false)
                        is Result.Success -> {

                            when (flag) {

                                OnboardingConstants.FLAG_NUMBER_CHANGE -> {
                                    onboardingAnalytics.get().trackNumberChangeOtpVerified(
                                        OnboardingAnalytics.OnboardingPropertyKey.NUMBER_CHANGE,
                                        isAutoReadSms.toString().capitalize(),
                                        OnboardingAnalytics.OnboardingPropertyValue.OLD
                                    )
                                    emitViewEvent(EnterOtpContract.ViewEvent.GoToChangeNumberScreen)
                                }

                                OnboardingConstants.FLAG_VERIFY_NEW_NUMBER_AND_CHANGE_NUMBER -> {
                                    onboardingAnalytics.get().trackNumberChangeOtpVerified(
                                        OnboardingAnalytics.OnboardingPropertyKey.NUMBER_CHANGE,
                                        isAutoReadSms.toString().capitalize(),
                                        OnboardingAnalytics.OnboardingPropertyValue.NEW
                                    )
                                    updateMerchantSubject.onNext(Unit)
                                }

                                OnboardingConstants.FLAG_FORGOT_PATTERN -> {
                                    appLockManager.get().clearAppLockData()
                                    emitViewEvent(EnterOtpContract.ViewEvent.GoToHome)
                                }

                                OnboardingConstants.FLAG_DEFAULT -> {
                                    onboardingAnalytics.get().trackLoginOtpVerified(
                                        flow,
                                        if (isAutoReadSms) AUTO else MANUAL,
                                        getCurrentState().lastRetryMedium
                                    )
                                }
                            }

                            EnterOtpContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                it.error is TooManyRequests -> {
                                    onboardingAnalytics.get()
                                        .trackOTPError(flow, type, TOO_MANY_REQUESTS, getCurrentState().lastRetryMedium)
                                    emitViewEvent(EnterOtpContract.ViewEvent.TooManyRequests)
                                    emitViewEvent(EnterOtpContract.ViewEvent.ResetCompleteOtpFlow)
                                    EnterOtpContract.PartialState.SetInCorrectOtpStatus(true)
                                }
                                it.error is InvalidOtp -> {
                                    onboardingAnalytics.get()
                                        .trackOTPError(flow, type, INVALID_OTP, getCurrentState().lastRetryMedium)
                                    EnterOtpContract.PartialState.SetInCorrectOtpStatus(true)
                                }
                                it.error is ExpiredOtp -> {
                                    onboardingAnalytics.get()
                                        .trackOTPError(flow, type, OTP_EXPIRED, getCurrentState().lastRetryMedium)
                                    EnterOtpContract.PartialState.SetInCorrectOtpStatus(true)
                                }
                                isAuthenticationIssue(it.error) -> {
                                    onboardingAnalytics.get()
                                        .trackOTPError(
                                            flow,
                                            type,
                                            AUTHENTICATION_ISSUE,
                                            getCurrentState().lastRetryMedium
                                        )
                                    emitViewEvent(EnterOtpContract.ViewEvent.GoToLogin)
                                    EnterOtpContract.PartialState.HideLoading
                                }
                                isInternetIssue(it.error) -> {
                                    onboardingAnalytics.get()
                                        .trackOTPError(flow, type, NO_INTERNET, getCurrentState().lastRetryMedium)
                                    EnterOtpContract.PartialState.SetNetworkError(true)
                                }
                                else -> {
                                    onboardingAnalytics.get()
                                        .trackOTPError(flow, type, UNKNOWN_ERROR, getCurrentState().lastRetryMedium)
                                    throw it.error
                                }
                            }
                        }
                    }
                },

            normalRegistrationOrLogin
                .switchMap {
                    authenticate.get().execute(Credential.Otp(otp!!, it))
                }
                .map {
                    when (it) {
                        is Result.Progress -> EnterOtpContract.PartialState.SetInCorrectOtpStatus(false)
                        is Result.Success -> {
                            onboardingAnalytics.get().otpVerified(flow, type, isAutoReadSms)
                            verifiedSuccessfully = true
                            val newlyRegisteredUser = it.value.first
                            val exitingUserAlreadyEnabledAppLock = it.value.second
                            Timber.i("newlyRegisteredUser $newlyRegisteredUser")
                            if (newlyRegisteredUser) { // new user successfully registered
                                onboardingAnalytics.get().setRegisterUserProperty()
                                if (isGooglePopupSelected) {
                                    onboardingAnalytics.get()
                                        .trackRegistrationSuccess(type = OnboardingAnalytics.OnboardingPropertyValue.GOOGLE_POPUP)
                                } else {
                                    onboardingAnalytics.get()
                                        .trackRegistrationSuccess(type = PropertyValue.SMS)
                                }
                                emitViewEvent(EnterOtpContract.ViewEvent.GoToEnterNameScreen)
                            } else {
                                setupLockLoginFlowSubject.onNext(exitingUserAlreadyEnabledAppLock)
                            }

                            val type = when {
                                isGooglePopupSelected -> OnboardingAnalytics.OnboardingPropertyValue.GOOGLE_POPUP
                                else -> PropertyValue.SMS
                            }

                            onboardingAnalytics.get().trackLoginSuccess(
                                type = type,
                                flow = if (flag == OnboardingConstants.FLAG_DEFAULT) flow else PropertyValue.AUTH_ERROR,
                                register = newlyRegisteredUser.toString()
                            )

                            EnterOtpContract.PartialState.SetVerifiedSuccessfully
                        }
                        is Result.Failure -> {
                            when {
                                it.error is InvalidOtp -> {
                                    onboardingAnalytics.get().trackOTPError(
                                        flow,
                                        type,
                                        OnboardingAnalytics.OnboardingPropertyValue.INVALID_OTP
                                    )
                                    EnterOtpContract.PartialState.SetInCorrectOtpStatus(true)
                                }
                                it.error is ExpiredOtp -> {
                                    onboardingAnalytics.get().trackOTPError(
                                        flow,
                                        type,
                                        OnboardingAnalytics.OnboardingPropertyValue.OTP_EXPIRED
                                    )
                                    EnterOtpContract.PartialState.SetInCorrectOtpStatus(true)
                                }
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(EnterOtpContract.ViewEvent.GoToLogin)
                                    EnterOtpContract.PartialState.HideLoading
                                }
                                isInternetIssue(it.error) -> EnterOtpContract.PartialState.SetNetworkError(true)
                                else -> throw it.error
                            }
                        }
                    }
                },

            successfulGratificationTimerSubject.flatMap {
                return@flatMap Observable.timer(3, TimeUnit.SECONDS)
            }.map {
                tracker.get().trackUpdateProfileV1(PropertyValue.MERCHANT, PropertyValue.MOBILE, merchantId)
                return@map EnterOtpContract.PartialState.LogOut
            },
            updateMerchantSubject
                .flatMapSingle {
                    getActiveBusinessId.get().execute()
                }
                .flatMap {
                    merchantId = it
                    updateIndividualMobile.get().execute(mobile)
                }
                .map {
                    when (it) {
                        is Result.Progress -> EnterOtpContract.PartialState.SetInCorrectOtpStatus(false)
                        is Result.Success -> {
                            if (signOutFromAllDevices) {
                                signOutAllSubject.onNext(Unit)
                            } else {
                                signOutCurrent.onNext(Unit)
                            }
                            EnterOtpContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                it.error is BusinessErrors.MerchantExists -> {
                                    tracker.get().trackError("Mobile Update", PropertyValue.EXISTING_NUMBER)
                                    canNavigateToEnterNewNumberScreen.onNext(true)
                                    EnterOtpContract.PartialState.SetMerchantExists
                                }
                                isInternetIssue(it.error) -> EnterOtpContract.PartialState.SetNetworkError(true)
                                else -> throw it.error
                            }
                        }
                    }
                },

            canNavigateToEnterNewNumberScreen.flatMap {
                if (it) {
                    return@flatMap Observable.timer(3, TimeUnit.SECONDS).map {
                        emitViewEvent(EnterOtpContract.ViewEvent.GoBackWithError("merchant_exists"))
                        EnterOtpContract.PartialState.NoChange
                    }
                } else {
                    return@flatMap just(Unit)
                }
            }.map {
                EnterOtpContract.PartialState.NoChange
            },

            signOutCurrent
                .switchMap { UseCase.wrapCompletable(signout.get().execute(null)) }
                .map {
                    when (it) {
                        is Result.Progress -> EnterOtpContract.PartialState.NoChange
                        is Result.Success -> {
                            successfulGratificationTimerSubject.onNext(Unit)

                            EnterOtpContract.PartialState.ShowMigrationSuccessfulView(true)
                        }
                        is Result.Failure -> {
                            when {
                                it.error is InvalidOtp -> EnterOtpContract.PartialState.SetInCorrectOtpStatus(true)
                                it.error is ExpiredOtp -> EnterOtpContract.PartialState.SetInCorrectOtpStatus(true)
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(EnterOtpContract.ViewEvent.GoToLogin)
                                    EnterOtpContract.PartialState.HideLoading
                                }
                                isInternetIssue(it.error) -> EnterOtpContract.PartialState.SetNetworkError(true)
                                else -> throw it.error
                            }
                        }
                    }
                },

            signOutAllSubject
                .switchMap {
                    UseCase.wrapCompletable(signout.get().logout())
                }
                .map {
                    when (it) {
                        is Result.Progress -> EnterOtpContract.PartialState.SetInCorrectOtpStatus(false)
                        is Result.Success -> {
                            successfulGratificationTimerSubject.onNext(Unit)
                            EnterOtpContract.PartialState.ShowMigrationSuccessfulView(true)
                        }
                        is Result.Failure -> {
                            when {
                                it.error is InvalidOtp -> EnterOtpContract.PartialState.SetInCorrectOtpStatus(true)
                                it.error is ExpiredOtp -> EnterOtpContract.PartialState.SetInCorrectOtpStatus(true)
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(EnterOtpContract.ViewEvent.GoToLogin)
                                    EnterOtpContract.PartialState.HideLoading
                                }
                                isInternetIssue(it.error) -> EnterOtpContract.PartialState.SetNetworkError(true)
                                else -> throw it.error
                            }
                        }
                    }
                },

            // handle `show alert` intent
            intent<EnterOtpContract.Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<EnterOtpContract.PartialState> { EnterOtpContract.PartialState.HideAlert }
                        .startWith(EnterOtpContract.PartialState.ShowAlert(it.message))
                },

            intent<EnterOtpContract.Intent.ResendOtp>()
                .map {
                    otpResendSource = NO_INTERNET_SNACKBAR_SOURCE
                    onboardingAnalytics.get().trackResendOtp(
                        flow, type,
                        getCurrentState().lastRetryMedium, otpResendSource
                    )
                    if (otp == null) {
                        sendOtpPublishSubject.onNext(Unit)
                        fetchRetryOptionsPublishSubject.onNext(Unit)
                    } else {
                        resendOtpPublishSubject.onNext(getCurrentState().lastRetryMedium)
                    }
                    EnterOtpContract.PartialState.ReSetDefaultOtpView
                },

            intent<EnterOtpContract.Intent.OtpReadFailed>()
                .map {
                    onboardingAnalytics.get().trackOtpReadFailed(flow, type, it.reason)
                    EnterOtpContract.PartialState.NoChange
                },

            intent<EnterOtpContract.Intent.EnterMobile>()
                .map {
                    onboardingAnalytics.get().trackEditMobile(flow, type)
                    emitViewEvent(EnterOtpContract.ViewEvent.GoToEnterMobileScreen)
                    EnterOtpContract.PartialState.NoChange
                },

            setupLockLoginFlowSubject
                .map { applockEnabled ->
                    if (applockEnabled) {
                        onboardingAnalytics.get().trackAppLockEnabled(PropertyValue.TRUE)
                        emitViewEvent(EnterOtpContract.ViewEvent.GoToAppLockAuthentication)
                    } else {
                        onboardingAnalytics.get().trackAppLockEnabled(PropertyValue.FALSE)
                        emitViewEvent(EnterOtpContract.ViewEvent.GoToSyncDataScreen)
                    }
                    EnterOtpContract.PartialState.NoChange
                }

        )
    }

    override fun reduce(
        currentState: EnterOtpContract.State,
        partialState: EnterOtpContract.PartialState,
    ): EnterOtpContract.State {
        return when (partialState) {
            is EnterOtpContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is EnterOtpContract.PartialState.HideLoading -> currentState.copy(isLoading = false)
            is EnterOtpContract.PartialState.ErrorState -> currentState.copy(
                isLoading = false,
                error = true,
                sendOtpLoader = false
            )
            is EnterOtpContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is EnterOtpContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is EnterOtpContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false,
                sendOtpLoader = false
            )
            is EnterOtpContract.PartialState.SetNetworkErrorWithRetry -> currentState.copy(
                networkErrorWithRetry = partialState.networkErrorWithRetry,
                isLoading = false,
                sendOtpLoader = false
            )
            is EnterOtpContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is EnterOtpContract.PartialState.SetMobile -> currentState.copy(mobile = partialState.mobile)
            is EnterOtpContract.PartialState.SetInCorrectOtpStatus -> currentState.copy(
                otpError = partialState.status,
                isLoading = !partialState.status
            )
            is EnterOtpContract.PartialState.SetResendOtpVisibility -> currentState.copy(isShowResendOtp = partialState.status)
            is EnterOtpContract.PartialState.SetSendOtpLoadingStatus -> currentState.copy(
                sendOtpLoader = partialState.status,
                networkErrorWithRetry = false
            )
            is EnterOtpContract.PartialState.ReSetDefaultOtpView -> currentState.copy(
                sendOtpLoader = true,
                otpError = false
            )
            is EnterOtpContract.PartialState.ClearOtpError -> currentState.copy(otpError = false)
            is EnterOtpContract.PartialState.NoChange -> currentState
            is EnterOtpContract.PartialState.LogOut -> currentState.copy(logout = true)
            is EnterOtpContract.PartialState.ShowMigrationSuccessfulView -> currentState.copy(
                canShowMigrationSuccessfulView = partialState.canShowMigrationSuccessfulView
            )
            is EnterOtpContract.PartialState.SetMerchantExists -> currentState.copy(
                merchantAlreadyExistsError = true,
                isLoading = false
            )
            is EnterOtpContract.PartialState.SetVerifiedSuccessfully -> currentState.copy(verifiedSuccessfully = true)
            is EnterOtpContract.PartialState.SetAppLockABVariant -> currentState.copy(appLockAbUIVariant = partialState.uiAbVariant)
            is EnterOtpContract.PartialState.Flag -> currentState.copy(flag = partialState.flag)
            is EnterOtpContract.PartialState.SetPinOrOtpText -> currentState.copy(pinOrOtpVariant = partialState.variant)
            is EnterOtpContract.PartialState.SetGooglePopUp -> currentState.copy(googlePopUp = partialState.googlePopUp)
            is EnterOtpContract.PartialState.MobileStatus -> currentState.copy(mobileStatus = partialState.mobileStatus)
            is EnterOtpContract.PartialState.HideFallbackOptions -> currentState.copy(showFallbackOption = false)
            is EnterOtpContract.PartialState.ShowFallbackOptions -> currentState.copy(showFallbackOption = true)
            is EnterOtpContract.PartialState.FallbackOptions -> currentState.copy(fallbackOptions = partialState.options)
            is EnterOtpContract.PartialState.LastRetryMedium -> currentState.copy(lastRetryMedium = partialState.lastRetryMedium)
        }
    }
}
