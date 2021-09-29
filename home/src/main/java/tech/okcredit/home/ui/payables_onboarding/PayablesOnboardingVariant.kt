package tech.okcredit.home.ui.payables_onboarding

import android.annotation.SuppressLint

enum class PayablesOnboardingVariant(val value: String) {
    TEST_GROUP("TestGroup"),
    CONTROL_GROUP("ControlGroup");

    companion object {
        private val map = values().associateBy(PayablesOnboardingVariant::value)

        @SuppressLint("SyntheticAccessor")
        fun fromString(value: String) = map[value]
    }
}
