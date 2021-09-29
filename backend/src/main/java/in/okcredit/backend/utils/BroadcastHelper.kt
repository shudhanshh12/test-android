package `in`.okcredit.backend.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import javax.inject.Inject

class BroadcastHelper @Inject constructor() {
    object IntentFilters {
        const val PlayerPause: String = "player_pause"
    }

    fun sendBroadcast(context: Context, intent: Intent) {
//        intent.data = Uri.parseUUID.randomUUID().toString())
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun registerReceiver(context: Context, receiver: BroadcastReceiver, intentFilter: IntentFilter) {
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, intentFilter)
    }

    fun unregisterReceiver(context: Context, receiver: BroadcastReceiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
    }

    fun onBroadcastReceived(intent: Intent?) {
        /* we can log intent from here
          intent?.action
           intent?.data*/
    }
}
