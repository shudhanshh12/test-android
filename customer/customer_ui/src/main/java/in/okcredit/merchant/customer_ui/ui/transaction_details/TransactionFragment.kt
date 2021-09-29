package `in`.okcredit.merchant.customer_ui.ui.transaction_details

import `in`.okcredit.analytics.*
import `in`.okcredit.backend.analytics.AnalyticsSuperProps
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.backend.utils.BitmapUtils
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.backend.utils.Utils
import `in`.okcredit.collection.contract.CollectionStatus
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.core.model.History
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.merchant.customer_ui.databinding.TransactionFragmentBinding
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.AddBillsView
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.AddBillsViewModel_
import `in`.okcredit.merchant.customer_ui.ui.dialogs.DeleteTransactionConfirmationDialog
import `in`.okcredit.merchant.customer_ui.ui.subscription.SubscriptionActivity
import `in`.okcredit.merchant.customer_ui.ui.transaction_details.TransactionContract.*
import `in`.okcredit.merchant.customer_ui.ui.transaction_details.views.ImageCarouselItem
import `in`.okcredit.merchant.customer_ui.ui.transaction_details.views.ImageCarouselItemModel_
import `in`.okcredit.merchant.customer_ui.ui.transaction_details.views.imageCarousel
import `in`.okcredit.merchant.customer_ui.ui.transaction_details.views.transactionAmountHistoryView
import `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount.UpdateTransactionAmountScreen
import `in`.okcredit.merchant.customer_ui.utils.GoogleVoiceTypingDisabledException
import `in`.okcredit.merchant.customer_ui.utils.SpeechInput
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.ScreenName
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.text.Html
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.EpoxyModel
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.common.base.Strings
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.transaction_fragment.*
import kotlinx.android.synthetic.main.txn_voice_input_layout.*
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.analytics.AccountingEventTracker.Companion.SOURCE_TXN_PAGE
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionDialog
import merchant.okcredit.accounting.utils.AccountingSharedUtils.getWhatsAppMsg
import org.joda.time.DateTime
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.KeyboardUtil
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import tech.okcredit.base.exceptions.ExceptionUtils
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.contract.AppLock
import tech.okcredit.contract.AppLockTracker
import tech.okcredit.contract.Constants.IS_AUTHENTICATED
import tech.okcredit.contract.Constants.SECURITY_PIN_CHANGED
import tech.okcredit.contract.Constants.SECURITY_PIN_SET
import tech.okcredit.contract.OnSetPinClickListener
import tech.okcredit.contract.OnUpdatePinClickListener
import tech.okcredit.userSupport.SupportRepository
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class TransactionFragment :
    BaseFragment<State, ViewEvent, TransactionContract.Intent>("TransactionScreen", R.layout.transaction_fragment),
    Navigator,
    ImageCarouselItem.Listener,
    AddBillsView.Listener,
    OnSetPinClickListener,
    OnUpdatePinClickListener {

    private val onNewImagesAdded: PublishSubject<Triple<ArrayList<CapturedImage>, List<merchant.okcredit.accounting.model.TransactionImage>, String>> =
        PublishSubject.create()
    private val syncNow: PublishSubject<Unit> = PublishSubject.create()
    private val onNoteSubmitClicked: PublishSubject<Pair<String, String>> = PublishSubject.create()
    private val openSmsApp: PublishSubject<Unit> = PublishSubject.create()

    internal val showAlert: PublishSubject<String> = PublishSubject.create()
    private val editPayment: PublishSubject<Unit> = PublishSubject.create()
    private val noteInputState: PublishSubject<Boolean> = PublishSubject.create()
    internal val onNoteChanged: PublishSubject<String> = PublishSubject.create()
    private val onKnowMoreClicked: PublishSubject<String> = PublishSubject.create()
    private val changedImages: PublishSubject<Triple<ArrayList<merchant.okcredit.accounting.model.TransactionImage>, ArrayList<merchant.okcredit.accounting.model.TransactionImage>, String>> =
        PublishSubject.create()
    internal val whatsAppUsPublishSubject: PublishSubject<Boolean> = PublishSubject.create()
    internal val deleteTxnPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private val showDeleteTxnEducationPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private val knowMorePublishSubject: PublishSubject<Unit> = PublishSubject.create()
    internal val showExpandedHistoryPublishSubject: PublishSubject<Boolean> = PublishSubject.create()

    internal val setNewPin: PublishSubject<UserIntent> = PublishSubject.create()
    internal val updateNewPin: PublishSubject<UserIntent> = PublishSubject.create()
    internal val updatePin: PublishSubject<UserIntent> = PublishSubject.create()
    internal val syncMerchant: PublishSubject<UserIntent> = PublishSubject.create()

    internal var manualText = false
    internal var voiceText = false
    internal var speachText: String? = null
    private var isEditAmountEducationShown = false
    private var addNoteRunnable: Runnable? = null

    companion object {
        const val DELETE_TXN_REQUEST = 1
        private const val CAMERA_ACTIVITY_REQUEST_CODE: Int = 3
        private const val RECORD_AUDIO_PERMISSION: Int = 4
        private const val MULTIPLE_IMAGE_SELECTION: Int = 2
        const val FILE_NAME_REMINDER = "reminder.jpg"
        const val FOLDER_NAME_REMINDER = "reminder_images"
        const val EDIT_TRANSACTION = 141
        const val EDIT_TRANSACTION_SET_NEW_PIN = 14001
        const val EDIT_TRANSACTION_UPDATE_PIN = 14002
    }

    internal var voiceIconAnimator: Animator? = null

    @Inject
    internal lateinit var speechRecognizer: SpeechRecognizer

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    @Inject
    internal lateinit var appLock: Lazy<AppLock>

    @Inject
    internal lateinit var appLockTracker: Lazy<AppLockTracker>

    @Inject
    internal lateinit var communicationRepository: CommunicationRepository

    @Inject
    lateinit var userSupport: Lazy<SupportRepository>

    @Inject
    lateinit var accountingEventTracker: Lazy<AccountingEventTracker>

    private var alert: Snackbar? = null
    private var speechInput: SpeechInput? = null
    private var status = ""

    private val binding: TransactionFragmentBinding by viewLifecycleScoped(TransactionFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.grey50
                )
            )
        )
        binding.contextualHelp.initDependencies(
            screenName = ScreenName.TxnDetailsScreen.value,
            tracker = tracker,
            legacyNavigator = legacyNavigator
        )
        binding.recyclerView.isNestedScrollingEnabled = false
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        add_note_input_field.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_CLASS_TEXT
        add_note_input_field.maxLines = 3
        var eventSent = false
        add_note_input_field.doAfterTextChanged {
            manualText = true
            if (!eventSent && isStateInitialized()) {
                eventSent = true
                tracker.trackAddNoteStarted(
                    "Edit Transaction",
                    "Customer",
                    getPaymentType(),
                    "Fab",
                    getCurrentState().customer?.id,
                    getCurrentState().transaction?.id
                )
            }
        }
        binding.rootView.setTracker(performanceTracker)
        setCustomerSupportListener()
    }

    private fun setCustomerSupportListener() {
        binding.llCustomerSupport.setOnClickListener {
            getCurrentState().let {
                accountingEventTracker.get().trackCustomerSupportMsgClicked(
                    source = AccountingEventTracker.TXN_PAGE_VIEW,
                    txnId = it.collection?.paymentId ?: "",
                    amount = it.transaction?.amountV2.toString(),
                    relation = LedgerType.CUSTOMER.value.lowercase(),
                    status = status.lowercase(),
                    supportMsg = getWhatsAppMsg(
                        requireContext(),
                        amount = CurrencyUtil.formatV2(it.transaction?.amountV2 ?: 0L),
                        paymentTime = DateTimeUtils.formatLong(it.transaction?.billDate ?: DateTime()),
                        txnId = it.collection?.paymentId ?: "",
                        status = status,
                    ),
                    type = it.supportType.value
                )
                CustomerSupportOptionDialog
                    .newInstance(
                        amount = it.transaction?.amountV2.toString(),
                        paymentTime = DateTimeUtils.formatLong(it.transaction?.billDate ?: DateTime()),
                        txnId = it.collection?.paymentId ?: "",
                        status = status,
                        accountId = getCurrentState().customer?.id ?: "",
                        ledgerType = LedgerType.CUSTOMER.value.lowercase(),
                        source = SOURCE_TXN_PAGE,
                    ).show(
                        childFragmentManager,
                        CustomerSupportOptionDialog.TAG
                    )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        addNoteRunnable?.let {
            add_note_input_field.removeCallbacks(it)
        }
    }

    private fun initListener() {
        binding.llAddedOn.setOnClickListener {
            val flow: String? = getFlow()
            tracker.trackTransactionDetails(
                eventName = Event.ADDED_ON,
                relation = PropertyValue.CUSTOMER,
                type = getTransactionType()!!,
                accountId = getCurrentState().customer!!.id,
                flow = flow
            )
        }

        binding.dateContainer.setOnClickListener {
            val flow: String? = getFlow()
            tracker.trackTransactionDetails(
                eventName = Event.DATE_CLICKED,
                relation = PropertyValue.CUSTOMER,
                type = getTransactionType()!!,
                accountId = getCurrentState().customer!!.id,
                flow = flow
            )
        }

        binding.rlAddedAmount.setOnClickListener {
            tracker.trackTransactionDetails(
                eventName = Event.AMOUNT_CLICKED,
                relation = PropertyValue.CUSTOMER,
                type = getTransactionType() ?: "",
                accountId = getCurrentState().customer?.id ?: ""
            )
        }

        binding.llTxnId.setOnClickListener {
            trackOnlineCollectionEvents(Event.ONLINE_TRANSACTION_ID, getCurrentState())
        }

        binding.llTxnTo.setOnClickListener {
            trackOnlineCollectionEvents(Event.ONLINE_PAYMENT_TO, getCurrentState())
        }

        binding.llPaymentFailedStatus.setOnClickListener {
            trackOnlineCollectionEvents(Event.PAYMENT_FAILED_CLICK, getCurrentState())
        }

        binding.subscriptionContainer.setOnClickListener { pushIntent(TransactionContract.Intent.SubscriptionClicked) }

        binding.addedByContainer.setOnClickListener {
            val flow: String =
                if (getCurrentState().customer?.isLiveSales == true) {
                    PropertyValue.LINK_PAY
                } else {
                    PropertyValue.ONLINE_TRANSACTION
                }
            tracker.trackEvents(
                Event.ADDED_BY,
                type = PropertyValue.PAYMENT,
                screen = PropertyValue.TRANSACTION_DETAILS,
                relation = PropertyValue.CUSTOMER,
                propertiesMap = PropertiesMap.create()
                    .add(PropertyKey.FLOW, flow)
                    .add(PropertyKey.ACCOUNT_ID, getCurrentState().customer?.id ?: "")

            )
        }

        binding.editTransactionAmount.setOnClickListener {
            if (getCurrentState().isPasswordSet) {
                if (getCurrentState().isMerchantSync) {
                    if (getCurrentState().isFourDigitPin) {
                        editPayment.onNext(Unit)
                    } else {
                        updateNewPin.onNext(TransactionContract.Intent.CheckIsFourdigitPinSet)
                    }
                } else {
                    syncMerchant.onNext(TransactionContract.Intent.SyncMerchantPref)
                }
            } else {
                setNewPin.onNext(TransactionContract.Intent.SetNewPin)
            }
        }

        binding.btnDelete.setOnClickListener {
            deleteTxn()
        }

        binding.editedOnContainer.setOnClickListener {
            val state = getCurrentState()
            if (state.transaction?.amountUpdated == true) {
                if (requireContext().isConnectedToInternet().not()) {
                    view?.snackbar(
                        getString(R.string.please_connect_internet),
                        Snackbar.LENGTH_LONG
                    )?.show()
                    return@setOnClickListener
                }

                val type = getTxnType(state)
                if (state.isTxnViewExpanded) {
                    tracker.trackEvents(
                        eventName = Event.ACCOUNT_EDIT_AMOUNT_HISTORY_CLOSE,
                        type = type,
                        screen = "transaction_detail",
                        relation = PropertyValue.CUSTOMER,
                        propertiesMap = PropertiesMap.create()
                            .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                            .add(PropertyKey.COUNT, state.transactionAmountHistory?.history?.size ?: 0)
                    )
                }
                showExpandedHistoryPublishSubject.onNext(state.isTxnViewExpanded.not())
            } else {
                if (getCurrentState().isPasswordSet) {
                    if (getCurrentState().isMerchantSync) {
                        if (getCurrentState().isFourDigitPin) {
                            editPayment.onNext(Unit)
                        } else {
                            updateNewPin.onNext(TransactionContract.Intent.CheckIsFourdigitPinSet)
                        }
                    } else {
                        syncMerchant.onNext(TransactionContract.Intent.SyncMerchantPref)
                    }
                } else {
                    setNewPin.onNext(TransactionContract.Intent.SetNewPin)
                }
            }
        }
    }

    internal fun showEditAmountDialog() {
        getCurrentState().transaction?.let {
            val dialog = UpdateTransactionAmountScreen.newInstance(transactionId = it.id!!)
            dialog.show(childFragmentManager, UpdateTransactionAmountScreen.TAG)

            dialog.initialise(object : UpdateTransactionAmountScreen.OnUpDateAmountDismissListener {
                override fun onDismiss() {
                    val state = getCurrentState()
                    if (state.isTxnViewExpanded) {
                        val type = getTxnType(state)
                        tracker.trackEvents(
                            Event.ACCOUNT_EDIT_AMOUNT_HISTORY_CLOSE,
                            type = type,
                            screen = "transaction_detail",
                            relation = PropertyValue.CUSTOMER,
                            propertiesMap = PropertiesMap.create()
                                .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                                .add(PropertyKey.COUNT, state.transactionAmountHistory?.history?.size ?: 0)
                        )
                        showExpandedHistoryPublishSubject.onNext(false)
                    }
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!isStateInitialized()) return

        if (resultCode == Activity.RESULT_OK && requestCode == DELETE_TXN_REQUEST) {
            activity?.setResult(Activity.RESULT_OK, data)
            activity?.finish()
        }
        if (requestCode == MULTIPLE_IMAGE_SELECTION) {
            val imagesInfo = ImagesInfo()
            val transactionId = getCurrentState().transaction?.id
            imagesInfo.transactionId = transactionId

            if (data?.getSerializableExtra("selectedImages") != null) {
                val listPhotos = data.getSerializableExtra("selectedImages") as ArrayList<CapturedImage>

                val listTranasactionImages = getCurrentState().transaction?.receiptUrl?.filter { transactionImage ->

                    listPhotos.any {
                        return@any transactionImage.url == Utils.sanitiseFilePathToURL(it.file.path)
                    }
                }

                listTranasactionImages?.let {
                    imagesInfo.existingImages.addAll(it)
                    imagesInfo.tempImages.addAll(it)
                }
            }
            if (data?.getSerializableExtra("deletedImages") != null) {
                val deletedImages = data.getSerializableExtra("deletedImages") as ArrayList<CapturedImage>

                val deletedTransactionImages = getCurrentState().transaction?.receiptUrl?.filter { transactionImage ->
                    deletedImages.any { transactionImage.url == Utils.sanitiseFilePathToURL(it.file.path) }
                }
                deletedTransactionImages?.let {
                    imagesInfo.deletedImages.addAll(it)
                    for (i in 0 until imagesInfo.deletedImages.size) {
                        imagesInfo.tempImages.remove(imagesInfo.deletedImages[i])
                    }
                }
            }
            if (data?.getSerializableExtra("addedImages") != null) {
                val newlyAddedList = data.getSerializableExtra("addedImages") as ArrayList<CapturedImage>

                newlyAddedList.let {
                    imagesInfo.newAddedImages.addAll(it)
                    it.forEach {
                        val timestamp = DateTimeUtils.currentDateTime()
                        imagesInfo.tempImages.add(
                            merchant.okcredit.accounting.model.TransactionImage(
                                transactionId,
                                UUID.randomUUID().toString(),
                                transactionId,
                                it.file.path,
                                timestamp
                            )
                        )
                    }
                }
            }

            lifecycleScope.launchWhenResumed {
                pushIntent(
                    TransactionContract.Intent.OnImagesChanged(
                        imagesInfo,
                        (getCurrentState().transaction?.isDirty ?: false)
                    )
                )
            }
        } else if (requestCode == CAMERA_ACTIVITY_REQUEST_CODE) {
            if (data?.getSerializableExtra("addedImages") != null) {
                val imagesInfo = ImagesInfo()
                val transactionId = getCurrentState().transaction?.id
                imagesInfo.transactionId = transactionId

                getCurrentState().transaction?.receiptUrl?.let {
                    imagesInfo.tempImages.addAll(it)
                }
                val newlyAddedList = data.getSerializableExtra("addedImages") as ArrayList<CapturedImage>

                if (newlyAddedList.size > 0) {
                    newlyAddedList.let { list ->
                        imagesInfo.newAddedImages.addAll(list)
                        list.forEach {
                            val timestamp = DateTimeUtils.currentDateTime()
                            imagesInfo.tempImages.add(
                                merchant.okcredit.accounting.model.TransactionImage(
                                    transactionId,
                                    UUID.randomUUID().toString(),
                                    transactionId,
                                    it.file.path,
                                    timestamp
                                )
                            )
                        }
                    }
                    lifecycleScope.launchWhenResumed {
                        pushIntent(
                            TransactionContract.Intent.OnImagesChanged(
                                imagesInfo,
                                (getCurrentState().transaction?.isDirty ?: false)
                            )
                        )
                    }
                }
            }
        }
        if (requestCode == EDIT_TRANSACTION || requestCode == EDIT_TRANSACTION_SET_NEW_PIN || requestCode == EDIT_TRANSACTION_UPDATE_PIN) {
            data?.let {
                if (it.getBooleanExtra(IS_AUTHENTICATED, false)) {

                    if (requestCode == EDIT_TRANSACTION_SET_NEW_PIN)
                        appLockTracker.get().trackEvents(eventName = SECURITY_PIN_SET, source = "TransactionScreen")
                    if (requestCode == EDIT_TRANSACTION_UPDATE_PIN)
                        appLockTracker.get().trackEvents(eventName = SECURITY_PIN_CHANGED, source = "TransactionScreen")

                    val state = getCurrentState()
                    if (state.isEditTxnAmountEnabled) {
                        showEditAmountDialog()
                        val type = getTxnType(state)
                        tracker.trackEvents(
                            Event.ACCOUNT_EDIT_AMOUNT_CLICK,
                            type = type,
                            screen = "transaction_detail",
                            relation = PropertyValue.CUSTOMER,
                            propertiesMap = PropertiesMap.create()
                                .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                                .add(PropertyKey.VALUE, "edited_container")
                        )
                    }
                }
            }
        }
    }

    override fun loadIntent(): UserIntent {
        return TransactionContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.just(TransactionContract.Intent.Resume),
            syncNow
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { TransactionContract.Intent.SyncTransaction },

            openSmsApp
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { TransactionContract.Intent.OpenSmsApp },

            showAlert
                .map { TransactionContract.Intent.ShowAlert(it) },

            deleteTxnPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    TransactionContract.Intent.Delete
                },

            knowMorePublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    TransactionContract.Intent.OnKnowMoreClicked(getCurrentState().customer!!.id)
                },

            binding.btnShare.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    val flow: String? = getFlow()
                    Analytics.track(
                        AnalyticsEvents.SHARE_TXN,
                        EventProperties
                            .create()
                            .with(PropertyKey.ACCOUNT_ID, getCurrentState().customer?.id)
                            .with("customer_id", getCurrentState().customer?.id)
                            .with("transaction_id", getCurrentState().transaction?.id)
                            .with(PropertyKey.RELATION, PropertyValue.CUSTOMER)
                            .with(PropertyKey.FLOW, flow)
                    )
                    TransactionContract.Intent.ShareOnWhatsApp
                },
            noteInputState.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    TransactionContract.Intent.Note(it)
                },
            changedImages
                .map {
                    TransactionContract.Intent.ImagesChanged(it)
                },
            onNewImagesAdded.map {
                TransactionContract.Intent.NewImagesAdded(it)
            },
            onNoteSubmitClicked.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    TransactionContract.Intent.NoteSubmitClicked(it)
                },
            onKnowMoreClicked.throttleFirst(300, TimeUnit.MILLISECONDS).map {
                TransactionContract.Intent.OnKnowMoreClicked(it)
            },

            whatsAppUsPublishSubject
                .map {
                    TransactionContract.Intent.WhatsApp(it)
                },

            showExpandedHistoryPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    TransactionContract.Intent.IsTxnViewExpanded(it)
                },

            showDeleteTxnEducationPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    TransactionContract.Intent.ShowDeleteTxnEducation
                },
            setNewPin
                .throttleFirst(300, TimeUnit.MILLISECONDS),
            updatePin
                .throttleFirst(300, TimeUnit.MILLISECONDS),
            updateNewPin
                .throttleFirst(300, TimeUnit.MILLISECONDS),
            syncMerchant
                .throttleFirst(300, TimeUnit.MILLISECONDS),

            editPayment
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    TransactionContract.Intent.EditPayment
                }
        )
    }

    private fun deleteTxn() {
        val state = getCurrentState()
        if (state.deleteStatus == DeleteLayoutStatus.InActive) {
            val type = getTxnType(state)

            val reason = when {
                state.transaction?.isCreatedByCustomer == true -> {
                    "Created By Other"
                }
                state.customer?.isBlockedByCustomer() == true -> {
                    "Blocked"
                }
                state.transaction?.isOnlinePaymentTransaction == true -> {
                    PropertyValue.ONLINE_PAYMENT
                }
                else -> {
                    "na"
                }
            }

            tracker.trackEvents(
                Event.ACCOUNT_EDIT_AMOUNT_INFO_CLICK,
                type = type,
                screen = "transaction_detail",
                relation = PropertyValue.CUSTOMER,
                propertiesMap = PropertiesMap.create()
                    .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                    .add(PropertyKey.REASON, reason)
            )
            knowMorePublishSubject.onNext(Unit)
        } else {
            if (state.isEditTxnAmountEnabled && state.transaction?.amountUpdated == false) {
                showDeleteTxnEducationPublishSubject.onNext(Unit)
            } else {
                tracker.trackDeleteTransaction(
                    state.customer?.id,
                    state.transaction?.id,
                    PropertyValue.CUSTOMER
                )
                deleteTxnPublishSubject.onNext(Unit)
            }
        }
    }

    override fun showDeleteTxnConfirmationDialog() {
        activity?.runOnUiThread {
            val dialog = DeleteTransactionConfirmationDialog.newInstance()
            dialog.show(childFragmentManager, DeleteTransactionConfirmationDialog.TAG)

            dialog.initialise(object : DeleteTransactionConfirmationDialog.DeleteTxnConfirmationListener {
                override fun onDeleteTxn() {
                    val state = getCurrentState()
                    val type = getTxnType(state)
                    tracker.trackEvents(
                        Event.ACCOUNT_DELETE_CLICK,
                        type = type,
                        screen = "delete_edit_education",
                        relation = PropertyValue.CUSTOMER,
                        propertiesMap = PropertiesMap.create()
                            .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                    )
                    deleteTxnPublishSubject.onNext(Unit)
                }

                override fun onEditAmount() {
                    val state = getCurrentState()
                    val type = getTxnType(state)
                    tracker.trackEvents(
                        Event.ACCOUNT_EDIT_AMOUNT_POPUP,
                        type = type,
                        screen = "delete_popup",
                        relation = PropertyValue.CUSTOMER,
                        propertiesMap = PropertiesMap.create()
                            .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                    )

                    if (getCurrentState().isPasswordSet) {
                        if (getCurrentState().isMerchantSync) {
                            if (getCurrentState().isFourDigitPin) {
                                editPayment.onNext(Unit)
                            } else {
                                updateNewPin.onNext(TransactionContract.Intent.CheckIsFourdigitPinSet)
                            }
                        } else {
                            syncMerchant.onNext(TransactionContract.Intent.SyncMerchantPref)
                        }
                    } else {
                        setNewPin.onNext(TransactionContract.Intent.SetNewPin)
                    }
                }
            })
        }
    }

    override fun goToEnterPinScreen() {
        requireActivity().startActivityForResult(
            appLock.get().appLock(getString(R.string.enterpin_screen_deeplink), requireActivity(), "TransactionScreen"),
            EDIT_TRANSACTION
        )
    }

    override fun goToSetPinScreen() {
        activity?.runOnUiThread {
            appLock.get().showSetNewPin(
                requireActivity().supportFragmentManager,
                this,
                EDIT_TRANSACTION_SET_NEW_PIN,
                AnalyticsEvents.DELETE_TRANSACTION_SCREEN
            )
        }
    }

    override fun handleFourDigitPin(isFourDigitPinSet: Boolean) {
        requireActivity().runOnUiThread {
            if (isFourDigitPinSet) {
                editPayment.onNext(Unit)
            } else {
                updatePin.onNext(TransactionContract.Intent.UpdatePin)
            }
        }
    }

    override fun syncDone() {
        updateNewPin.onNext(TransactionContract.Intent.CheckIsFourdigitPinSet)
    }

    override fun showUpdatePinScreen() {
        activity?.runOnUiThread {
            appLock.get().showUpdatePin(
                requireActivity().supportFragmentManager,
                this,
                EDIT_TRANSACTION_UPDATE_PIN,
                AnalyticsEvents.DELETE_TRANSACTION_SCREEN
            )
        }
    }

    private fun getFlow(): String? {
        return if (getCurrentState().customer?.isLiveSales == true) PropertyValue.LINK_PAY else null
    }

    @AddTrace(name = Traces.RENDER_TRANSACTION_DETAILS)
    override fun render(state: State) {
        binding.contextualHelp.setContextualHelpIds(state.contextualHelpIds)

        if (state.transaction != null && state.customer != null && sync_container != null) {
            sync_container.setOnClickListener {

                if (state.transaction.isDirty) {
                    val flow: String? = getFlow()
                    tracker.trackTransactionDetails(
                        eventName = Event.SYNC_CLICKED,
                        relation = PropertyValue.CUSTOMER,
                        type = getTransactionType() ?: "",
                        accountId = state.customer.id,
                        status = PropertyValue.FAILED,
                        flow = flow
                    )

                    Analytics.track(
                        AnalyticsEvents.SYNC_TXN,
                        EventProperties
                            .create()
                            .with(PropertyKey.ACCOUNT_ID, state.customer.id)
                            .with("customer_id", state.customer.id)
                            .with("transaction_id", state.transaction.id)
                            .with(PropertyKey.RELATION, PropertyValue.CUSTOMER)
                    )
                    syncNow.onNext(Unit)
                } else {
                    val flow: String? = getFlow()
                    tracker.trackTransactionDetails(
                        eventName = Event.SYNC_CLICKED,
                        relation = PropertyValue.CUSTOMER,
                        type = getTransactionType()!!,
                        accountId = state.customer.id,
                        status = PropertyValue.SUCCESS,
                        flow = flow
                    )
                }
            }

            binding.smsContainer.setOnClickListener {
                if (state.transaction.isDirty) {
                    tracker.trackSendReminder(
                        PropertyValue.SMS,
                        state.customer.id,
                        state.transaction.id,
                        "transaction_page",
                        PropertyValue.CUSTOMER,
                        getCurrentState().customer?.mobile,
                        ""
                    )
                    openSmsApp.onNext(Unit)
                }

                var channel = ""
                getCurrentState().customer?.let {
                    channel = if (it.isRegistered()) {
                        PropertyValue.NOTIFICATION
                    } else {
                        PropertyValue.SMS
                    }
                }
                tracker.trackTransactionDetails(
                    eventName = Event.COMMUNICATION_DELIVERED,
                    relation = PropertyValue.CUSTOMER,
                    type = getTransactionType()!!,
                    accountId = state.customer.id,
                    channel = channel
                )
            }

            binding.txnVoiceInput.voiceIcon.setOnClickListener {
                tracker.trackAddNoteStarted(
                    "Edit Transaction",
                    "Customer",
                    getPaymentType(),
                    "Voice",
                    getCurrentState().customer?.id,
                    getCurrentState().transaction?.id
                )

                requestRecordAudioPermission()
            }

            val color = if (state.transaction.type == merchant.okcredit.accounting.model.Transaction.CREDIT) {
                R.color.tx_credit
            } else {
                R.color.tx_payment
            }

            rupee_symbol.setTextColor(ContextCompat.getColor(requireContext(), color))
            amount.setTextColor(ContextCompat.getColor(requireContext(), color))

            amount.text = CurrencyUtil.formatV2(state.transaction.amountV2)

            date.text = getString(R.string.bill_on, DateTimeUtils.formatDateOnly(state.transaction.billDate))
            created_date.text = getString(R.string.added_on_date, DateTimeUtils.formatLong(state.transaction.createdAt))

            if (state.transaction.type == merchant.okcredit.accounting.model.Transaction.PAYMENT || state.transaction.type == merchant.okcredit.accounting.model.Transaction.RETURN) {
                screen_title.text = getString(R.string.payment_details)
                delete_text.text = getString(R.string.delete_payment)
            } else if (state.transaction.type == merchant.okcredit.accounting.model.Transaction.CREDIT) {
                screen_title.text = getString(R.string.credit_details)
                delete_text.text = getString(R.string.delete_credit)
            }

            if (Strings.isNullOrEmpty(state.transaction.note)) {
                note.setTextIsSelectable(false)
                note.text = null
                note.setTextColor(resources.getColor(R.color.grey800))
                note_image.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary))
            } else {
                note.setTextIsSelectable(true)
                note.text = state.transaction.note
                note_image.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey900))

                note.setOnLongClickListener {
                    Analytics.track(
                        AnalyticsEvents.LONG_PRESS,
                        EventProperties.create()
                            .with(PropertyKey.SCREEN, "transaction_screen")
                            .with(PropertyKey.RELATION, PropertyValue.CUSTOMER)
                            .with(PropertyKey.ACCOUNT_ID, getCurrentState().customer?.id)
                    )
                    return@setOnLongClickListener false
                }
            }

            if (!state.transaction.isDirty) {
                syc_title.text = getString(R.string.txn_synced)
                syc_title.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey700))
                sync_left_icon.setImageResource(R.drawable.ic_single_tick)
                sync_left_icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey900))

                setDeliveryMessage(state.customer)
            } else {
                syc_title.text = getString(R.string.txn_sync_now)
                syc_title.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
                sync_left_icon.setImageResource(R.drawable.clock_outline)
                sync_left_icon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary))

                if (state.transaction.isSmsSent || state.isSmsSent) {
                    setDeliveryMessage(state.customer)
                } else {
                    sms_title.text = getString(R.string.sms_from_phone)
                    sms_title.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
                    sms_left_icon.setImageResource(R.drawable.ic_reply_black_24dp)
                    sms_left_icon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary))
                }
            }

            added_by_left_icon.setImageResource(R.drawable.ic_single_tick)

            if (state.customer.mobile.isNullOrEmpty()) {
                binding.smsContainer.gone()
                binding.smsDivider.gone()
            } else {
                binding.smsContainer.visible()
                binding.smsDivider.visible()
            }

            if (state.transaction.isDeleted) {
                binding.deleteContainer.visible()
                binding.deleteDivider.visible()
                binding.btnDelete.gone()
                binding.btnShare.gone()
                binding.rupeeSymbol.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey400))
                binding.amount.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey400))
                binding.deletedDate.text =
                    "${getString(R.string.deleted_on)} : ${DateTimeUtils.formatLong(state.transaction.deleteTime)}"
            } else {
                binding.deleteContainer.gone()
                delete_divider.gone()
                binding.btnShare.visible()
                if (!state.collection?.id.isNullOrEmpty() || state.customer.status != 1) {
                    btn_delete.gone()
                } else {
                    btn_delete.visible()
                }
            }

            if (state.collection != null) {
                Timber.e("<<<Txn ${state.collection.status} and ${state.collection.id}")
                online_payment.visible()
                txn_id.text =
                    if (state.collection.paymentId.isNotNullOrBlank()) state.collection.paymentId else state.collection.id
                upi_id_to_title.text = state.collection.merchantName

                if (state.collection.merchantName.isNullOrBlank()) {
                    binding.llTxnTo.gone()
                    binding.dividerView.gone()
                } else {
                    binding.llTxnTo.visible()
                    binding.dividerView.visible()
                }

                when (state.collection.status) {
                    CollectionStatus.REFUNDED -> {
                        status = "Refunded"
                        llCustomerSupport.visible()
                        tv_payment_status.text = getString(R.string.refund_successful)
                        tv_payment_date.text = DateTimeUtils.formatLong(state.collection.create_time)
                        payment_status_divider.visible()
                        ll_payment_failed_status.visible()
                    }
                    CollectionStatus.FAILED -> {
                        status = "Failed"
                        llCustomerSupport.visible()
                        ll_payment_failed_status.visible()
                        payment_status_divider.visible()
                        tv_payment_status.text = getString(R.string.payment_failed)
                        tv_payment_date.text = DateTimeUtils.formatLong(state.collection.create_time)
                    }
                    else -> {
                        ll_payment_failed_status.gone()
                        payment_status_divider.gone()
                    }
                }
            } else {
                online_payment.gone()
            }

            edit_transaction_amount.isVisible = (state.isEditTxnAmountEnabled)

            showDotEducation(state)

            // show/hide alert
            if (state.error or state.isAlertVisible or state.networkError) {
                alert = when {
                    state.networkError -> view?.snackbar(
                        getString(R.string.home_no_internet_msg),
                        Snackbar.LENGTH_INDEFINITE
                    )
                    state.isAlertVisible -> view?.snackbar(state.alertMessage, Snackbar.LENGTH_INDEFINITE)
                    else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
                }
                alert?.show()
            } else {
                alert?.dismiss()
            }

            if (state.customer.mobile.isNullOrEmpty()) {
                binding.btnShare.gone()
            }

            val deletedTransaction = state.transaction.isDeleted
            val addedTransaction = state.transaction.isDeleted.not()

            when {
                addedTransaction && state.transaction.isCreatedByCustomer -> {
                    added_by_container.visible()
                    added_by_title.text =
                        Html.fromHtml(context?.getString(R.string.added_by_v2, state.customer.description))
                }
                deletedTransaction && state.transaction.isDeletedByCustomer -> {
                    added_by_container.visible()
                    added_by_title.text =
                        Html.fromHtml(context?.getString(R.string.deleted_by_v2, state.customer.description))
                }
                else -> {
                    added_by_container.gone()
                }
            }
        }

        setToolbarInfo(state)

        binding.recyclerView.withModels {
            imageCarousel {
                id("bill_carousel")
                models(
                    mutableListOf<EpoxyModel<*>>().apply {
                        add(
                            AddBillsViewModel_()
                                .id("add_bill_view")
                                .listener(this@TransactionFragment)
                        )
                        state.transaction?.receiptUrl?.let {
                            for (i in it.indices) {
                                add(
                                    ImageCarouselItemModel_()
                                        .id(state.transaction.receiptUrl!![i].url)
                                        .data(state.transaction.receiptUrl!![i])
                                        .listener(this@TransactionFragment)
                                )
                            }
                        }
                    }
                )
            }
        }

        state.transaction?.let { transaction ->
            when {
                transaction.amountUpdated -> {
                    editedOnContainerUIAfterAmountUpdated(transaction)
                    showViewHistoryLoader(state.showViewHistoryLoader)
                    viewEditedAmountHistory(state)
                }
                state.isEditTxnAmountEnabled -> {
                    editedOnContainerUIBeforeAmountUpdated()
                }
                else -> {
                    edited_on_container.gone()
                }
            }
        }

        if (state.canOpenNoteEditor) {
            binding.btnShare.gone()
            add_note_input_field.postDelayed(
                getAddNoteFieldHandler(add_note_input_field, state), 500
            )
        } else {
            binding.btnShare.visible()
            hideSoftKeyboard()
            binding.bottomTextContainer.gone()
        }
        add_note_input_field.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onNoteChanged.onNext(add_note_input_field.text.toString())
                    return true
                }
                return false
            }
        })
        state.transaction?.let {
            btn_submit.setOnClickListener {
                speechInput?.destroy()
                note.text = add_note_input_field.text.toString()
                if (note.text.isNotEmpty()) {
                    val flow: String =
                        if (getCurrentState().customer?.isLiveSales == true) {
                            PropertyValue.LINK_PAY
                        } else {
                            PropertyValue.EDIT_TRANSACTION
                        }
                    tracker.trackAddNoteCompleted(
                        flow,
                        "Customer",
                        getPaymentType(),
                        getInputMethod(),
                        getCurrentState().customer?.id,
                        getCurrentState().transaction?.id
                    )
                }
                onNoteSubmitClicked.onNext(Pair(note.text.toString(), state.transaction.id))
            }
        }

        binding.noteSuperLayout.post {
            try {
                binding.noteSuperLayout.requestFocus()
            } catch (e: Exception) {
            }
            binding.noteSuperLayout.setOnClickListener {
                state.transaction?.let {
                    if (it.isCreatedByCustomer.not())
                        noteInputState.onNext(true)
                }
            }
        }
        binding.noteImage.post {
            try {
                binding.noteImage.requestFocus()
            } catch (e: Exception) {
            }
            binding.noteImage.setOnClickListener {
                state.transaction?.let {
                    if (it.isCreatedByCustomer.not())
                        noteInputState.onNext(true)
                }
            }
        }
        binding.note.post {
            binding.note.requestFocus()
            binding.note.setOnClickListener {
                state.transaction?.let {
                    if (it.isCreatedByCustomer.not()) {
                        var method = "Add"
                        if (getCurrentState().transaction?.note?.length != 0) {
                            method = "Edit"
                        }
                        val flow: String =
                            if (getCurrentState().customer?.isLiveSales == true) {
                                PropertyValue.LINK_PAY
                            } else {
                                PropertyValue.EDIT_TRANSACTION
                            }
                        tracker.trackAddNoteClicked(
                            flow,
                            "Customer",
                            getPaymentType(),
                            method,
                            getCurrentState().customer?.id,
                            getCurrentState().transaction?.id
                        )
                        noteInputState.onNext(true)
                    }
                }
            }
        }

        binding.bottomTextContainer.setOnTouchListener { _, _ ->
            noteInputState.onNext(false)
            binding.bottomTextContainer.gone()
            KeyboardUtil.hideKeyboard(this)
            speechInput?.destroy()
            return@setOnTouchListener true
        }

        if (state.deleteStatus == DeleteLayoutStatus.InActive) {
            binding.deleteIv.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.grey900)
            binding.deleteText.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey900))
            binding.deleteText.setTextAppearance(requireContext(), R.style.TextAppearance_OKCTheme_Body2)
            binding.deleteText.text = Html.fromHtml(getString(R.string.cannot_delete_customer_transaction))
            if (state.isSingleListEnabled) {
                binding.deleteText.text =
                    Html.fromHtml(getString(R.string.single_list_cannot_delete_customer_transaction))
            }
        }
        checkForSubscriptionTransaction(state)
        setCashbackUi(state)
    }

    private fun setCashbackUi(state: State) {
        state.collection?.let {
            materialTextCashback.isVisible = it.cashbackGiven
            materialTextCashback.text =
                getString(R.string.SU2_Cashback_Discovery_body_2, state.customer?.description?.capitalizeWords() ?: "")
        }
    }

    private fun checkForSubscriptionTransaction(state: State) {
        if (state.showSubscription) {
            binding.subscriptionContainer.visible()
            binding.subscriptionDivider.visible()
            binding.subscriptionName.text = state.subscriptionName
        } else {
            binding.subscriptionDivider.gone()
            binding.subscriptionContainer.gone()
        }
    }

    private fun getAddNoteFieldHandler(addNote: EditText?, state: State): Runnable? {
        addNoteRunnable = Runnable {
            addNote?.let { note ->
                note.requestFocusFromTouch()
                KeyboardUtil.showKeyboard(binding.note.context, binding.note)
                binding.bottomTextContainer.visible()
                state.transaction?.note?.let {
                    note.setText(it)
                    note.setSelection(note.text.toString().length)
                }
                showSoftKeyboard(note)
            }
        }
        return addNoteRunnable
    }

    private fun showViewHistoryLoader(showViewHistoryLoader: Boolean) {
        if (showViewHistoryLoader) {
            binding.pbViewHistory.visible()
            binding.ivExpandIcon.gone()
        } else {
            binding.pbViewHistory.gone()
            binding.ivExpandIcon.visible()
        }
    }

    private fun showDotEducation(state: State) {
        if (state.isEditTxnAmountEnabled && state.isEditAmountEducationShown == false && isEditAmountEducationShown.not()) {
            isEditAmountEducationShown = true
            lottie_collection_highlighter.enableMergePathsForKitKatAndAbove(true)
            lottie_collection_highlighter.isVisible = true
            pushIntent(
                TransactionContract.Intent.RxPreferenceBoolean(
                    RxSharedPrefValues.IS_EDIT_AMOUNT_EDUCATION_SHOWN,
                    true,
                    Scope.Individual
                )
            )

            val type =
                getTxnType(state)
            tracker.trackEvents(
                Event.ACCOUNT_EDIT_AMOUNT_DOT_DISPLAYED,
                type = type,
                relation = PropertyValue.CUSTOMER,
                propertiesMap = PropertiesMap.create()
                    .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
            )
        }
    }

    internal fun getTxnType(state: State): String {
        return if (state.transaction?.type == merchant.okcredit.accounting.model.Transaction.PAYMENT || state.transaction?.type == merchant.okcredit.accounting.model.Transaction.RETURN) {
            PropertyValue.PAYMENT
        } else if (state.transaction?.type == merchant.okcredit.accounting.model.Transaction.CREDIT) {
            PropertyValue.CREDIT
        } else {
            "na"
        }
    }

    private fun editedOnContainerUIBeforeAmountUpdated() {
        edited_on_container.visible()
        iv_edit_icon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary))
        edited_on_date.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
        edited_on_date.text = getString(R.string.edit_transaction)
        binding.ivExpandIcon.gone()
    }

    private fun editedOnContainerUIAfterAmountUpdated(transaction: merchant.okcredit.accounting.model.Transaction) {
        edited_on_container.visible()
        iv_edit_icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey900))
        edited_on_date.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey700))
        transaction.amountUpdatedAt?.let {
            edited_on_date.text =
                getString(R.string.edited_on, DateTimeUtils.formatLong(transaction.amountUpdatedAt))
        }
    }

    private fun viewEditedAmountHistory(state: State) {
        binding.editAmountHistory.editAmountRecyclerView.let { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.withModels {
                if (state.isTxnViewExpanded) {
                    binding.ivExpandIcon.rotation = 180f
                    state.transactionAmountHistory?.let {

                        if (it.history.isNotEmpty()) {
                            val type = getTxnType(state)
                            tracker.trackEvents(
                                eventName = Event.ACCOUNT_EDIT_AMOUNT_HISTORY_VIEW,
                                type = type,
                                screen = "transaction_detail",
                                relation = PropertyValue.CUSTOMER,
                                propertiesMap = PropertiesMap.create()
                                    .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                                    .add(PropertyKey.COUNT, state.transactionAmountHistory.history.size)
                            )
                        }

                        it.history.forEachIndexed { index, history ->
                            transactionAmountHistoryView {
                                id("history_view_$index")
                                txnHistory(Pair(false, history))
                            }
                        }

                        transactionAmountHistoryView {
                            id("initial_amount_${it.transactionId}")
                            txnHistory(
                                Pair(
                                    true,
                                    History(
                                        newAmount = it.initialTransactionAmount,
                                        createdAt = it.initialTransactionCreatedAt
                                    )
                                )
                            )
                        }
                    }
                } else {
                    binding.ivExpandIcon.rotation = 0f
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        initListener()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RECORD_AUDIO_PERMISSION -> {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    // TODO
                } else {
                    onRecordAudioPermissionGranted()
                }
            }
        }
    }

    private fun requestRecordAudioPermission() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.RECORD_AUDIO
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION
            )
        } else {
            onRecordAudioPermissionGranted()
        }
    }

    private fun onRecordAudioPermissionGranted() {
        val inputTextLength = add_note_input_field.text.toString().length
        val maxInputLimit = requireContext().resources.getInteger(R.integer.max_transaction_note_input_limit)

        if (inputTextLength >= maxInputLimit) {
            return
        }
        if (speechInput == null) {
            speechInput = SpeechInput(
                speechRecognizer, requireActivity(),
                object : SpeechInput.OnSpeechInputListener {
                    override fun showAlertMessage(alertMessage: String) {
                        showAlert.onNext(alertMessage)
                    }

                    override fun onTextResult(result: String) {

                        speachText = if (speachText != null) {
                            if (!speachText?.trim().equals(result)) result else ""
                        } else {
                            result
                        }

                        if (add_note_input_field == null) {
                            return
                        }
                        voiceText = true
                        val noteText = if (add_note_input_field.text.toString().trim() == speachText?.trim()) {
                            result
                        } else {
                            add_note_input_field.text.toString() + " " + speachText
                        }
                        add_note_input_field.setText(noteText)
                        add_note_input_field.setSelection(if (noteText.length > 80) 80 else noteText.length)
                    }

                    override fun startVoiceIconAnimation() {
                        if (voiceIconAnimator == null) {
                            voiceIconAnimator = animateMe(voice_animation_view)
                        }
                        voiceIconAnimator?.start()
                        voice_icon.setBackgroundResource(R.drawable.dark_blue_voice_animation_icon)
                        voice_icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
                        add_note_input_field.hint = requireContext().resources.getString(R.string.listening)
                        voice_animation_view.visible()
                    }

                    override fun stopVoiceIconAnimation() {
                        voiceIconAnimator?.cancel()
                        if (voice_animation_view == null) {
                            return
                        }
                        voice_animation_view.gone()
                        voice_icon.setBackgroundColor(Color.TRANSPARENT)
                        add_note_input_field.hint = requireContext().resources.getString(R.string.add_note_optional)
                        voice_icon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary))
                    }
                }
            )
        }

        try {
            if (speechInput!!.isUserSpeaking) {
                speechInput!!.stopListening()
            } else {
                speechInput!!.startListening()
            }
        } catch (e: Exception) {
            if (e is GoogleVoiceTypingDisabledException)
                ExceptionUtils.logException(GoogleVoiceTypingDisabledException())
        }
    }

    private fun getInputMethod(): String {
        return if (voiceText && manualText) {
            "Fab & Voice"
        } else if (voiceText) {
            "Voice"
        } else {
            "Fab"
        }
    }

    internal fun getPaymentType(): String {
        var txnType = merchant.okcredit.accounting.model.Transaction.CREDIT
        try {
            txnType = getCurrentState().transaction?.type ?: merchant.okcredit.accounting.model.Transaction.CREDIT
        } catch (e: Exception) {
        }

        var type = "Credit"
        if (txnType == merchant.okcredit.accounting.model.Transaction.PAYMENT) {
            type = "Payment"
        } else if (txnType == merchant.okcredit.accounting.model.Transaction.CREDIT) {
            type = "Credit"
        }
        return type
    }

    private fun setDeliveryMessage(customer: Customer) {
        val deliverMessage: String = if (customer.isRegistered()) {
            getString(R.string.notification_delivered)
        } else {
            getString(R.string.sms_delivered)
        }
        sms_title.text = deliverMessage
        sms_title.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey700))
        sms_left_icon.setImageResource(R.drawable.ic_sms)
        sms_left_icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey900))
    }

    private fun setToolbarInfo(state: State) {
        if (state.customer != null) {
            screen_title.text = state.customer.description

            val defaultPic = TextDrawable
                .builder()
                .buildRound(
                    state.customer.description.substring(0, 1).toUpperCase(),
                    ColorGenerator.MATERIAL.getColor(state.customer.description)
                )

            Glide
                .with(this)
                .load(state.customer.profileImage)
                .circleCrop()
                .placeholder(defaultPic)
                .fallback(defaultPic)
                .into(binding.profileImage)
        }
    }

    /****************************************************************
     * Methods
     ****************************************************************/

    private fun shareImageInWhatsapp(bitmap: Bitmap, customer: Customer?) {
        val whatsappIntentBuilder = ShareIntentBuilder(
            shareText = getString(R.string.also_have_business, getCurrentState().referralId),
            phoneNumber = customer?.mobile,
            imageFrom = ImagePath.ImageUriFromBitMap(
                bitmap = bitmap,
                context = requireContext(),
                folderName = FOLDER_NAME_REMINDER,
                imageName = FILE_NAME_REMINDER
            )
        )
        communicationRepository.goToWhatsApp(whatsappIntentBuilder).map {
            startActivity(it)
        }.doOnError {
            if (it is IntentHelper.NoWhatsAppError) {
                showAlert.onNext(getString(R.string.whatsapp_not_installed))
            } else {
                showAlert.onNext(it.message ?: getString(R.string.err_default))
            }
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    internal fun animateMe(voiceIcon: ImageView): Animator {
        val animator = ObjectAnimator.ofFloat(voiceIcon, "translationX", -requireContext().dpToPixel(14f))
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 300L
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = ValueAnimator.INFINITE

        return animator
    }

    /****************************************************************
     * Lifecycle methods
     ****************************************************************/

    override fun onBackPressed(): Boolean {
        hideSoftKeyboard()
        return false
    }

    /****************************************************************
     * Navigation
     ****************************************************************/
    @UiThread
    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireContext())
        }
    }

    @UiThread
    override fun goToSmsApp(mobile: String, smsContent: String) {
        activity?.runOnUiThread {
            Analytics.setUserProperty(AnalyticsSuperProps.REMINDER_SMS, null)
            val whatsappIntentBuilder = ShareIntentBuilder(
                shareText = smsContent,
                phoneNumber = mobile
            )

            communicationRepository.goToSms(whatsappIntentBuilder).map {
                startActivity(it)
            }.doOnError {
                showAlert.onNext(it.message ?: getString(R.string.err_default))
            }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }

    @UiThread
    override fun goToDeletePage(transactionId: String) {
        activity?.runOnUiThread {
            legacyNavigator.goToDeleteTxnScreenForResult(requireActivity(), transactionId, DELETE_TXN_REQUEST)
        }
    }

    override fun goToWhatsappShare(
        customer: Customer,
        business: Business,
        transaction: merchant.okcredit.accounting.model.Transaction,
    ) {
        val bitmap = BitmapUtils.createTransactionClusterBitmap(requireContext(), customer, business, transaction)
        shareImageInWhatsapp(bitmap, customer)
    }

    override fun onPictureClicked(transactionImage: merchant.okcredit.accounting.model.TransactionImage) {
        getCurrentState().transaction?.let {
            if (it.isCreatedByCustomer) {
                return
            }
        }
        val selectdImage = CapturedImage(File(transactionImage.url))
        val allImageList = ArrayList<CapturedImage>()
        getCurrentState().transaction?.receiptUrl?.forEach {
            allImageList.add(CapturedImage(File(it.url)))
        }

        activity?.runOnUiThread {
            tracker.trackEditReceipt(
                "Edit Transaction",
                "Customer",
                getPaymentType(),
                getCurrentState().customer?.id,
                getCurrentState().transaction?.id
            )
            legacyNavigator.goToMultipleImageSelectedScreen(
                requireActivity(),
                MULTIPLE_IMAGE_SELECTION,
                selectdImage,
                allImageList,
                "Edit Transaction",
                "Customer",
                getPaymentType(),
                "Edit Screen",
                getCurrentState().customer?.id,
                getCurrentState().customer?.mobile,
                getCurrentState().transaction?.id
            )
        }
    }

    override fun onAddBillsClicked() {
        getCurrentState().transaction?.let {
            if (it.isCreatedByCustomer) {
                return
            }
        }

        checkForPermissionAndProceed()
    }

    private fun checkForPermissionAndProceed() {
        Permission.requestStorageAndCameraPermission(
            requireActivity(),
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                }

                override fun onPermissionGranted() {
                    var type = "Credit"
                    if (getCurrentState().transaction?.type == merchant.okcredit.accounting.model.Transaction.PAYMENT) {
                        type = "Payment"
                    } else if (getCurrentState().transaction?.type == merchant.okcredit.accounting.model.Transaction.CREDIT) {
                        type = "Credit"
                    }
                    val flow: String =
                        if (getCurrentState().customer?.isLiveSales == true) {
                            PropertyValue.LINK_PAY
                        } else {
                            PropertyValue.EDIT_TRANSACTION
                        }
                    tracker.trackAddReceiptStarted(
                        flow = flow,
                        relation = "Customer",
                        type = type,
                        screen = "Add Screen",
                        account = getCurrentState().customer?.id,
                        txnId = getCurrentState().transaction?.id
                    )
                    legacyNavigator.goToCameraActivity(
                        context = requireActivity(),
                        requestCode = CAMERA_ACTIVITY_REQUEST_CODE,
                        flow = flow,
                        relation = "Customer",
                        type = type,
                        screen = "Edit Screen",
                        account = getCurrentState().customer?.id,
                        mobile = getCurrentState().customer?.mobile,
                        existingImages = 0
                    )
                }

                override fun onPermissionDenied() {
                    longToast(R.string.camera_permission_denied)
                }
            }
        )
    }

    @UiThread
    override fun goBack() {
        activity?.runOnUiThread {
            activity?.finish()
        }
    }

    override fun goToKnowMoreScreen(it: String, accountType: String) {
        activity?.runOnUiThread {
            legacyNavigator.goToKnowMoreScreen(requireActivity(), it, accountType)
        }
    }

    override fun openWhatsApp(okCreditNumber: String) {
        val uri = Uri.parse("whatsapp://send")
            .buildUpon()
            .appendQueryParameter("text", getString(R.string.help_whatsapp_msg))
            .appendQueryParameter("phone", "91$okCreditNumber")
            .build()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        val packageManager = activity?.packageManager
        if (packageManager != null && intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            shortToast(getString(R.string.whatsapp_not_installed))
        }
    }

    override fun goToWhatsAppOptIn() {
        legacyNavigator.goToWhatsAppScreen(requireActivity())
    }

    private fun getTransactionType(): String? {
        return getCurrentState().transaction?.let {
            return@let if (it.type == merchant.okcredit.accounting.model.Transaction.CREDIT) {
                PropertyKey.CREDIT
            } else {
                PropertyKey.PAYMENT
            }
        }
    }

    private fun trackOnlineCollectionEvents(eventName: String, state: State) {
        if (state.collection == null) return

        val flow: String =
            if (getCurrentState().customer?.isLiveSales == true) {
                PropertyValue.LINK_PAY
            } else {
                PropertyValue.ONLINE_TRANSACTION
            }

        val status: String = when (state.collection.status) {
            CollectionStatus.PAID -> PropertyValue.PROCESSING
            CollectionStatus.FAILED -> PropertyValue.FAILED
            CollectionStatus.COMPLETE -> PropertyValue.COMPLETED
            else -> ""
        }
        tracker.trackEvents(
            eventName,
            type = PropertyValue.PAYMENT,
            screen = PropertyValue.TRANSACTION_DETAILS,
            relation = PropertyValue.CUSTOMER,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.FLOW, flow)
                .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                .add(PropertyKey.STATUS, status)
        )
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.SubscriptionDetail -> goToSubscriptionDetail(event.subscription)
        }
    }

    private fun goToSubscriptionDetail(subscription: Subscription) {
        startActivity(
            SubscriptionActivity.getIntent(
                requireContext(),
                subscription,
                getCurrentState().customer?.id ?: ""
            )
        )
    }

    override fun onSetPinClicked(requestCode: Int) {
        requireActivity().startActivityForResult(
            appLock.get()
                .appLock(getString(R.string.changepin_screen_deeplink), requireActivity(), "TransactionScreen"),
            requestCode
        )
    }

    override fun onDismissed() {}
    override fun onSetNewPinClicked(requestCode: Int) {
        requireActivity().startActivityForResult(
            appLock.get()
                .appLock(getString(R.string.changepin_screen_deeplink), requireActivity(), "TransactionScreen"),
            requestCode
        )
    }

    override fun onUpdateDialogDismissed() {
    }
}
