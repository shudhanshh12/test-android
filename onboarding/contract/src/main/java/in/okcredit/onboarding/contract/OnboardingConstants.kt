package `in`.okcredit.onboarding.contract

@Deprecated(message = "Keep constant in the class where they belong")
object OnboardingConstants {

    const val ARG_MOBILE = "mobile"

    // TODO Move it back to enter otp screen, once it is modularized
    const val FLAG_VERIFY_NEW_NUMBER_AND_CHANGE_NUMBER = 5
    const val FLAG_NUMBER_CHANGE = 4
    const val FLAG_DEFAULT = 0
    const val FLAG_FORGOT_PATTERN = 2

    const val ARG_GOOGLE_AUTO_READ_MOBILE_NUMBER = "google_auto_read_mobile_number"
}
