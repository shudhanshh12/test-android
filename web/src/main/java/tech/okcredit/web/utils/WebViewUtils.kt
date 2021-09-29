package tech.okcredit.web.utils

import `in`.okcredit.web.WebExperiment
import android.net.Uri
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

object WebViewUtils {

    const val CONFIG_WHITELISTED_DOMAINS = "whitelisted_domains"
    const val DEFAULT_WHITELISTED_DOMAINS = "okcredit.in,okrelief.in,okshop.in,okstaff.in,okcr.in"

    // public so other WebviewClient can also access it
    @Suppress("SpellCheckingInspection")
    private val ACCEPTABLE_NETWORK_FAILURE_WHITELIST = hashSetOf(
        "gamezop.com",
    )

    fun isAcceptableFailure(webviewUrl: String?, requestUrl: Uri?): Boolean {
        val urlHost = webviewUrl?.let { Uri.parse(it) }?.host
        val reqHost = requestUrl?.host
        return urlHost != reqHost &&
            urlHost in ACCEPTABLE_NETWORK_FAILURE_WHITELIST
    }
}

fun String.isThirdPartyUrl(): Boolean {
    val uri = Uri.parse(Uri.decode(this))
    val host = uri.host ?: ""
    if (host == Uri.parse(WebExperiment.WEBVIEW_LIBRARY_URL).host) {
        return false
    }

    val domains = FirebaseRemoteConfig.getInstance().getString(WebViewUtils.CONFIG_WHITELISTED_DOMAINS)

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
