package tech.okcredit.contract

interface AppLockTracker {
    fun trackEvents(eventName: String, source: String? = null, screen: String? = null)
}

const val SET_PIN = "Set PIN"
const val UPDATE_PIN = "Update PIN"
const val FORGOT_PIN = "Forgot PIN"
