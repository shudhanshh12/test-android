package `in`.okcredit.onboarding.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.BuildConfig
import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.ALLOW_WHATSAPP
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.AUTO_LANG_CURRENT_LANGUAGE
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.CHANGE_LANGUAGE_BUTTON_CLICKED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.CONFIRM_LANGUAGE
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.DISCLAIMER_CLICKED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.DISMISS_CHANGE_LANGUAGE_BOTTOMSHEET
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.DISMISS_WHATSAPP
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.ENTER_NAME_SCREEN
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.GET_STARTED_CLICKED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.IS_APP_LOCK_ENABLED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.LANGUAGE_BOTTOM_SHEET_SHOWN
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.LOGIN_STARTED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.OTP_AUTO_READ_FAILED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.OTP_ENTERED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.OTP_ERROR
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.OTP_RECEIVED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.REGISTER_STARTED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.REQUEST_OTP_SUCCESSFUL
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.RESEND_OTP
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.RESEND_OTP_SUCCESSFUL
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.SKIP
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.SOCIAL_NEXT_STORY_CLICKED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.SOCIAL_PREVIOUS_STORY_CLICKED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.SOCIAL_SCREEN_LOADED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.SOCIAL_STORY_COMPLETED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.VERIFIED_OTP
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.VIEW_MOBILE_SCREEN
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.VIEW_ONBOARDING_SCREEN
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.VIEW_SOCIAL_SCREEN
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingEvent.WHATSAPP_DIALOG_DISPLAYED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.AUTO_READ
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.HAS_TRUECALLER
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.MESSAGE
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.SOCIAL_ACTIVE_STORY
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.SOCIAL_SOURCE
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.SOCIAL_STORY_COUNT
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.SOCIAL_STORY_PROGRESS
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.TC_ADDRESS
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.TC_CITY
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.TC_COUNTRY_CODE
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.TC_EMAIL
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.TC_FIRST_NAME
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.TC_GENDER
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.TC_LAST_NAME
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.TC_PROFILE_URL
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyKey.URL
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.CALL_CHANNEL
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.DEFAULT_SOURCE
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.MOBILE_SCREEN
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.NO_MOBILE_NUMBER_FOUND
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.SMS_CHANNEL
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.SOCIAL_SCREENS_SOURCE_BACKEND
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.SOCIAL_SCREENS_SOURCE_FALLBACK
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.TILE
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.WHATSAPP_CHANNEL
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.OnboardingPropertyValue.WHATSAPP_NOT_INSTALLED
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics.UserProperty.REGISTERED_VERSION
import com.scottyab.rootbeer.RootBeer
import com.truecaller.android.sdk.TrueError
import dagger.Lazy
import dagger.Reusable
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.auth.server.AuthApiClient.RequestOtpMedium
import tech.okcredit.android.base.language.LocaleManager
import javax.inject.Inject

@Reusable
class OnboardingAnalytics @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
    private val localeManager: Lazy<LocaleManager>,
    private val rootBeer: Lazy<RootBeer>,
) {

    private val trueCallerErrorMap = mapNumberFetchError()

    object OnboardingEvent {
        const val SKIP = "Skip"
        const val LOGIN_STARTED = "Login Started"
        const val REGISTER_STARTED = "Register: Started"
        const val VIEW_ONBOARDING_SCREEN = "View Onboarding Screen"
        const val VIEW_MOBILE_SCREEN = "View Mobile Screen"
        const val MOBILE_NUMBER_ENTERED = "Mobile Number Entered"
        const val ALLOW_WHATSAPP = "Allow WhatsApp"
        const val DISMISS_WHATSAPP = "Dismiss Whatsapp"
        const val WHATSAPP_DIALOG_DISPLAYED = "Whatsapp Dialog Displayed"
        const val CONFIRM_LANGUAGE = "Confirm Language"
        const val TRUECALLER_DISPLAYED = "Truecaller Displayed"
        const val VERIFIED_OTP = "Verified OTP"
        const val OTP_RECEIVED = "OTP Received"
        const val OTP_ENTERED = "OTP Entered"
        const val OTP_ERROR = "OTP Error"
        const val OTP_AUTO_READ_FAILED = "OTP AutoRead Failed"
        const val REQUEST_OTP_SUCCESSFUL = "Request OTP Successful"
        const val RESEND_OTP = "Resend OTP"
        const val RESEND_OTP_SUCCESSFUL = "Resend OTP Successful"
        const val EDIT_MOBILE = "Edit Mobile"
        const val ENTER_NAME_SCREEN = "Enter Name Screen"
        const val ENTER_NAME_SKIPPED = "Enter Name Skipped"
        const val NAME_ENTERED = "Name Entered"
        const val IS_APP_LOCK_ENABLED = "IsAppLock Enabled"
        const val AUTO_LANG_CURRENT_LANGUAGE = "Auto lang current language"

        const val DISMISS_CHANGE_LANGUAGE_BOTTOMSHEET = "Dismiss change language bottomsheet"
        const val CHANGE_LANGUAGE_BUTTON_CLICKED = "Change language Button Clicked"
        const val LANGUAGE_BOTTOM_SHEET_SHOWN = "Language bottom sheet shown"

        const val VIEW_SOCIAL_SCREEN = "View Social Screen"
        const val SOCIAL_SCREEN_LOADED = "Social Screen Loaded"
        const val GET_STARTED_CLICKED = "Get Started Clicked"
        const val DISCLAIMER_CLICKED = "Terms and Privacy Link Clicked"
        const val SOCIAL_NEXT_STORY_CLICKED = "Social Next Story Clicked"
        const val SOCIAL_PREVIOUS_STORY_CLICKED = "Social Previous Story Clicked"
        const val SOCIAL_STORY_COMPLETED = "Social Story Completed"
    }

    object OnboardingPropertyKey {
        const val TC_FIRST_NAME = "TC First Name"
        const val TC_LAST_NAME = "TC Last Name"
        const val TC_COUNTRY_CODE = "TC Code Code"
        const val TC_CITY = "TC City"
        const val TC_EMAIL = "TC Email"
        const val TC_ADDRESS = "TC Address"
        const val TC_PROFILE_URL = "TC Profile Url"
        const val TC_GENDER = "TC Gender"
        const val HAS_TRUECALLER = "Has Truecaller"
        const val AUTO_READ = "Auto Read"
        const val NUMBER_CHANGE = "Number Change"
        const val MESSAGE = "Message"
        const val IS_ROOTED_PHONE = "Is Rooted Phone"
        const val HAS_CHANGED_LANGUAGE = "Has changed language"

        const val SOCIAL_SOURCE = "source"
        const val SOCIAL_STORY_COUNT = "story_count"
        const val SOCIAL_ACTIVE_STORY = "active_story"
        const val SOCIAL_STORY_PROGRESS = "story_progress"

        const val URL = "url"
    }

    object OnboardingPropertyValue {
        const val OTP = "OTP"
        const val TRUE_CALLER = "Truecaller"
        const val GOOGLE_POPUP = "Google Popup"
        const val MANUAL = "Manual"
        const val MOBILE_SCREEN = "Mobile Screen"
        const val TILE = "Tile"
        const val BUTTON = "Button"
        const val SCREEN = "Screen"
        const val AUTO = "Auto"
        const val NEW = "New"
        const val OLD = "Old"
        const val INVALID_OTP = "Invalid OTP"
        const val OTP_EXPIRED = "OTP Expired"
        const val TOO_MANY_REQUESTS = "Too Many Requests"
        const val AUTHENTICATION_ISSUE = "Authentication Failed"
        const val NO_INTERNET = "No Internet"
        const val UNKNOWN_ERROR = "Unknown Error"
        const val POPUP = "Popup"
        const val WHATSAPP = "Whatsapp"
        const val WHATSAPP_NOT_INSTALLED = "Whatsapp Not Installed"
        const val NO_MOBILE_NUMBER_FOUND = "No Mobile Number Found"
        const val NO_INTERNET_SNACKBAR_SOURCE = "No Internet Snackbar"
        const val DEFAULT_SOURCE = "Default"
        const val SMS_CHANNEL = "Sms"
        const val CALL_CHANNEL = "Call"
        const val WHATSAPP_CHANNEL = "Whatsapp"

        const val SOCIAL_SCREENS_SOURCE_BACKEND = "backend"
        const val SOCIAL_SCREENS_SOURCE_FALLBACK = "fallback"
    }

    object UserProperty {
        const val REGISTERED_VERSION = "Registered Version"
    }

    private fun getDefaultEventProperties(): MutableMap<String, Any> {
        return mutableMapOf(
            AUTO_LANG_CURRENT_LANGUAGE to localeManager.get().getLanguage(),
        )
    }

    fun trackViewLanguageScreen() {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = PropertyValue.LOGIN
            this[PropertyKey.TYPE] = TILE
        }

        analyticsProvider.get().trackEvents(Event.VIEW_LANGUAGE, properties)
    }

    fun trackLanguageBottomSheetDismissed(hasChangedLanguage: Boolean) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.TYPE] = TILE
            this[OnboardingPropertyKey.HAS_CHANGED_LANGUAGE] = hasChangedLanguage
        }

        analyticsProvider.get().trackEvents(DISMISS_CHANGE_LANGUAGE_BOTTOMSHEET, properties)
    }

    fun trackChangeLanguageButtonClicked() {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.TYPE] = TILE
        }
        analyticsProvider.get().trackEvents(CHANGE_LANGUAGE_BUTTON_CLICKED, properties)
    }

    fun trackLanguageBottomSheetShown() {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.TYPE] = TILE
        }
        analyticsProvider.get().trackEvents(LANGUAGE_BOTTOM_SHEET_SHOWN, properties)
    }

    @NonNls
    fun trackSelectLanguage(language: String) {
        // Not using default properties to avoid ambiguity in language event property
        val properties = mapOf(
            PropertyKey.SET_VALUE to language,
            PropertyKey.FLOW to PropertyValue.LOGIN
        )
        analyticsProvider.get().trackEvents(Event.SELECT_LANGUAGE, properties)
    }

    @NonNls
    fun trackConfirmLanguage() {
        val default = if (localeManager.get().getLanguage() == LocaleManager.LANGUAGE_ENGLISH) {
            PropertyValue.TRUE
        } else {
            PropertyValue.FALSE
        }
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.SET_VALUE] = localeManager.get().getLanguage()
            this[PropertyKey.DEFAULT] = default
            this[PropertyKey.FLOW] = PropertyValue.LOGIN
        }
        analyticsProvider.get().trackEvents(CONFIRM_LANGUAGE, properties)
    }

    fun trackViewMobileScreen() {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = PropertyValue.LOGIN
        }
        analyticsProvider.get().trackEvents(VIEW_MOBILE_SCREEN, properties)
    }

    @NonNls
    fun trackSelectMobile(type: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = PropertyValue.LOGIN
            this[PropertyKey.TYPE] = type
        }
        analyticsProvider.get().trackEvents(Event.SELECT_MOBILE, properties)
    }

    @NonNls
    fun trackSkip(type: String, method: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = PropertyValue.LOGIN
            this[PropertyKey.TYPE] = type
            this[PropertyKey.METHOD] = method
        }
        analyticsProvider.get().trackEvents(SKIP, properties)
    }

    @NonNls
    fun trackVerifyMobile(isTrueCallerInstalled: Boolean) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = PropertyValue.LOGIN
            this[HAS_TRUECALLER] = isTrueCallerInstalled.toString().capitalize()
        }
        analyticsProvider.get().trackEvents(Event.VERIFY_MOBILE, properties)
    }

    @NonNls
    fun trackNumberReadDisplayed(type: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = PropertyValue.LOGIN
        }
        analyticsProvider.get().trackEvents(type, properties)
    }

    @NonNls
    fun trackTrueCallerFailure(type: String, error: Int) {
        trackTrueCallerFailure(type, trueCallerErrorMap.getValue(error))
    }

    @NonNls
    fun trackWhatsAppFailure(type: String, flow: String) {
        trackTrueCallerFailure(type, WHATSAPP_NOT_INSTALLED, flow)
    }

    @NonNls
    fun trackMobileNumberHintNotFound(type: String) {
        trackTrueCallerFailure(type, NO_MOBILE_NUMBER_FOUND)
    }

    @NonNls
    private fun trackTrueCallerFailure(
        type: String,
        error: String,
        flow: String = PropertyValue.LOGIN,
    ) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.TYPE] = type
            this[PropertyKey.FLOW] = flow
            this[PropertyValue.REASON] = error
        }
        analyticsProvider.get().trackEvents(Event.FAILURE, properties)
    }

    @NonNls
    fun trackRegistrationStarted(type: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.TYPE] = type
        }
        analyticsProvider.get().trackEvents(REGISTER_STARTED, properties)
    }

    @NonNls
    fun trackLoginStarted(type: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.TYPE] = type
        }
        analyticsProvider.get().trackEvents(LOGIN_STARTED, properties)
    }

    fun trackViewOnboardingScreen() {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = PropertyValue.LOGIN
        }
        analyticsProvider.get().trackEvents(VIEW_ONBOARDING_SCREEN, properties)
    }

    @NonNls
    fun setTruecallerUserProperty(
        firstName: String?,
        lastName: String?,
        countryCode: String?,
        city: String?,
        email: String?,
        address: String?,
        profileUrl: String?,
        gender: String?,
    ) {
        val properties = mapOf(
            TC_FIRST_NAME to (firstName ?: ""),
            TC_LAST_NAME to (lastName ?: ""),
            TC_COUNTRY_CODE to (countryCode ?: ""),
            TC_CITY to (city ?: ""),
            TC_EMAIL to (email ?: ""),
            TC_ADDRESS to (address ?: ""),
            TC_PROFILE_URL to (profileUrl ?: ""),
            TC_GENDER to (gender ?: ""),
        )
        analyticsProvider.get().setUserProperty(properties)
    }

    fun setRegisterUserProperty() {
        val properties = getDefaultEventProperties().apply {
            this[REGISTERED_VERSION] = BuildConfig.VERSION_CODE.toString()
        }
        analyticsProvider.get().setUserProperty(properties)
    }

    @NonNls
    fun trackRegistrationSuccess(type: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.TYPE] = type
        }
        analyticsProvider.get().trackEvents(Event.REGISTER_SUCCESSFUL, properties)
        analyticsProvider.get().flushEvents()
    }

    @NonNls
    fun trackLoginSuccess(type: String, flow: String, register: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
            this[PropertyKey.REGISTER] = register
            this[OnboardingPropertyKey.IS_ROOTED_PHONE] = rootBeer.get().isRooted()
        }
        analyticsProvider.get().trackEvents(Event.LOGIN_SUCCESS, properties)
    }

    fun trackAppLockEnabled(type: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.TYPE] = type
        }
        analyticsProvider.get().trackEvents(IS_APP_LOCK_ENABLED, properties)
    }

    fun trackMobileNumberCleared() {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.TYPE] = PropertyValue.MOBILE
            this[PropertyKey.SCREEN] = MOBILE_SCREEN
        }
        analyticsProvider.get().trackEvents(Event.CLEARED, properties)
    }

    fun trackViewRequestOTP(type: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] =
                PropertyValue.LOGIN
            this[PropertyKey.TYPE] = type
        }
        analyticsProvider.get().trackEvents(Event.REQUEST_OTP, properties)
    }

    fun trackRequestOTPSuccessful(type: String, flow: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
        }
        analyticsProvider.get().trackEvents(REQUEST_OTP_SUCCESSFUL, properties)
    }

    fun trackOtpReceived(flow: String, type: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
        }
        analyticsProvider.get().trackEvents(OTP_RECEIVED, properties)
    }

    fun trackOtpEntered(flow: String, type: String, channel: RequestOtpMedium = RequestOtpMedium.SMS) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
            this[PropertyKey.CHANNEL] = getChannelName(channel)
        }
        analyticsProvider.get().trackEvents(OTP_ENTERED, properties)
    }

    fun trackOtpReadFailed(flow: String, type: String, reason: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
            this[PropertyKey.REASON] = reason
        }
        analyticsProvider.get().trackEvents(OTP_AUTO_READ_FAILED, properties)
    }

    fun trackResendOtp(
        flow: String,
        type: String,
        channel: RequestOtpMedium = RequestOtpMedium.SMS,
        source: String = DEFAULT_SOURCE,
    ) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.CHANNEL] = getChannelName(channel)
            this[PropertyKey.SOURCE] = source
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
        }
        analyticsProvider.get().trackEvents(RESEND_OTP, properties)
    }

    fun trackResendOtpSuccess(
        flow: String,
        type: String,
        channel: RequestOtpMedium = RequestOtpMedium.SMS,
        source: String = DEFAULT_SOURCE,
    ) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.CHANNEL] = getChannelName(channel)
            this[PropertyKey.SOURCE] = source
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
        }
        analyticsProvider.get().trackEvents(RESEND_OTP_SUCCESSFUL, properties)
    }

    private fun getChannelName(channel: RequestOtpMedium): String {
        return when (channel) {
            RequestOtpMedium.SMS -> SMS_CHANNEL
            RequestOtpMedium.CALL -> CALL_CHANNEL
            RequestOtpMedium.WHATSAPP -> WHATSAPP_CHANNEL
        }
    }

    fun trackEditMobile(flow: String, type: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
        }
        analyticsProvider.get().trackEvents(OnboardingEvent.EDIT_MOBILE, properties)
    }

    fun trackAllowWhatsapp(flow: String, type: String, source: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
            this[PropertyKey.SOURCE] = source
        }
        analyticsProvider.get().trackEvents(ALLOW_WHATSAPP, properties)
    }

    fun trackDismissWhatsapp(flow: String, type: String, source: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
            this[PropertyKey.SOURCE] = source
        }
        analyticsProvider.get().trackEvents(DISMISS_WHATSAPP, properties)
    }

    fun trackWhatsappDialogDisplay(flow: String, type: String, source: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
            this[PropertyKey.SOURCE] = source
        }
        analyticsProvider.get().trackEvents(WHATSAPP_DIALOG_DISPLAYED, properties)
    }

    fun otpVerified(flow: String, type: String, autoRead: Boolean) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
            this[AUTO_READ] = autoRead
            this[PropertyKey.VALUE] = if (autoRead) {
                OnboardingPropertyValue.AUTO
            } else {
                OnboardingPropertyValue.MANUAL
            }
        }
        analyticsProvider.get().trackEvents(VERIFIED_OTP, properties)
    }

    fun trackNumberChangeOtpVerified(flow: String, autoRead: String, value: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = flow
            this[AUTO_READ] = autoRead
            this[PropertyKey.VALUE] = value
        }
        analyticsProvider.get().trackEvents(VERIFIED_OTP, properties)
    }

    fun trackLoginOtpVerified(flow: String, type: String, channel: RequestOtpMedium = RequestOtpMedium.SMS) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
            this[PropertyKey.CHANNEL] = getChannelName(channel)
        }
        analyticsProvider.get().trackEvents(VERIFIED_OTP, properties)
    }

    fun trackOTPError(flow: String, type: String, reason: String, channel: RequestOtpMedium = RequestOtpMedium.SMS) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = flow
            this[PropertyKey.TYPE] = type
            this[PropertyKey.REASON] = reason
            this[PropertyKey.CHANNEL] = getChannelName(channel)
        }
        analyticsProvider.get().trackEvents(OTP_ERROR, properties)
    }

    fun trackEnterNameScreen() {
        trackNameScreen(ENTER_NAME_SCREEN)
    }

    fun trackNameScreen(event: String) {
        val properties = getDefaultEventProperties().apply {
            this[PropertyKey.FLOW] = PropertyValue.REGISTER
        }
        analyticsProvider.get().trackEvents(event, properties)
    }

    private fun mapNumberFetchError(): HashMap<Int, String> {
        val error = HashMap<Int, String>()
        error[TrueError.ERROR_TYPE_INTERNAL] = "ERROR_TYPE_INTERNAL"
        error[TrueError.ERROR_TYPE_NETWORK] = "ERROR_TYPE_NETWORK"
        error[TrueError.ERROR_TYPE_USER_DENIED] = "ERROR_TYPE_USER_DENIED"
        error[TrueError.ERROR_PROFILE_NOT_FOUND] = "ERROR_PROFILE_NOT_FOUND"
        error[TrueError.ERROR_TYPE_UNAUTHORIZED_USER] = "ERROR_TYPE_UNAUTHORIZED_USER"
        error[TrueError.ERROR_TYPE_TRUECALLER_CLOSED_UNEXPECTEDLY] = "ERROR_TYPE_TRUECALLER_CLOSED_UNEXPECTEDLY"
        error[TrueError.ERROR_TYPE_TRUESDK_TOO_OLD] = "ERROR_TYPE_TRUESDK_TOO_OLD"
        error[TrueError.ERROR_TYPE_POSSIBLE_REQ_CODE_COLLISION] = "ERROR_TYPE_POSSIBLE_REQ_CODE_COLLISION"
        error[TrueError.ERROR_TYPE_RESPONSE_SIGNATURE_MISMATCH] = "ERROR_TYPE_RESPONSE_SIGNATURE_MISMATCH"
        error[TrueError.ERROR_TYPE_REQUEST_NONCE_MISMATCH] = "ERROR_TYPE_REQUEST_NONCE_MISMATCH"
        error[TrueError.ERROR_TYPE_INVALID_ACCOUNT_STATE] = "ERROR_TYPE_INVALID_ACCOUNT_STATE"
        error[TrueError.ERROR_TYPE_TC_NOT_INSTALLED] = "ERROR_TYPE_TC_NOT_INSTALLED"
        error[TrueError.ERROR_TYPE_PARTNER_INFO_NULL] = "ERROR_TYPE_PARTNER_INFO_NULL"
        error[TrueError.ERROR_TYPE_USER_DENIED_WHILE_LOADING] = "ERROR_TYPE_USER_DENIED_WHILE_LOADING"
        error[TrueError.ERROR_TYPE_CONTINUE_WITH_DIFFERENT_NUMBER] = "ERROR_TYPE_CONTINUE_WITH_DIFFERENT_NUMBER"
        return error
    }

    fun logBreadcrumb(log: String, message: String? = null) {
        if (message == null) {
            analyticsProvider.get().logBreadcrumb(log)
        } else {
            val properties: Map<String, Any> = mapOf(MESSAGE to message)
            analyticsProvider.get().logBreadcrumb(log, properties)
        }
    }

    fun trackFlow(flow: String, number: String) {
        if (flow == PropertyValue.LOGIN) {
            trackLoginStarted(OnboardingPropertyValue.TRUE_CALLER)
        } else {
            trackRegistrationStarted(
                OnboardingPropertyValue.TRUE_CALLER,
            )
        }
    }

    fun trackEnteredMobileScreen() {
        analyticsProvider.get().trackEvents(OnboardingEvent.MOBILE_NUMBER_ENTERED, getDefaultEventProperties())
    }

    // Social
    fun trackViewSocialScreen() {
        analyticsProvider.get().trackEvents(VIEW_SOCIAL_SCREEN)
    }

    fun trackSocialScreenLoaded(sourceBackend: Boolean, storyCount: Int) {
        analyticsProvider.get().trackEvents(
            SOCIAL_SCREEN_LOADED,
            mapOf(
                SOCIAL_SOURCE to if (sourceBackend) SOCIAL_SCREENS_SOURCE_BACKEND else SOCIAL_SCREENS_SOURCE_FALLBACK,
                SOCIAL_STORY_COUNT to storyCount,
            )
        )
    }

    fun trackGetStartedClicked(activeStory: Int, progress: Int) {
        analyticsProvider.get().trackEvents(
            GET_STARTED_CLICKED,
            mapOf(
                SOCIAL_ACTIVE_STORY to activeStory,
                SOCIAL_STORY_PROGRESS to progress,
            )
        )
    }

    fun trackDisclaimerClicked(url: String) {
        analyticsProvider.get().trackEvents(
            DISCLAIMER_CLICKED,
            mapOf(
                URL to url,
            )
        )
    }

    fun trackSocialNextStoryClicked(activeStory: Int, progress: Int) {
        analyticsProvider.get().trackEvents(
            SOCIAL_NEXT_STORY_CLICKED,
            mapOf(
                SOCIAL_ACTIVE_STORY to activeStory,
                SOCIAL_STORY_PROGRESS to progress,
            )
        )
    }

    fun trackSocialPreviousStoryClicked(activeStory: Int, progress: Int) {
        analyticsProvider.get().trackEvents(
            SOCIAL_PREVIOUS_STORY_CLICKED,
            mapOf(
                SOCIAL_ACTIVE_STORY to activeStory,
                SOCIAL_STORY_PROGRESS to progress,
            )
        )
    }

    fun trackSocialStoryCompleted(activeStory: Int) {
        analyticsProvider.get().trackEvents(
            SOCIAL_STORY_COMPLETED,
            mapOf(
                SOCIAL_ACTIVE_STORY to activeStory
            )
        )
    }
}
