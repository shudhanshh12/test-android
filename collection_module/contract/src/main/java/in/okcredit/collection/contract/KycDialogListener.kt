package `in`.okcredit.collection.contract

interface KycDialogListener {
    fun onDisplayed(eventName: String, campaign: String)
    fun onConfirmKyc(dontAskAgain: Boolean, eventName: String = "")
    fun onCancelKyc(dontAskAgain: Boolean, eventName: String)
    fun onDismissKyc(eventName: String)
}

enum class KycDialogMode(val value: String) {
    Complete("Complete"), Risk("Risk"), Status("Status"), Remind("Remind")
}
