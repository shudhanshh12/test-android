package `in`.okcredit.shared.deeplink

import `in`.okcredit.shared.R
import `in`.okcredit.shared.deeplink.DeeplinkConstants.INTERNAL_NAVIGATION_DEEPLINK_SCHEME
import `in`.okcredit.shared.view.CallPermissionActivity
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.annotation.IdRes
import androidx.navigation.findNavController
import dagger.Lazy
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.isPermissionGranted
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.app_contract.ResolveIntentsAndExtrasFromDeeplink
import javax.inject.Inject

class InternalDeeplinkNavigator @Inject constructor(
    private val resolveIntentsAndExtrasFromDeeplink: Lazy<ResolveIntentsAndExtrasFromDeeplink>,
    @IdRes private val fragmentContainerView: Int,
) {

    fun executeDeeplink(deeplink: String, activity: Activity) {
        val deeplinkUri = Uri.parse(deeplink)
        when {
            isCallDeeplink(deeplinkUri) -> executeCall(deeplinkUri, activity)
            isWhatsAppDeeplink(deeplinkUri) -> executeWhatsAppDeeplink(deeplink, activity)
            isInternalNavigationDeeplink(deeplinkUri) -> executeInternalNavigationDeeplink(deeplinkUri, activity)
            else -> executeInternalDeeplink(deeplink, activity)
        }
    }

    private fun executeInternalDeeplink(deeplink: String, activity: Activity) {
        try {
            (
                resolveIntentsAndExtrasFromDeeplink.get().execute(deeplink).lastOrNull() ?: resolveIntent(
                    deeplink,
                    activity
                )
                )
                ?.also { activity.startActivity(it) }
                ?: RecordException.recordException(IllegalArgumentException("Unknown deeplink $deeplink"))
        } catch (_: SecurityException) {
            // https://console.firebase.google.com/u/0/project/okcredit-6cb68/crashlytics/app/android:in.okcredit.merchant/issues/7530fadbb3ee88b01eb62aea4bcbc5e1
            // https://stackoverflow.com/questions/57223127/securityexception-crash-opening-link-in-external-browser-when-mx-player-is-inst
            // Todo : Handle this case if the exception count increases
            RecordException.recordException(RuntimeException("Only MX player found, fail silently"))
        }
    }

    private fun isCallDeeplink(uri: Uri) = uri.scheme == "tel"

    private fun executeCall(uri: Uri, activity: Activity) {
        if (activity.isPermissionGranted(Manifest.permission.CALL_PHONE)) {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = uri
            }
            activity.startActivity(intent)
        } else {
            CallPermissionActivity.start(activity, uri)
        }
    }

    private fun isWhatsAppDeeplink(deeplinkUri: Uri) = deeplinkUri.scheme == "whatsapp"

    private fun executeWhatsAppDeeplink(deeplink: String, activity: Activity) {
        resolveIntent(deeplink, activity)?.let {
            activity.startActivity(it)
        } ?: activity.shortToast(R.string.whatsapp_not_installed)
    }

    private fun isInternalNavigationDeeplink(deeplinkUri: Uri) =
        deeplinkUri.scheme == INTERNAL_NAVIGATION_DEEPLINK_SCHEME

    private fun executeInternalNavigationDeeplink(deeplinkUri: Uri, activity: Activity) {
        try {
            activity.findNavController(fragmentContainerView).navigate(deeplinkUri)
        } catch (e: IllegalArgumentException) {
            RecordException.recordException(e)
        }
    }

    private fun resolveIntent(deeplink: String, activity: Activity): Intent? {
        return Intent(Intent.ACTION_VIEW)
            .apply { data = Uri.parse(deeplink) }
            .takeIf { it.resolveActivity(activity.packageManager) != null }
    }
}
