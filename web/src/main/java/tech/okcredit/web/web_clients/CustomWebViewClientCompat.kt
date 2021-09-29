package tech.okcredit.web.web_clients

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebViewClientCompat
import tech.okcredit.base.exceptions.ExceptionUtils
import tech.okcredit.web.WebTracker
import tech.okcredit.web.ui.WebViewActivity
import tech.okcredit.web.utils.WebViewUtils
import tech.okcredit.web.web_interfaces.WebAppInterface
import timber.log.Timber
import java.lang.ref.WeakReference

class CustomWebViewClientCompat internal constructor(
    private val activity: WeakReference<WebViewActivity>,
    private val startBootTime: Long,
    private val webTracker: WebTracker,
) : WebViewClientCompat() {

    private var startLoadTime: Long? = null

    override fun onReceivedError(webview: WebView, request: WebResourceRequest, error: WebResourceErrorCompat) {
        val isAcceptableFailure = WebViewUtils.isAcceptableFailure(webview.url, request.url)

        try {
            if (error.errorCode == WebViewClient.ERROR_HOST_LOOKUP && !isAcceptableFailure) {
                webview.stopLoading()
                activity.get()?.manageWebViewContainer(false)
            }
        } catch (e: Exception) {
            ExceptionUtils.logException("WebViewActivity: CustomWebViewClientCompat onReceivedError", e)
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        startLoadTime = System.currentTimeMillis()
        Timber.d(WebViewActivity.TAG_ANDROID_WEBVIEW, "CustomWebViewClientCompat Page started => $url")
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        WebAppInterface.injectJavascript(view)
        val loadTime = System.currentTimeMillis() - startLoadTime!!
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
        super.onPageFinished(view, url)
    }

    override fun onPageCommitVisible(view: WebView, url: String) {
        activity.get()?.hideLoading()
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        Timber.d(WebViewActivity.TAG_ANDROID_WEBVIEW, "CustomWebViewClientCompat Url => ${view.url}")
        return activity.get()?.loadInBrowser(request) ?: false
    }
}
