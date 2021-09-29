package tech.okcredit.web.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Telephony
import timber.log.Timber

class SmsBroadcastReceiver(
    private val smsAddress: String,
    private val activity: Activity,
    private val smsReceiverListener: SmsReceivedListener
) {
    private var smsReceiver: BroadcastReceiver? = null
    private val receiveSmsPermission = Telephony.Sms.Intents.SMS_RECEIVED_ACTION

    init {
        startListening()
    }

    private fun startListening() {
        if (smsReceiver == null) {
            smsReceiver = object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent
                ) {
                    if (receiveSmsPermission == intent.action) {
                        intent.extras?.let {
                            val smsMessage = Telephony.Sms.Intents.getMessagesFromIntent(intent)[0]
                            smsMessage.originatingAddress?.let {
                                val smsBody: String? = smsMessage.messageBody?.trim()
                                val address: String = it.trim()
                                if (address.contains(smsAddress, ignoreCase = true) && smsBody != null)
                                    smsReceiverListener.onMessageReceived(smsBody)
                            }
                        }
                    }
                }
            }
            activity.registerReceiver(
                smsReceiver,
                IntentFilter(receiveSmsPermission)
            )
        }
    }

    fun stopListening() {
        try {
            if (smsReceiver != null) {
                activity.unregisterReceiver(smsReceiver)
                smsReceiver = null
            }
        } catch (e: Exception) {
            Timber.e("Error: SmsBroadcastReceiver $e")
        }
    }
}
