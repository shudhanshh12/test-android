package tech.okcredit.android.base.applock

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import tech.okcredit.android.base.R

object Security {

    const val DEVICE_LOCK_SETUP_SCREEN = 1445
    const val AUTHENTICATE = 1446

    fun isKeyguardEnabled(fragment: Fragment): Boolean {

        val keyguardManager = fragment.context?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager.isDeviceSecure
        } else {
            keyguardManager.isKeyguardSecure
        }
    }

    fun isKeyguardEnabled(activity: AppCompatActivity): Boolean {

        val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager.isDeviceSecure
        } else {
            keyguardManager.isKeyguardSecure
        }
    }

    fun authenticate(title: String, activity: AppCompatActivity, listener: SecurityListener, appResume: Boolean) {
        if (isKeyguardEnabled(activity)) {
            val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val keyguardIntent = keyguardManager.createConfirmDeviceCredentialIntent(
                title,
                activity.getString(R.string.confirm_screen_lock)
            )
            try {
                activity.startActivityForResult(keyguardIntent, AUTHENTICATE)
            } catch (e: ActivityNotFoundException) {
                listener.onError(appResume)
            }
        } else {
            listener.onNoDeviceSecurity()
            val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
            activity.startActivityForResult(intent, DEVICE_LOCK_SETUP_SCREEN)
        }
    }
}
