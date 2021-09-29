package `in`.okcredit.frontend.ui.live_sales

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Screen
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui._dialogs.LinkPayBottomSheetPaymentDialog
import `in`.okcredit.frontend.ui.live_sales.views.TransactionView
import `in`.okcredit.frontend.utils.Utils
import `in`.okcredit.merchant.collection.analytics.CollectionTraces
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.live_sales_fragment.*
import org.joda.time.DateTime
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LiveSalesFragment :
    BaseScreen<LiveSalesContract.State>("LiveSalesScreen"),
    LiveSalesContract.Navigator,
    TransactionView.Listener {

    // intents
    private val transactionClicks: PublishSubject<Pair<String, Long>> = PublishSubject.create()
    private val expandTransactions: PublishSubject<Unit> = PublishSubject.create()
    private val topScrollItemChanged: PublishSubject<DateTime> = PublishSubject.create()
    private val privacyClicks: PublishSubject<Unit> = PublishSubject.create()
    private val updateLastViewedTime: PublishSubject<Unit> = PublishSubject.create()
    internal val hideQrCodeDialogPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    internal var sendReminderPublishSubject = PublishSubject.create<String>()
    private var changeAdoptionModePublishSubject = PublishSubject.create<String>()
    private var setCollectionPopupOpenPublishSubject = PublishSubject.create<Boolean>()

    private var alert: Snackbar? = null
    internal var accountNumEventSent = false
    internal var ifscEventSent = false
    internal var upiIdEventSent = false
    internal var isStartedCollectionEventSent = false

    private val compositeDisposable = CompositeDisposable()

    internal lateinit var liveSalesController: LiveSalesController

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var imageLoader: IImageLoader

    @Inject
    internal lateinit var tracker: Tracker

    private var alertDialog: AlertDialog? = null

    private lateinit var linearLayoutManager: LinearLayoutManager

    private val dataObserver by lazy {
        object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (recycler_view != null) {
                    recycler_view.scrollToPosition(liveSalesController.adapter.itemCount - 1)
                }
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                if (recycler_view != null) {
                    recycler_view.scrollToPosition(liveSalesController.adapter.itemCount - 1)
                }
            }
        }
    }

    override fun onDestroyView() {
        liveSalesController.adapter.unregisterAdapterDataObserver(dataObserver)
        super.onDestroyView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.live_sales_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        liveSalesController = LiveSalesController(this)
        linearLayoutManager = LinearLayoutManager(context)
        recycler_view.layoutManager = linearLayoutManager
        recycler_view.adapter = liveSalesController.adapter
        recycler_view.setHasFixedSize(true)

        liveSalesController.adapter.registerAdapterDataObserver(dataObserver)

        account_clear.setOnClickListener {
            account_number.text?.clear()
        }

        ifsc_clear.setOnClickListener {
            ifsc.text?.clear()
        }

        upi_clear.setOnClickListener {
            upi_id.text?.clear()
        }

        account_number.setOnFocusChangeListener { v, hasFocus ->
            Timber.e("<<<LinkPay account_number $hasFocus")
            if (hasFocus) {
                tv_account_number.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
                bank_account_container.setBackgroundResource(R.drawable.circular_corners_selected_background)
            } else {
                tv_account_number.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey900))
                bank_account_container.setBackgroundResource(R.drawable.circular_corners_unselected_background)
            }
        }

        ifsc.setOnFocusChangeListener { v, hasFocus ->
            Timber.e("<<<LinkPay ifsc $hasFocus")
            if (hasFocus) {
                tv_ifsc.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
                ifsc_container.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.circular_corners_selected_background)
            } else {
                tv_ifsc.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey900))
                ifsc_container.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.circular_corners_unselected_background)
            }
        }

        ifsc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                ifsc_error.visibility = if (Utils.isValidIFSC(editable.toString())) View.GONE else View.VISIBLE
                if (editable.isEmpty()) {
                    ifsc_clear.visibility = View.GONE
                } else {
                    ifsc_clear.visibility = View.VISIBLE
                    tracker.trackEvents(
                        Event.ENTERED_INVALID_COLLECTION_DETAILS,
                        type = PropertyValue.IFSC,
                        screen = Screen.SUPPLIER_SCREEN,
                        relation = PropertyValue.MERCHANT
                    )
                }
                if (editable.isNotEmpty() && ifscEventSent.not()) {
                    ifscEventSent = true
                    tracker.trackEvents(
                        Event.ENTER_COLLECTION_DETAILS, type = PropertyValue.IFSC,
                        relation = PropertyValue.MERCHANT,
                        propertiesMap = PropertiesMap.create()
                            .add(PropertyKey.METHOD, PropertyValue.Typing)
                    )

                    if (isStartedCollectionEventSent.not()) {
                        isStartedCollectionEventSent = true
                        trackCollectionStartedEvent()
                    }
                }
            }
        })

        account_number.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                account_clear.visibility = if (editable.isEmpty()) View.GONE else View.VISIBLE
                if (editable.isNotEmpty() && accountNumEventSent.not()) {
                    accountNumEventSent = true
                    tracker.trackEvents(
                        Event.ENTER_COLLECTION_DETAILS, type = PropertyValue.ACCOUNT_NUMBER,
                        relation = PropertyValue.MERCHANT,
                        propertiesMap = PropertiesMap.create()
                            .add(PropertyKey.METHOD, PropertyValue.Typing)
                    )
                    if (isStartedCollectionEventSent.not()) {
                        isStartedCollectionEventSent = true
                        trackCollectionStartedEvent()
                    }
                }
            }
        })

        upi_id.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                upi_clear.visibility = if (editable.isEmpty()) View.GONE else View.VISIBLE
                if (editable.isNotEmpty() && upiIdEventSent.not()) {
                    upiIdEventSent = true
                    tracker.trackEvents(
                        Event.ENTER_COLLECTION_DETAILS, type = PropertyValue.UPI,
                        relation = PropertyValue.MERCHANT,
                        propertiesMap = PropertiesMap.create()
                            .add(PropertyKey.METHOD, PropertyValue.Typing)
                    )
                    tracker.trackEvents(
                        Event.STARTED_COLLECTION_ADAPTION_TYPE,
                        type = PropertyValue.UPI,
                        source = Screen.LINK_PAY_SCREEN
                    )
                }
            }
        })
        dimLayout.setOnClickListener {
            closeCollectionAdoptionPopup()
            KeyboardVisibilityEvent.hideKeyboard(activity)
        }
        root_view.setTracker(performanceTracker)
    }

    private fun trackCollectionStartedEvent() {
        tracker.trackEvents(
            Event.STARTED_COLLECTION_ADAPTION_TYPE,
            type = PropertyValue.BANK,
            source = Screen.LINK_PAY_SCREEN
        )
    }

    override fun loadIntent(): UserIntent {
        return LiveSalesContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            // transaction click intent
            transactionClicks
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    LiveSalesContract.Intent.ViewTransactionDetails(it.first, it.second)
                },

            // expand txns click intent
            expandTransactions
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { LiveSalesContract.Intent.ExpandTransactions },

            // set top scroll item
            topScrollItemChanged
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map { LiveSalesContract.Intent.SetScrollTopTransaction(it) },

            // expand txns click intent
            privacyClicks
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map { LiveSalesContract.Intent.GoToPrivacyScreen },

            updateLastViewedTime
                .map { LiveSalesContract.Intent.UpdateLastViewTime },

            qr_code.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    LiveSalesContract.Intent.ShowQrCodeDialog
                },

            hideQrCodeDialogPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    LiveSalesContract.Intent.HideQrCodeDialog
                },

            sendReminderPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    LiveSalesContract.Intent.SendWhatsAppReminder(it)
                },

            submit_details.clicks()
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    val state = getCurrentState()
                    if (state.adoptionMode == "upi") {
                        trackUpiCollectionStartedEvent()
                        LiveSalesContract.Intent.SetUpiVpa(upi_id.text.toString(), Screen.LINK_PAY_SCREEN)
                    } else {
                        trackBankCollectionStarted()
                        val paymentAddress = "${account_number.text}@${ifsc.text}"
                        LiveSalesContract.Intent.ConfirmBankAccount(paymentAddress)
                    }
                },

            changeAdoptionModePublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    LiveSalesContract.Intent.SetAdoptionMode(it)
                },

            setCollectionPopupOpenPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    LiveSalesContract.Intent.SetCollectionPopupOpen(it)
                }
        )
    }

    private fun trackBankCollectionStarted() {
        tracker.trackEvents(
            Event.STARTED_ADOPT_COLLECTION,
            type = PropertyValue.BANK,
            source = Screen.LINK_PAY_SCREEN
        )
        tracker.trackEvents(
            Event.CONFIRM_COLLECTION_DETAILS,
            type = PropertyValue.BANK,
            relation = PropertyValue.MERCHANT,
            source = Screen.LINK_PAY_SCREEN
        )
    }

    private fun trackUpiCollectionStartedEvent() {
        tracker.trackEvents(
            Event.STARTED_ADOPT_COLLECTION,
            type = PropertyValue.UPI,
            source = Screen.LINK_PAY_SCREEN
        )
        tracker.trackEvents(
            Event.CONFIRM_COLLECTION_DETAILS,
            type = PropertyValue.UPI,
            relation = PropertyValue.MERCHANT,
            source = Screen.LINK_PAY_SCREEN
        )
    }

    @SuppressLint("CheckResult")
    @AddTrace(name = CollectionTraces.RENDER_LIVE_SALES)
    override fun render(state: LiveSalesContract.State) {
        val epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(recycler_view)

        liveSalesController.setState(state)

        val totalAmount: Long = if (state.customer?.balanceV2 == null) 0L else state.customer.balanceV2
        CurrencyUtil.renderV2(totalAmount, total_balance, 0)

        if (state.upiLoaderStatus) {
            upi_loader.visibility = View.VISIBLE
            submit_details.visibility = View.GONE
        } else {
            upi_loader.visibility = View.GONE
            submit_details.visibility = View.VISIBLE
        }

        if (state.isCollectionActivated) {
            KeyboardVisibilityEvent.hideKeyboard(activity)
            closeCollectionAdoptionPopup()
        }

        ll_share_link_pay.setOnClickListener {
            if (state.isCollectionActivated) {
                val customer = state.customer ?: return@setOnClickListener
                sendReminderPublishSubject.onNext(customer.id)
            } else {
                showUpiUI()
                openCollectionAdoptionPopup(state)
                changeAdoptionModePublishSubject.onNext(if (state.adoptionMode.isNullOrEmpty()) "upi" else state.adoptionMode)
            }

            tracker.trackEvents(
                Event.SHARE_LINK_PAY, screen = Screen.LINK_PAY_SCREEN,
                propertiesMap = PropertiesMap.create()
                    .add(PropertyKey.COLLECTION_ADOPTED, state.isCollectionActivated)
                    .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
            )

            tracker.trackEvents(
                Event.SEND_REMINDER, type = PropertyValue.WHATSAPP, screen = Screen.LINK_PAY_SCREEN,
                relation = PropertyValue.CUSTOMER,
                propertiesMap = PropertiesMap.create()
                    .add(PropertyKey.COLLECTION_ADOPTED, state.isCollectionActivated)
                    .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                    .add(PropertyKey.FLOW, PropertyValue.LINK_PAY)
            )
        }

        if (state.adoptionMode == CollectionDestinationType.UPI.value) {
            showUpiUI()
        } else if (state.adoptionMode == CollectionDestinationType.BANK.value) {
            showBankUI()
        }

        tv_switch_payment_mode.setOnClickListener {
            val newAdoptionMode =
                if (state.adoptionMode == CollectionDestinationType.UPI.value) {
                    tracker.trackEvents(
                        Event.CHANGE_COLLECTION_METHOD,
                        type = CollectionDestinationType.BANK.value,
                        relation = PropertyValue.MERCHANT,
                        source = Screen.LINK_PAY_SCREEN
                    )
                    CollectionDestinationType.BANK.value
                } else if (state.adoptionMode == CollectionDestinationType.BANK.value) {
                    tracker.trackEvents(
                        Event.CHANGE_COLLECTION_METHOD,
                        type = CollectionDestinationType.UPI.value,
                        relation = PropertyValue.MERCHANT,
                        source = Screen.LINK_PAY_SCREEN
                    )
                    CollectionDestinationType.UPI.value
                } else {
                    ""
                }
            changeAdoptionModePublishSubject.onNext(newAdoptionMode)
        }

        // show/hide alert
        showAlertForErrors(state)

        // show loading
        if (state.isLoading) {
            shimmer_view_container.visibility = View.VISIBLE
        } else {
            shimmer_view_container.visibility = View.GONE
        }

        liveSaleTitle.text = requireContext().getString(R.string.link_pay)
    }

    private fun openCollectionAdoptionPopup(state: LiveSalesContract.State) {
        KeyboardVisibilityEvent.showKeyboard(
            context,
            if (state.adoptionMode == "upi") upi_id else account_number,
            root_view
        )
        rl_add_destination_container.visibility = View.VISIBLE
        dimLayout.visibility = View.VISIBLE
        AnimationUtils.fadeIn(dimLayout)
        setCollectionPopupOpenPublishSubject.onNext(true)
    }

    private fun closeCollectionAdoptionPopup() {
        AnimationUtils.fadeOut(dimLayout)
        rl_add_destination_container.visibility = View.GONE
        compositeDisposable.add(
            Completable
                .timer(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    rl_add_destination_container.visibility = View.GONE
                    dimLayout.visibility = View.GONE
                    setCollectionPopupOpenPublishSubject.onNext(false)
                }
        )
    }

    private fun showAlertForErrors(state: LiveSalesContract.State) {
        if (state.networkError or state.error or state.isAlertVisible or state.upiErrorServer
            or state.invalidBankAccountError
        ) {
            alert = when {
                state.networkError -> view?.snackbar(
                    getString(R.string.home_no_internet_msg),
                    Snackbar.LENGTH_INDEFINITE
                )
                state.isAlertVisible -> view?.snackbar(state.alertMessage, Snackbar.LENGTH_INDEFINITE)
                state.error -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_LONG)
                state.upiErrorServer -> view?.snackbar(getString(R.string.invalid_upi), Snackbar.LENGTH_INDEFINITE)
                state.invalidBankAccountError -> {

                    var errorMsg = ""
                    when (state.invalidBankAccountCode) {
                        LiveSalesContract.INVALID_ACCOUNT_NUMBER -> {
                            errorMsg = requireContext().resources.getString(R.string.invalid_account_number)
                        }
                        LiveSalesContract.INVALID_IFSC_CODE -> {
                            errorMsg = requireContext().resources.getString(R.string.invalid_ifsc_code)
                        }
                        LiveSalesContract.INVALID_NAME -> {
                            errorMsg = requireContext().resources.getString(R.string.invalid_account_name)
                        }
                        LiveSalesContract.INVALID_ACCOUNT_NUMBER_AND_IFSC_CODE -> {
                            errorMsg = requireContext().resources.getString(R.string.invalid_account_or_ifsc)
                        }
                    }

                    view?.snackbar(errorMsg, Snackbar.LENGTH_LONG)
                }
                else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_LONG)
            }
            alert?.show()
        } else {
            alert?.dismiss()
        }
    }

    private fun showUpiUI() {
        ifsc_container.visibility = View.GONE
        bank_account_container.visibility = View.GONE
        tv_account_number.visibility = View.GONE
        tv_ifsc.visibility = View.GONE
        upi_id_container.visibility = View.VISIBLE
        tv_upi_id.visibility = View.VISIBLE
        tv_switch_payment_mode.text = getString(R.string.add_bank_account)
        tv_add_payment_title.text = getString(R.string.add_upi_id)
        ifsc_error.visibility = View.GONE
        tv_switch_payment_mode.paintFlags = tv_switch_payment_mode.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        upi_id.requestFocus()
    }

    private fun showBankUI() {
        ifsc_container.visibility = View.VISIBLE
        bank_account_container.visibility = View.VISIBLE
        tv_account_number.visibility = View.VISIBLE
        tv_ifsc.visibility = View.VISIBLE
        upi_id_container.visibility = View.GONE
        tv_upi_id.visibility = View.GONE
        tv_switch_payment_mode.text = getString(R.string.add_upi_id)
        tv_add_payment_title.text = getString(R.string.add_bank_details)
        tv_switch_payment_mode.paintFlags = tv_switch_payment_mode.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        account_number.requestFocus()
    }

    /****************************************************************
     * Lifecycle methods (for child views)
     ****************************************************************/

    override fun onBackPressed(): Boolean {

        if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
            KeyboardVisibilityEvent.hideKeyboard(activity)
            return true
        } else if (getCurrentState().isCollectionPopupOpen == true) {
            closeCollectionAdoptionPopup()
            return true
        }
        LocaleManager.fixWebViewLocale(requireContext())

        updateLastViewedTime.onNext(Unit)
        return super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable.dispose()

        if (alertDialog != null && alertDialog?.isShowing() == true) {
            alertDialog?.dismiss()
        }
    }

    /****************************************************************
     * Listeners (for child views)
     ****************************************************************/
    override fun onTransactionClicked(collectionId: String, currentDue: Long) {
        transactionClicks.onNext(collectionId to currentDue)
    }

    fun onTopScrollItemChanged(date: DateTime) {
        if (date.millis != 0L) {
            topScrollItemChanged.onNext(date)
        } else {
            topScrollItemChanged.onNext(DateTime(0))
        }
    }

    @UiThread
    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    @UiThread
    override fun gotoTransactionScreen(transactionId: String, currentDue: Long) {
        activity?.runOnUiThread {
            legacyNavigator.goToTransactionDetailFragment(requireActivity(), transactionId)
        }
    }

    override fun gotoCustomerPrivacyScreen() {
        activity?.runOnUiThread {
            legacyNavigator.gotoPrivacyScreen(requireContext())
        }
    }

    override fun shareReminder(intent: Intent) {
        activity?.startActivity(intent)
    }

    @UiThread
    override fun showQrCodePopup(
        customer: Customer,
        collectionCustomerProfile: CollectionCustomerProfile,
        business: Business?,
        merchantPaymentAddress: String?
    ) {
        if (collectionCustomerProfile.qr_intent.isNullOrEmpty()) return

        val fragmentMangaer = requireActivity().supportFragmentManager
        var paymentDialogFrag = fragmentMangaer.findFragmentByTag(LinkPayBottomSheetPaymentDialog.TAG)

        if (paymentDialogFrag != null && paymentDialogFrag is LinkPayBottomSheetPaymentDialog) {
            paymentDialogFrag.render(customer, collectionCustomerProfile)
            return
        }

        tracker.trackEvents(
            Event.VIEW_LINK_PAY_QR, screen = Screen.LINK_PAY_SCREEN,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.COLLECTION_ADOPTED, getCurrentState().isCollectionActivated)
        )

        tracker.trackEvents(Event.CLICKED_ON_LINK_PAY_ICON)

        paymentDialogFrag = LinkPayBottomSheetPaymentDialog.newInstance(
            LinkPayBottomSheetPaymentDialog.LinkPayReminderDialogData(
                customer,
                collectionCustomerProfile,
                merchantPaymentAddress
            )
        )
        paymentDialogFrag.show(fragmentMangaer, LinkPayBottomSheetPaymentDialog.TAG)

        paymentDialogFrag.initialize(object : LinkPayBottomSheetPaymentDialog.CustomerListener {

            override fun onSendReminderClicked(
                customer: Customer,
                collectionCustomerProfile: CollectionCustomerProfile
            ) {
                tracker.trackEvents(Event.CLICKED_ON_SHARE_LINK_PAY)
                sendReminderPublishSubject.onNext(customer.id)
            }

            override fun onDismiss() {
                hideQrCodeDialogPublishSubject.onNext(Unit)
                tracker.trackEvents(
                    Event.LINK_PAY_QR_CLOSED, screen = Screen.LINK_PAY_SCREEN,
                    propertiesMap = PropertiesMap.create()
                        .add(PropertyKey.COLLECTION_ADOPTED, getCurrentState().isCollectionActivated)
                )
            }
        })
    }
}
