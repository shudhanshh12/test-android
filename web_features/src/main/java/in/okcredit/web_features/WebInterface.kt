import android.webkit.JavascriptInterface

class WebInterface(private val listener: Listener?) {

    interface Listener {
        fun goBack()
        fun onPageLoaded()
        fun getMerchantId(): String?
        fun getAuthToken(): String?
    }

    @JavascriptInterface
    fun goBack() {
        listener?.goBack()
    }

    @JavascriptInterface
    fun onPageLoaded() {
        listener?.onPageLoaded()
    }

    @JavascriptInterface
    fun getMerchantId(): String? {
        return listener?.getMerchantId()
    }

    @JavascriptInterface
    fun getAuthToken(): String? {
        return listener?.getAuthToken()
    }

    companion object {
        const val JAVASCRIPT_WEB_INTERFACE = "Android"
    }
}
