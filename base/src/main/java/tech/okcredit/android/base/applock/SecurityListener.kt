package tech.okcredit.android.base.applock

interface SecurityListener {
    fun onNoDeviceSecurity()
    fun onError(appResume: Boolean)
}
