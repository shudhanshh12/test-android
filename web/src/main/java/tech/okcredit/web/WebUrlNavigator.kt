package tech.okcredit.web

import `in`.okcredit.web.WebExperiment
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.web.custom_tabs.CustomTabHelper
import tech.okcredit.web.ui.WebViewActivity
import tech.okcredit.web.utils.WebViewUtils
import javax.inject.Inject

/**
 * Wrapper class which decides whether to use chrome custom tabs on the basis of constraints defined or should we use
 * [WebViewActivity] build in the module.
 */
class WebUrlNavigator @Inject constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
) {

    companion object {
        // Refer https://source.chromium.org/chromium/chromium/src/+/master:chrome/android/java/src/org/chromium/chrome/browser/customtabs/CustomTabIntentDataProvider.java
        // for more details
        /** Extra that enables the client to disable the star button in menu.  */
        private val EXTRA_DISABLE_STAR_BUTTON = "org.chromium.chrome.browser.customtabs.EXTRA_DISABLE_STAR_BUTTON"

        /** Extra that enables the client to disable the download button in menu.  */
        private val EXTRA_DISABLE_DOWNLOAD_BUTTON = "org.chromium.chrome.browser.customtabs.EXTRA_DISABLE_DOWNLOAD_BUTTON"
    }

    fun openUrl(activity: Activity, url: String) {
        if (isThirdPartyUrl(url)) {
            openChromeCustomTabs(activity, url)
        } else {
            openWebViewActivity(activity, url)
        }
    }

    private fun openWebViewActivity(activity: Activity, url: String) {
        return WebViewActivity.start(context = activity, url)
    }

    private fun openChromeCustomTabs(activity: Activity, url: String) {
        // check if url can be opened natively by any application
        if (launchNativeBeforeApi30(activity, Uri.parse(url))) {
            return
        }

        val color: Int = activity.getColorFromAttr(R.attr.colorPrimary)
        val secondaryColor: Int = activity.getColorFromAttr(R.attr.colorSecondary)

        val intentBuilder: CustomTabsIntent.Builder = CustomTabsIntent.Builder().apply {
            val defaultColors = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(color)
                .setSecondaryToolbarColor(secondaryColor)
                .build()
            setDefaultColorSchemeParams(defaultColors)
            setShareState(CustomTabsIntent.SHARE_STATE_OFF)
        }
        val customTabsIntent = intentBuilder.build()

        val packageName = CustomTabHelper.getPackageNameToUse(activity)
        if (packageName != null) {
            customTabsIntent.intent.putExtra(EXTRA_DISABLE_DOWNLOAD_BUTTON, true)
            customTabsIntent.intent.putExtra(EXTRA_DISABLE_STAR_BUTTON, true)
            customTabsIntent.intent.setPackage(packageName)
            customTabsIntent.launchUrl(activity, Uri.parse(url))
        } else {
            // fallback to start web view if we do not find any chrome package
            openWebViewActivity(activity, url)
        }
    }

    /**
     * The approach used here is to query the Package Manager for applications that support a generic “http” intent.
     * Those applications are likely browsers. Then, query for applications that handle intents for the
     * specific URL we want to launch. This will return both browsers and native applications setup to handle that URL.
     * Now, remove all browsers found on the first list from the second list, and we’ll be left only with native apps.
     * If the list is empty, we know there are no native handlers and return false. Otherwise, we launch
     * the intent for the native handler.
     */
    private fun launchNativeBeforeApi30(context: Context, uri: Uri): Boolean {
        val pm: PackageManager = context.packageManager

        // Get all Apps that resolve a generic url
        val browserActivityIntent: Intent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.fromParts("http", "", null))
        val genericResolvedList: Set<String> = extractPackageNames(pm.queryIntentActivities(browserActivityIntent, 0))

        // Get all apps that resolve the specific Url
        val specializedActivityIntent: Intent = Intent(Intent.ACTION_VIEW, uri)
            .addCategory(Intent.CATEGORY_BROWSABLE)
        val resolvedSpecializedList = extractPackageNames(pm.queryIntentActivities(specializedActivityIntent, 0))

        // Keep only the Urls that resolve the specific, but not the generic urls.
        resolvedSpecializedList.removeAll(genericResolvedList)
        resolvedSpecializedList.remove(context.packageName)

        // If the list is empty, no native app handlers were found.
        if (resolvedSpecializedList.isEmpty()) {
            return false
        }

        // We found native handlers. Launch the Intent.
        specializedActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(specializedActivityIntent)
        return true
    }

    private fun extractPackageNames(queryIntentActivities: List<ResolveInfo>): MutableSet<String> {
        if (queryIntentActivities.isEmpty()) return mutableSetOf()

        val packageNameSet = mutableSetOf<String>()
        queryIntentActivities.forEach {
            packageNameSet.add(it.activityInfo.packageName)
        }

        return packageNameSet
    }

    private fun isThirdPartyUrl(url: String): Boolean {
        val uri = Uri.parse(Uri.decode(url))
        val host = uri.host ?: ""
        if (host == Uri.parse(WebExperiment.WEBVIEW_LIBRARY_URL).host) {
            return false
        }

        val domains = firebaseRemoteConfig.getString(WebViewUtils.CONFIG_WHITELISTED_DOMAINS)

        val domainsArray = if (domains.isNotBlank()) {
            domains.split(",")
        } else {
            WebViewUtils.DEFAULT_WHITELISTED_DOMAINS.split(",")
        }

        domainsArray.forEach {
            if (host.contains(it, true)) {
                return false
            }
        }
        return true
    }
}
