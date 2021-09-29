package `in`.okcredit.payment.ui.juspay

import `in`.juspay.hypersdk.core.MerchantViewType
import `in`.juspay.hypersdk.data.JuspayResponseHandler
import `in`.juspay.hypersdk.ui.HyperPaymentsCallback
import `in`.juspay.hypersdk.ui.JuspayWebView
import `in`.juspay.services.HyperServices
import `in`.okcredit.payment.utils.JuspayPayloadUtils
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentActivity
import org.json.JSONObject
import tech.okcredit.android.base.extensions.ifLet
import java.util.*

/**
 *[HyperServiceHolder] This class contains variable for juspay initialisation and process so before starting any new
 * screen or instance for payment do reset params as it provides
 * params for application life time which may cause unexpected bahaviour so better to reset before every use
 */
class HyperServiceHolder :
    HyperPaymentsCallback {
    enum class EventsType {
        OnEvent, OnStartWaitingDialogCreated, OnWebViewReady, GetMerchantView
    }

    private var hs: HyperServices? = null
    private var isJuspayInitiated = false
    private var isJuspayInitiateStarted = false
    private var isPrefetchDone = false
    private var callback: HyperPaymentsCallback? = null

    private val queue: Queue<QueuedEvents> = LinkedList()

    val isInitiated: Boolean
        get() = isJuspayInitiated

    private val isInitiateStarted: Boolean
        get() = isJuspayInitiateStarted

    val isJuspayPrefetchDone: Boolean
        get() = isPrefetchDone

    fun getHyperServiceInstance(fragmentActivity: FragmentActivity?): HyperServices {
        if (hs == null && fragmentActivity != null) {
            hs = HyperServices(fragmentActivity)
        }
        return hs!!
    }

    fun prefetch(activity: FragmentActivity, service: String) {
        isPrefetchDone = true
        HyperServices.preFetch(activity, JuspayPayloadUtils.constructPrefetchPayload(service))
    }

    fun initiate(jsonObj: JSONObject?, activity: FragmentActivity) {
        ifLet(activity, jsonObj) { fragmentActivity, jsonObject ->
            if (!isInitiateStarted)
                getHyperServiceInstance(activity).initiate(fragmentActivity, jsonObject, this)
            isJuspayInitiateStarted = true
        }
    }

    fun process(jsonObj: JSONObject?, activity: FragmentActivity) {
        ifLet(activity, jsonObj) { fragmentActivity, jsonObject ->
            getHyperServiceInstance(fragmentActivity).process(fragmentActivity, jsonObject)
        }
    }

    fun setCallback(hyperPaymentsCallback: HyperPaymentsCallback) {
        callback = hyperPaymentsCallback
        runQueueEvents()
    }

    override fun onStartWaitingDialogCreated(parent: View?) {
        if (callback != null) callback!!.onStartWaitingDialogCreated(
            parent
        ) else {
            val qEvent = QueuedEvents()
            queue.add(qEvent)
            qEvent.eventType =
                EventsType.OnStartWaitingDialogCreated
            qEvent.parent = parent
        }
    }

    override fun onWebViewReady(webView: JuspayWebView) {
        if (callback != null) callback!!.onWebViewReady(webView) else {
            val qEvent = QueuedEvents()
            queue.add(qEvent)
            qEvent.eventType =
                EventsType.OnWebViewReady
            qEvent.jpWebView = webView
        }
    }

    override fun onEvent(event: JSONObject, handler: JuspayResponseHandler) {
        val event1 = event.optString("event", "")
        if (event1 == "initiate_result") {
            isJuspayInitiated = true
        }
        if (callback != null) {
            callback!!.onEvent(event, handler)
        } else {
            val qEvent = QueuedEvents()
            queue.add(qEvent)
            qEvent.event = event
            qEvent.handler = handler
            qEvent.eventType = EventsType.OnEvent
        }
    }

    override fun getMerchantView(parent: ViewGroup?, viewType: MerchantViewType?): View? {
        if (callback != null) callback!!.getMerchantView(parent, viewType) else {
            val qEvent = QueuedEvents()
            queue.add(qEvent)
            qEvent.eventType = EventsType.GetMerchantView
            qEvent.viewGroup = parent
            qEvent.viewType = viewType
        }
        return null
    }

    override fun createJuspaySafeWebViewClient(): WebViewClient? {
        return null
    }

    private fun runQueueEvents() {
        val head = queue.poll()
        if (head != null) {
            when (head.eventType) {
                EventsType.OnEvent -> if (callback != null) callback!!.onEvent(
                    head.event,
                    head.handler
                )
                EventsType.OnStartWaitingDialogCreated -> if (callback != null) callback!!.onStartWaitingDialogCreated(
                    head.parent
                )
                EventsType.OnWebViewReady -> if (callback != null) callback!!.onWebViewReady(
                    head.jpWebView
                )
                EventsType.GetMerchantView -> if (callback != null) callback!!.getMerchantView(
                    head.viewGroup,
                    head.viewType
                )
                else -> {
                }
            }
            runQueueEvents()
        }
    }

    fun resetParams() {
        hs?.terminate()
        hs = null
        isJuspayInitiated = false
        isJuspayInitiateStarted = false
        isPrefetchDone = false
        callback = null
    }
}

internal class QueuedEvents {
    var event: JSONObject? = null
    var handler: JuspayResponseHandler? = null
    var eventType: HyperServiceHolder.EventsType? = null
    var viewGroup: ViewGroup? = null
    var parent: View? = null
    var jpWebView: JuspayWebView? = null
    var viewType: MerchantViewType? = null
}
