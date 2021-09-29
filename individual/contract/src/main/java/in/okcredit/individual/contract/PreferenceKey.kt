package `in`.okcredit.individual.contract

enum class PreferenceKey(val key: String, val defaultValue: String) {
    LANGUAGE("lang", "en"),
    WHATSAPP("whatsapp", false.toString()),
    PAYMENT_PASSWORD("payment_password_enabled", false.toString()),
    APP_LOCK("app_lock", false.toString()),
    FINGER_PRINT_LOCK("fingerprint_lock", false.toString()),
    FOUR_DIGIT_PIN("four_digit_pin", false.toString()),
}
