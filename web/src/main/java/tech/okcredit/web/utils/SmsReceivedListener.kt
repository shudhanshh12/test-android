package tech.okcredit.web.utils

interface SmsReceivedListener {
    fun onMessageReceived(msg: String)
}
