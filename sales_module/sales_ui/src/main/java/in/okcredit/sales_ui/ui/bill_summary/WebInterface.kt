package `in`.okcredit.sales_ui.ui.bill_summary

import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.joda.time.DateTime

class WebInterface(private val listener: Listener?) {

    interface Listener {
        fun onEditMerchant()
        fun onEditBillingName(buyerName: String?, buyerMobile: String?)
        fun openDatePicker(date: DateTime?)
    }

    @JavascriptInterface
    fun onEditMerchant() {
        listener?.onEditMerchant()
    }

    @JavascriptInterface
    fun onEditBillingName(buyerName: String?, buyerMobile: String?) {
        listener?.onEditBillingName(buyerName, buyerMobile)
    }

    @JavascriptInterface
    fun openDatePicker(date: String?) {
        date?.let {
            val d = DateTime(it.toLong() * 1000)
            listener?.openDatePicker(d)
        }
    }

    companion object {
        const val JAVASCRIPT_WEB_INTERFACE = "Android"

        fun injectJavascript(view: WebView?) {
            view?.evaluateJavascript(
                "javascript: " + "window.androidObj.onEditMerchant = function() { " + JAVASCRIPT_WEB_INTERFACE + ".onEditMerchant() }",
                null
            )
        }
    }
}
