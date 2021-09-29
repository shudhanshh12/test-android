package tech.okcredit.android.communication.brodcaste_receiver

import `in`.okcredit.analytics.Tracker
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.android.AndroidInjection
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

class ApplicationShareReceiver : BroadcastReceiver() {

    @Inject
    lateinit var tracker: Tracker

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        if (intent.extras?.keySet().isNullOrEmpty().not()) {

            var packageId: String? = intent.getStringExtra(EXTRA_PACKAGE) ?: ""

            intent.extras?.keySet()?.forEach { key ->
                try {
                    val componentInfo = intent.extras?.get(key) as ComponentName
                    packageId = componentInfo.packageName
                } catch (e: Exception) {
                    RecordException.recordException(e)
                }
            }

            val appName = if (packageId.isNullOrEmpty().not()) {
                getApplicationName(context, packageId!!)
            } else {
                ""
            }
            val shareType: String? = intent.getStringExtra(SHARE_TYPE)
            val content = intent.getStringExtra(EXTRA_CONTENT_TYPE) ?: ""
            if (shareType.isNullOrEmpty().not()) {
                tracker.trackShared(content, appName, packageId, shareType)
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE = "package"
        const val SHARE_TYPE = "share"
        const val EXTRA_CONTENT_TYPE = "content_type"

        enum class ApplicationShareTypes(val value: String) {
            SHARE("Share"),
            REFERRAL("Referral"),
            HOME_SHARE("Home Share")
        }

        fun getApplicationName(context: Context, packageId: String): String {
            return context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(
                    packageId,
                    PackageManager.GET_META_DATA
                )
            ) as String
        }
    }
}
