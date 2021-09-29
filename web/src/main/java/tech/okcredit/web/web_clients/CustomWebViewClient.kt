package tech.okcredit.web.web_clients

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import tech.okcredit.base.exceptions.ExceptionUtils
import tech.okcredit.web.WebTracker
import tech.okcredit.web.ui.WebViewActivity
import tech.okcredit.web.utils.WebViewUtils.isAcceptableFailure
import tech.okcredit.web.web_interfaces.WebAppInterface
import timber.log.Timber
import java.lang.ref.WeakReference

class CustomWebViewClient internal constructor(
    private val activity: WeakReference<WebViewActivity>,
    startBootTime_: Long,
    webTracker_: WebTracker,
) : WebViewClient() {

    private var startLoadTime: Long? = null
    private val startBootTime = startBootTime_
    private val webTracker = webTracker_

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedError(webview: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        val isAcceptableFailure = isAcceptableFailure(webview?.url, request?.url)

        try {
            if (error?.errorCode == ERROR_HOST_LOOKUP && !isAcceptableFailure) {
                webview?.stopLoading()
                activity.get()?.manageWebViewContainer(false)
            }
        } catch (e: Exception) {
            ExceptionUtils.logException("WebViewActivity: CustomWebViewClient onReceivedError", e)
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        startLoadTime = System.currentTimeMillis()
        Timber.d(WebViewActivity.TAG_ANDROID_WEBVIEW, "Page started => $url")
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        WebAppInterface.injectJavascript(view)
        startLoadTime?.let {
            val loadTime = System.currentTimeMillis() - it
            val bootTime = System.currentTimeMillis() - startBootTime
            Timber.d("time: %d, %d", loadTime, bootTime)
            webTracker.trackBootUpTime(
                bootTime,
                WebTracker.Experiments.UNKNOWN, "", url
            )
            webTracker.trackPageLoadTime(
                loadTime,
                WebTracker.Experiments.UNKNOWN, "", url
            )
        }
        super.onPageFinished(view, url)
    }

    override fun onPageCommitVisible(view: WebView, url: String) {
        activity.get()?.hideLoading()
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        Timber.d(WebViewActivity.TAG_ANDROID_WEBVIEW, "Url => ${view.url}")

        val loadInBrowser = with(request.url) {
            getQueryParameter("browser")?.toBoolean()
                ?: path?.startsWith("upi://")
        }

        if (loadInBrowser == true) {
            kotlin.runCatching {
                val intent = Intent(Intent.ACTION_VIEW, request.url)
                activity.get()?.startActivity(intent)
            }
        }
        return loadInBrowser == true
    }
}
