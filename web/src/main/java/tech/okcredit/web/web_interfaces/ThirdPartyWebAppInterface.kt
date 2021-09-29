package tech.okcredit.web.web_interfaces

import android.webkit.JavascriptInterface

class ThirdPartyWebAppInterface constructor(
    private val listener: WebViewCallbackListener,
) {

    @JavascriptInterface
    fun backPress() {
        listener.backPress()
    }

    @JavascriptInterface
    fun pageBack() {
        listener.pageBack()
    }

    @JavascriptInterface
    fun debug(msg: String) {
        listener.debug(msg)
    }
}
