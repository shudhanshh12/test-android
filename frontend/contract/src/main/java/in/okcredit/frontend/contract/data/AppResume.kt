package `in`.okcredit.frontend.contract.data

enum class AppResume(val resumeType: String) {
    NONE("NONE"),
    BUSINESS_NAME("BUSINESS_NAME"),
    APP_LOCK_SETUP("APP_LOCK_SETUP"),
    NEW_APP_LOCK_RESUME("NEW_APP_LOCK_RESUME"),
    OLD_APP_LOCK_RESUME("OLD_APP_LOCK_RESUME"),
    NOT_AUTHENTICAED("NOT_AUTHENTICAED")
}
