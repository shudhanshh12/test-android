package tech.okcredit.applock.analytics

object AppLockEventProperties {
    const val CONFIRM_OTP_SUCCESSFUL = "Confirm OTP Successful"
    const val CONFIRM_OTP_FAILURE = "Confirm OTP Failure"
    const val PIN_ENTRY_STARTED = "Pin Entry Started"
    const val PIN_RE_ENTERED_MATCH = "Pin Re-entered Match"
    const val PIN_RE_ENTERED_MIS_MATCH = "Pin Re-entered Mis-Match"
    const val SECURITY_PIN_MIS_MATCH = "Security Pin Mismatch"
    const val SECURITY_PIN_MATCH = "Security Pin match"
    const val FORGOT_PASSWORD_CLICK = "Forgot Password click"
    const val OTP_AUTHENTICATION_STARTED = "OTP authentication started"
    const val RESEND_OTP = "Resend OTP"
}
