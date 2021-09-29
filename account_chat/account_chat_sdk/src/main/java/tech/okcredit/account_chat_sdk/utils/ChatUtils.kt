package tech.okcredit.account_chat_sdk.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.card.MaterialCardView
import org.joda.time.DateTime
import tech.okcredit.account_chat_sdk.R
import tech.okcredit.android.base.utils.DateTimeUtils

object ChatUtils {

    fun setDate(currentTime: String?, targetView: TextView, containerView: MaterialCardView) {
        if (currentTime != null) {
            targetView.visibility = View.VISIBLE
            containerView.visibility = View.VISIBLE
            targetView.text = DateTimeUtils.formatTx(DateTime(currentTime.toLong()), targetView.context)
        } else {
            targetView.visibility = View.GONE
            containerView.visibility = View.GONE
        }
    }

    fun disableChat(
        targetView: TextView,
        containerView: MaterialCardView
    ) {
        targetView.visibility = View.GONE
        containerView.visibility = View.GONE
    }

    fun canShowDate(previousTime: String?, currentTime: String?): Boolean {
        previousTime?.let { previousTime ->
            currentTime?.let { currentTime ->
                return !DateTimeUtils.isSameDay(previousTime, currentTime)
            }
        }
        return false
    }

    fun playReceivedSound(context: Context) {
        ReceivedMediaPlayer.init(context).start(context)
    }

    fun playSentSound(context: Context) {
        SentMediaPlayer.init(context).start(context)
    }

    fun provideHapticFeedback(context: Context) {
        val v: Vibrator? =
            getSystemService(context, Vibrator::class.java) as Vibrator?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // deprecated in API 26
            v?.vibrate(200)
        }
    }

    fun getAccountRole(role: String?): String? {
        role?.let {
            if (role == "SELLER") return "BUYER"
            if (role == "BUYER") return "SELLER"
        }
        return null
    }

    object ReceivedMediaPlayer {
        private var context: Context? = null
        var mp: MediaPlayer? = null
        fun start(context: Context) {
            mp?.start()
        }

        fun init(context: Context?): ReceivedMediaPlayer {
            if (context != null) {
                ReceivedMediaPlayer.context = context
                mp = MediaPlayer.create(
                    ReceivedMediaPlayer.context, R.raw.received
                )
            }
            return this
        }
    }

    object SentMediaPlayer {
        private var context: Context? = null
        var mp: MediaPlayer? = null
        fun start(context: Context) {
            mp?.start()
        }

        fun init(context: Context?): SentMediaPlayer {
            if (context != null) {
                SentMediaPlayer.context = context
                mp = MediaPlayer.create(
                    SentMediaPlayer.context, R.raw.sent
                )
            }
            return this
        }
    }
}
