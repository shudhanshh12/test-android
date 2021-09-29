package `in`.okcredit.payment.ui.juspay.juspayWorkerFragment

import `in`.juspay.hypersdk.core.MerchantViewType
import `in`.juspay.hypersdk.data.JuspayResponseHandler
import `in`.juspay.hypersdk.ui.HyperPaymentsCallback
import `in`.juspay.hypersdk.ui.JuspayWebView
import `in`.okcredit.payment.BuildConfig.JUSPAY_SERVICE
import `in`.okcredit.payment.BuildConfig.JUSPAY_SERVICE_UPI_PSP
import `in`.okcredit.payment.R
import `in`.okcredit.payment.contract.JuspayCallbackListener
import `in`.okcredit.payment.contract.JuspayErrorType
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import `in`.okcredit.payment.ui.juspay.HyperServiceHolder
import `in`.okcredit.payment.ui.juspay.analytics.JuspayEventTracker
import `in`.okcredit.payment.ui.juspay.analytics.JuspayEventTracker.Companion.WORKER_FRAGMENT
import `in`.okcredit.payment.ui.juspay.juspayWorkerFragment.JuspayWorkerFragment.Companion.initiateJuspayWorkerFragment
import `in`.okcredit.payment.ui.juspay.juspayWorkerFragment.JuspayWorkerFragment.Companion.isBackPressed
import `in`.okcredit.payment.utils.JuspayPayloadUtils
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.annotation.Nullable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.addFragmentToFragmentManager
import tech.okcredit.android.base.extensions.disableScreanCapture
import javax.inject.Inject

/**
 * This fragment is a worker fragment to initiate and process juspay on calling page.Reason to create a worker fragment is to remove
 * Ui screen which is hampering UX as after coming from Usecase screen it was showing amount editing creen again which is not required.
 * Function need to call are like below:
 * [initiateJuspayWorkerFragment] is needed to start worker fragment and initialisation of Juspay.(call after view is created)
 * [startJuspaySdk] is needed to open juspay sdk It needs payment id so make sure once get getPaymentAttributes via callbacks methods than only
 * show edit amount page or proceed button should enabled after that only.
 * [setJuspayCallbackListener] this listener need to be implement at page where callbacks are needed don't set this listener more than once otherwise
 * callback will be replaced to current one and older one will not events.
 * [isBackPressed] This will be used to handle juspay page back press when this returns true return true in backpressed function of current page
 * otherwise use default
 */
class JuspayWorkerFragment :
    BaseFragment<JuspayWorkerContract.State, JuspayWorkerContract.ViewEvent, JuspayWorkerContract.Intent>("JuspayWorkerFragment") {

    private val initiateJuspayPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    internal var juspayEventCallbackListener: JuspayCallbackListener? = null
    internal var paymentLinkId: String = ""
    internal var amountProvided: Long = 0L

    // nextStateToExecute this params tells which step to execute
    // ex : when initiate is not done and proceed is clicked for payment we wait till initiate finish and
    // help of this param we start process call directly
    internal var nextStateToExecute = JuspayWorkerContract.JuspayWorkerState.JUSPAY_INITIATE_STARTED
    internal var juspayService = JUSPAY_SERVICE
    internal var pspFeature = JuspayWorkerContract.JuspayPspFeature.PROFILE
    private var incomingIntentData = ""
    internal var gatewayTxnId = ""
    internal var gatewayRefId = ""

    @Inject
    lateinit var juspayEventTracker: Lazy<JuspayEventTracker>

    @Inject
    lateinit var hyperServicesHolder: Lazy<HyperServiceHolder>

    companion object {
        private const val ARG_JUSPAY_SERVICE: String = "juspay_service"
        private const val ARG_PSP_FEATURE: String = "psp_feature"
        private const val ARG_NEXT_STATE_TO_EXECUTE: String = "next_state_to_execute"
        private const val ARG_INCOMING_INTENT: String = "incoming_intent"
        private const val ARG_GTW_TXN_ID: String = "gtw_txn_id"
        private const val ARG_GTW_REF_ID: String = "gtw_ref_id"
        private const val TAG = "Juspay worker fragment"

        fun initiateJuspayWorkerFragment(
            activity: FragmentActivity,
        ) {
            val frag = JuspayWorkerFragment()
            activity.supportFragmentManager.addFragmentToFragmentManager(
                fragment = frag,
                tag = TAG,
                addToBackStack = true
            )
        }

        fun startJuspaySdk(activity: FragmentActivity, linkId: String, amount: Long) {
            val juspayWorkerFragment = activity.supportFragmentManager.findFragmentByTag(TAG) as JuspayWorkerFragment?
            juspayWorkerFragment?.let {
                it.paymentLinkId = linkId
                it.amountProvided = amount
                if (it.hyperServicesHolder.get().isInitiated) {
                    it.startJuspaySdk(linkId, amount)
                } else {
                    it.nextStateToExecute = JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_STARTED
                }
            }
        }

        fun startPspProfileManagementSdk(
            activity: FragmentActivity,
        ) {

            val frag = JuspayWorkerFragment()
            val bundle = Bundle()
            bundle.apply {
                putString(ARG_JUSPAY_SERVICE, JUSPAY_SERVICE_UPI_PSP)
                putInt(ARG_PSP_FEATURE, JuspayWorkerContract.JuspayPspFeature.PROFILE.value)
                putInt(
                    ARG_NEXT_STATE_TO_EXECUTE,
                    JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_STARTED.value
                )
            }
            frag.arguments = bundle
            activity.supportFragmentManager.addFragmentToFragmentManager(
                fragment = frag,
                tag = TAG,
                addToBackStack = true
            )
        }

        fun startPspIncomingIntentSdk(
            activity: FragmentActivity,
            intentData: String,
        ) {
            val frag = JuspayWorkerFragment()
            val bundle = Bundle()
            bundle.apply {
                putString(ARG_JUSPAY_SERVICE, JUSPAY_SERVICE_UPI_PSP)
                putInt(ARG_PSP_FEATURE, JuspayWorkerContract.JuspayPspFeature.INCOMING_INTENT.value)
                putInt(
                    ARG_NEXT_STATE_TO_EXECUTE,
                    JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_STARTED.value
                )
                putString(ARG_INCOMING_INTENT, intentData)
            }
            frag.arguments = bundle

            activity.supportFragmentManager.addFragmentToFragmentManager(
                fragment = frag,
                tag = TAG,
                addToBackStack = true
            )
        }

        fun startApproveCollectRequestSdk(
            activity: FragmentActivity,
            gatewayTransactionId: String,
            gatewayReferenceId: String,
        ) {
            val frag = JuspayWorkerFragment()

            val bundle = Bundle()
            bundle.apply {
                putString(ARG_JUSPAY_SERVICE, JUSPAY_SERVICE_UPI_PSP)
                putInt(ARG_PSP_FEATURE, JuspayWorkerContract.JuspayPspFeature.APPROVE_COLLECT_REQUEST.value)
                putInt(
                    ARG_NEXT_STATE_TO_EXECUTE,
                    JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_STARTED.value
                )
                putString(ARG_GTW_TXN_ID, gatewayTransactionId)
                putString(ARG_GTW_REF_ID, gatewayReferenceId)
            }
            frag.arguments = bundle
            activity.supportFragmentManager.addFragmentToFragmentManager(
                fragment = frag,
                tag = TAG,
                addToBackStack = true
            )
        }

        fun isBackPressed(activity: FragmentActivity): Boolean {
            val juspayWorkerFragment = activity.supportFragmentManager.findFragmentByTag(TAG) as JuspayWorkerFragment?
            juspayWorkerFragment?.let { frag ->
                if (frag.activity == null) return false
                frag.requireActivity().let {
                    return frag.hyperServicesHolder.get().getHyperServiceInstance(it).onBackPressed()
                }
            }
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requireActivity().window.disableScreanCapture()
        super.onCreate(savedInstanceState)
        setArgumentsData()
        prefetchJuspay(requireActivity(), juspayService)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is JuspayCallbackListener) {
            juspayEventCallbackListener = context
        }
    }

    private fun setArgumentsData() {
        arguments?.let {
            juspayService = it.getString(ARG_JUSPAY_SERVICE, JUSPAY_SERVICE)
            pspFeature = JuspayWorkerContract.JuspayPspFeature.fromValue(it.getInt(ARG_PSP_FEATURE))
            nextStateToExecute = JuspayWorkerContract.JuspayWorkerState.fromValue(it.getInt(ARG_NEXT_STATE_TO_EXECUTE))
            incomingIntentData = it.getString(ARG_INCOMING_INTENT, "")
            gatewayTxnId = it.getString(ARG_GTW_TXN_ID, "")
            gatewayRefId = it.getString(ARG_GTW_REF_ID, "")
        }
    }

    private fun prefetchJuspay(fragmentActivity: FragmentActivity, service: String) {
        if (!hyperServicesHolder.get().isJuspayPrefetchDone) {
            juspayEventTracker.get().trackEventJuspayPrefetchCalled()
            hyperServicesHolder.get().prefetch(fragmentActivity, service)
        }
    }

    override fun onResume() {
        super.onResume()
        initHyperServicesH()
    }

    private fun initHyperServicesH() {
        if (!hyperServicesHolder.get().isInitiated) {
            initiateJuspayPublishSubject.onNext(Unit)
        }
    }

    override fun loadIntent(): UserIntent {
        return JuspayWorkerContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            initiateJuspayPublishSubject
                .map {
                    JuspayWorkerContract.Intent.Load
                }
        )
    }

    override fun render(state: JuspayWorkerContract.State) {
        when (state.juspayWorkerState) {
            JuspayWorkerContract.JuspayWorkerState.JUSPAY_NO_STATE -> {
            }
            JuspayWorkerContract.JuspayWorkerState.JUSPAY_INITIATE_STARTED -> {
                state.getJuspayInitiateResponse?.let {
                    initiateJuspaySDK(it)
                }
            }
            JuspayWorkerContract.JuspayWorkerState.JUSPAY_INITIATE_FINISHED -> {
            }
            JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_STARTED -> {
                state.getJuspayProcessResponse?.let {
                    processJuspaySdk(it)
                }
            }
            JuspayWorkerContract.JuspayWorkerState.JUSPAY_SDK_OPENED -> {
                juspayEventCallbackListener?.onJuspaySdkOpened()
            }
            JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_FINISHED -> {
                pushIntent(JuspayWorkerContract.Intent.SetJuspayWorkerState(JuspayWorkerContract.JuspayWorkerState.JUSPAY_NO_STATE))
                juspayEventCallbackListener?.onJuspaySdkClosed(
                    state.getPaymentAttributesResponse?.paymentId ?: "",
                    state.getPaymentAttributesResponse?.pollingType ?: "",
                    amountProvided,
                    getCurrentState().juspayErrorType
                )
            }
            JuspayWorkerContract.JuspayWorkerState.API_ERROR -> {
                pushIntent(JuspayWorkerContract.Intent.SetJuspayWorkerState(JuspayWorkerContract.JuspayWorkerState.JUSPAY_NO_STATE))
                juspayEventCallbackListener?.onApiFailure(state.apiErrorType)
            }
        }
    }

    private fun initiateJuspaySDK(juspayInitiateResponse: PaymentApiMessages.GetJuspayAttributesResponse) {
        initCallback()
        val initiatePayload: JSONObject =
            JuspayPayloadUtils.generateJuspayInitiatePayload(
                juspayInitiateResponse.signaturePayload,
                juspayInitiateResponse.signature,
                boldFont = R.font.bold,
                regularFont = R.font.app_font,
                juspayService
            )
        juspayEventTracker.get().trackEventJuspayInitiateCalled(WORKER_FRAGMENT)
        hyperServicesHolder.get().initiate(initiatePayload, requireActivity())
    }

    fun startJuspaySdk(linkId: String, amount: Long) {
        // GetPaymentAttribute should be push once juspay worker fragment is resumed
        this.lifecycleScope.launchWhenResumed {
            pushIntent(JuspayWorkerContract.Intent.GetPaymentAttribute(linkId, amount))
        }
    }

    private fun processJuspaySdk(getJuspayProcessResponse: PaymentApiMessages.GetJuspayAttributesResponse) {
        val payload: JSONObject = JuspayPayloadUtils.generateProcessPayload(
            getJuspayProcessResponse,
            getCurrentState().getPaymentAttributesResponse?.quickPayEnabled ?: false
        )
        hyperServicesHolder.get().process(payload, requireActivity())
    }

    internal fun processPspSdk() {
        val payload: JSONObject = when (pspFeature) {
            JuspayWorkerContract.JuspayPspFeature.PROFILE -> {
                getCurrentState().let {
                    JuspayPayloadUtils.constructPspManagementPayload(
                        it.getJuspayInitiateResponse?.signature ?: "",
                        it.getJuspayInitiateResponse?.signaturePayload ?: ""
                    )
                }
            }
            JuspayWorkerContract.JuspayPspFeature.INCOMING_INTENT -> {
                getCurrentState().let {
                    JuspayPayloadUtils.constructPspIntentPayload(
                        incomingIntentData,
                        it.getJuspayInitiateResponse?.signature ?: "",
                        it.getJuspayInitiateResponse?.signaturePayload ?: ""
                    )
                }
            }
            JuspayWorkerContract.JuspayPspFeature.APPROVE_COLLECT_REQUEST -> {
                getCurrentState().let {
                    JuspayPayloadUtils.constructPspApproveCollectRequestPayload(
                        gatewayTxnId,
                        gatewayRefId,
                        it.getJuspayInitiateResponse?.signature ?: "",
                        it.getJuspayInitiateResponse?.signaturePayload ?: ""
                    )
                }
            }
        }
        hyperServicesHolder.get().process(payload, requireActivity())
    }

    override fun handleViewEvent(event: JuspayWorkerContract.ViewEvent) {}

    private fun initCallback() {
        hyperServicesHolder.get().setCallback(generateHyperPaymentsCallback())
    }

    private fun generateHyperPaymentsCallback(): HyperPaymentsCallback {
        return object : HyperPaymentsCallback {
            override fun onStartWaitingDialogCreated(@Nullable view: View?) {
                // do nothing
            }

            override fun onWebViewReady(juspayWebView: JuspayWebView) {
                // do nothing
            }

            override fun getMerchantView(p0: ViewGroup?, p1: MerchantViewType?): View? {
                return null
            }

            override fun createJuspaySafeWebViewClient(): WebViewClient? {
                return null
            }

            override fun onEvent(
                jsonObject: JSONObject,
                juspayResponseHandler: JuspayResponseHandler,
            ) {
                try {
                    val event = jsonObject.getString("event")
                    val error = jsonObject.optBoolean("error")

                    when (event) {
                        "hide_loader" -> {
                            juspayEventTracker.get().trackEventJuspayHideLoaderCalled()
                            pushIntent(JuspayWorkerContract.Intent.SetJuspayWorkerState(JuspayWorkerContract.JuspayWorkerState.JUSPAY_SDK_OPENED))
                        }
                        "initiate_result" -> {
                            if (nextStateToExecute == JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_STARTED) {
                                if (juspayService == JUSPAY_SERVICE)
                                    startJuspaySdk(paymentLinkId, amountProvided)
                                else if (juspayService == JUSPAY_SERVICE_UPI_PSP) {
                                    processPspSdk()
                                }
                            } else pushIntent(JuspayWorkerContract.Intent.SetJuspayWorkerState(JuspayWorkerContract.JuspayWorkerState.JUSPAY_INITIATE_FINISHED))
                        }
                        "process_result" -> {

                            val payload = jsonObject.optJSONObject("payload")
                            val payloadStatus = payload?.optString("status")

                            if (error) {
                                val errorCode = jsonObject.optString("errorCode")
                                val errorMessage = jsonObject.optString("errorMessage")

                                when (payloadStatus) {
                                    "backpressed" -> {
                                        juspayEventCallbackListener?.onJuspaySdkBackpressed()
                                    }
                                    else -> {
                                        juspayEventTracker.get()
                                            .trackEventJuspayProcessError(
                                                payload?.toString() ?: "",
                                                errorMessage,
                                                errorCode
                                            )
                                        if (errorCode == JuspayErrorType.JP_001.value || errorCode == JuspayErrorType.JP_002.value || errorCode == JuspayErrorType.JP_005.value) {
                                            pushIntent(
                                                JuspayWorkerContract.Intent.SetJuspayWorkerState(
                                                    JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_FINISHED,
                                                    JuspayErrorType.fromValue(errorCode)
                                                )
                                            )
                                        } else {
                                            pushIntent(
                                                JuspayWorkerContract.Intent.SetJuspayWorkerState(
                                                    JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_FINISHED
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {
                                pushIntent(JuspayWorkerContract.Intent.SetJuspayWorkerState(JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_FINISHED))
                            }
                        }
                    }
                } catch (e: Exception) {
                    RecordException.recordException(e)
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDetach() {
        juspayEventCallbackListener = null
        super.onDetach()
    }

    override fun onDestroy() {
        hyperServicesHolder.get().resetParams()
        super.onDestroy()
    }
}
