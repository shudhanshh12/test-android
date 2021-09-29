package `in`.okcredit.web_features.cash_counter

import WebInterface
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.web_features.BuildConfig
import `in`.okcredit.web_features.R
import `in`.okcredit.web_features.databinding.CashCounterScreenBinding
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.shortToast

class CashCounterFragment :
    BaseFragment<CashCounterContract.State, CashCounterContract.ViewEvent, CashCounterContract.Intent>("CashCounterScreen"),
    WebInterface.Listener {

    lateinit var binding: CashCounterScreenBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = CashCounterScreenBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView()
    }

    override fun onBackPressed(): Boolean {
        if (isStateInitialized() && getCurrentState().isLoading.not()) {
            binding.webView.evaluateJavascript("javascript:webViewGoBack()") {}
            return true
        }
        return super.onBackPressed()
    }

    private fun initWebView() {
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                addJavascriptInterface(WebInterface(this@CashCounterFragment), WebInterface.JAVASCRIPT_WEB_INTERFACE)
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    context?.let {
                        pushIntent(CashCounterContract.Intent.ShowError(it.getString(R.string.err_loading_page)))
                    }
                }
            }
            webChromeClient = object : WebChromeClient() {
            }
            loadUrl(BuildConfig.CASH_COUNTER_URL)
        }
    }

    override fun loadIntent(): UserIntent {
        return CashCounterContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: CashCounterContract.State) {}

    override fun handleViewEvent(event: CashCounterContract.ViewEvent) {
        when (event) {
            CashCounterContract.ViewEvent.WebPageLoaded -> setWebData()
            CashCounterContract.ViewEvent.ShowError -> showError()
        }
    }

    private fun showError() = context?.shortToast(resources.getString(R.string.err_default))

    private fun setWebData() {
        setMerchantId(getCurrentState().merchantId)
        setAuthToken(getCurrentState().authToken)
    }

    private fun setMerchantId(merchantId: String) =
        binding.webView.evaluateJavascript("javascript:setMerchantId('$merchantId')") {}

    private fun setAuthToken(authToken: String) =
        binding.webView.evaluateJavascript("javascript:setAuthToken('$authToken')") {}

    override fun goBack() {
        activity?.finish()
    }

    override fun onPageLoaded() {
        pushIntent(CashCounterContract.Intent.WebPageLoaded)
    }

    override fun getMerchantId(): String? {
        return getCurrentState().merchantId
    }

    override fun getAuthToken(): String? {
        return getCurrentState().authToken
    }
}
