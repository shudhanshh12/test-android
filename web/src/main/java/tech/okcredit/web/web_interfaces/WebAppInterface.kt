package tech.okcredit.web.web_interfaces

import android.webkit.JavascriptInterface
import android.webkit.WebView
import tech.okcredit.web.utils.isThirdPartyUrl

class WebAppInterface(
    private val listener: WebViewCallbackListener,
    private val dataLayerListener: WebViewDataLayerCallbackListener,
) {

    @JavascriptInterface
    fun call(phone: String) {
        listener.call(phone)
    }

    @JavascriptInterface
    fun shareOnWhatsApp(msg: String, phone: String, url: String?) {
        listener.shareOnWhatsApp(msg, phone, url)
    }

    @JavascriptInterface
    fun shareOnAnyApp(msg: String, imageUrl: String?) {
        listener.shareOnAnyApp(msg, imageUrl)
    }

    @JavascriptInterface
    fun backPress() {
        listener.backPress()
    }

    @JavascriptInterface
    fun pageBack() {
        listener.pageBack()
    }

    @JavascriptInterface
    fun getMixpanelProps(): String {
        return dataLayerListener.getMixpanelProps()
    }

    @JavascriptInterface
    fun makeToast(msg: String) {
        listener.makeToast(msg)
    }

    @JavascriptInterface
    fun debug(msg: String) {
        listener.debug(msg)
    }

    @JavascriptInterface
    fun navigate(deepLink: String) {
        listener.navigate(deepLink)
    }

    @JavascriptInterface
    fun requestLocationPermission() {
        listener.requestLocationPermission()
    }

    @JavascriptInterface
    fun requestSmsPermission() {
        listener.requestSmsPermission()
    }

    @JavascriptInterface
    fun stopListeningSms() {
        listener.stopListeningSms()
    }

    @JavascriptInterface
    fun getLocation(): String {
        return listener.getLocation()
    }

    @JavascriptInterface
    fun getAndroidVersionCode(): String {
        return dataLayerListener.getAndroidVersionCode()
    }

    @JavascriptInterface
    fun getLanguage(): String {
        return dataLayerListener.getLanguage()
    }

    @JavascriptInterface
    fun getAuthToken(): String {
        return dataLayerListener.getAuthToken()
    }

    @JavascriptInterface
    fun getContacts(): String {
        return dataLayerListener.getContacts()
    }

    @JavascriptInterface
    fun getMerchantId(): String {
        return dataLayerListener.getMerchantId()
    }

    @JavascriptInterface
    fun isFeatureEnabled(feature: String): Boolean {
        return dataLayerListener.isFeatureEnabled(feature)
    }

    @JavascriptInterface
    fun isExperimentEnabled(experiment: String): Boolean {
        return dataLayerListener.isExperimentEnabled(experiment)
    }

    @JavascriptInterface
    fun getExperimentVariant(experiment: String): String {
        return dataLayerListener.getExperimentVariant(experiment)
    }

    @JavascriptInterface
    fun getVariantConfigurations(experiment: String): String {
        return dataLayerListener.getVariantConfigurations(experiment)
    }

    @JavascriptInterface
    fun syncDynamicComponent() {
        return dataLayerListener.syncDynamicComponent()
    }

    companion object {
        const val JAVASCRIPT_WEB_INTERFACE = "Android"

        fun injectJavascript(view: WebView?) {

            view?.evaluateJavascript(
                "javascript: window.androidObj.backPress = function() { $JAVASCRIPT_WEB_INTERFACE.backPress() }",
                null
            )

            view?.evaluateJavascript(
                "javascript: window.androidObj.pageBack = function() { $JAVASCRIPT_WEB_INTERFACE.pageBack() }",
                null
            )

            view?.evaluateJavascript(
                "javascript: window.androidObj.debug = function(message) { $JAVASCRIPT_WEB_INTERFACE.debug(message) }",
                null
            )

            if (view?.url?.isThirdPartyUrl()?.not() == true) {
                view.evaluateJavascript(
                    "javascript: window.androidObj.getMixpanelProps = function() { return $JAVASCRIPT_WEB_INTERFACE.getMixpanelProps() }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.shareOnWhatsApp = function(message, phone, url) { $JAVASCRIPT_WEB_INTERFACE.shareOnWhatsApp(message, phone, url) }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.shareOnAnyApp = function(message, imageUrl) { $JAVASCRIPT_WEB_INTERFACE.shareOnAnyApp(message, imageUrl) }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.getLanguage = function() { return $JAVASCRIPT_WEB_INTERFACE.getLanguage() }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.makeToast = function(message) { $JAVASCRIPT_WEB_INTERFACE.makeToast(message) }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.navigate = function(deepLink) { $JAVASCRIPT_WEB_INTERFACE.navigate(deepLink) }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.requestLocationPermission = function() { $JAVASCRIPT_WEB_INTERFACE.requestLocationPermission() }",
                    null
                )
                view.evaluateJavascript(
                    "javascript: window.androidObj.requestSmsPermission = function() { $JAVASCRIPT_WEB_INTERFACE.requestSmsPermission() }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.stopListeningSms = function() { $JAVASCRIPT_WEB_INTERFACE.stopListeningSms() }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.getLocation = function() { return $JAVASCRIPT_WEB_INTERFACE.getLocation() }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.getAuthToken = function() { return $JAVASCRIPT_WEB_INTERFACE.getAuthToken() }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.getAndroidVersionCode = function() { return $JAVASCRIPT_WEB_INTERFACE.getAndroidVersionCode() }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.syncEverything = function() { $JAVASCRIPT_WEB_INTERFACE.syncEverything() }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.call = function(phone) { $JAVASCRIPT_WEB_INTERFACE.call(phone) }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.getContacts = function() { return $JAVASCRIPT_WEB_INTERFACE.getContacts() }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.getMerchantId = function() { return $JAVASCRIPT_WEB_INTERFACE.getMerchantId() }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.isFeatureEnabled = function(feature) { return $JAVASCRIPT_WEB_INTERFACE.isFeatureEnabled(feature) }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.isExperimentEnabled = function(experiment) { return $JAVASCRIPT_WEB_INTERFACE.isExperimentEnabled(experiment) }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.getExperimentVariant = function(experiment) { return $JAVASCRIPT_WEB_INTERFACE.getExperimentVariant(experiment) }",
                    null
                )

                view.evaluateJavascript(
                    "javascript: window.androidObj.getVariantConfigurations = function(experiment) { return $JAVASCRIPT_WEB_INTERFACE.getVariantConfigurations(experiment) }",
                    null
                )

                view.evaluateJavascript("javascript:setAuthToken($JAVASCRIPT_WEB_INTERFACE.getAuthToken())") {}

                view.evaluateJavascript(
                    "javascript: window.androidObj.syncDynamicComponent = function() { return $JAVASCRIPT_WEB_INTERFACE.syncDynamicComponent() }",
                    null
                )
            }
        }
    }
}
