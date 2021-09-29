package `in`.okcredit.merchant.customer_ui.ui.discount_details

import `in`.okcredit.analytics.*
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.backend.utils.SmsHelper
import `in`.okcredit.backend.utils.Utils
import `in`.okcredit.collection.contract.CollectionStatus
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.ui.add_discount.views.AddBillsView
import `in`.okcredit.merchant.customer_ui.ui.add_discount.views.AddBillsViewModel_
import `in`.okcredit.merchant.customer_ui.ui.discount_details.views.ImageCarouselItem
import `in`.okcredit.merchant.customer_ui.ui.discount_details.views.ImageCarouselItemModel_
import `in`.okcredit.merchant.customer_ui.ui.discount_details.views.imageCarousel
import `in`.okcredit.merchant.customer_ui.utils.SpeechInput
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.ScreenName
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.text.Html
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.EpoxyModel
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.bill_carousel.*
import kotlinx.android.synthetic.main.discount_details_screen.*
import kotlinx.android.synthetic.main.txn_voice_input_layout.*
import merchant.okcredit.accounting.model.Transaction
import merchant.okcredit.accounting.model.TransactionImage
import tech.okcredit.android.base.extensions.dpToPixel
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.camera_contract.CapturedImage
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class DiscountDetailsFragment :
    BaseScreen<DiscountDetailsContract.State>("DiscountDetailsScreen"),
    DiscountDetailsContract.Navigator,
    ImageCarouselItem.Listener,
    AddBillsView.Listener {

    private val CAMERA_ACTIVITY_REQUEST_CODE: Int = 3
    private val RECORD_AUDIO_PERMISSION: Int = 4
    private val onNewImagesAdded: PublishSubject<Triple<ArrayList<CapturedImage>, List<TransactionImage>, String>> =
        PublishSubject.create()
    private val MULTIPLE_IMAGE_SELECTION: Int = 2
    private val syncNow: PublishSubject<Unit> = PublishSubject.create()
    private val onNoteSubmitClicked: PublishSubject<Pair<String, String>> = PublishSubject.create()
    private val openSmsApp: PublishSubject<Unit> = PublishSubject.create()

    private val showAlert: PublishSubject<String> = PublishSubject.create()
    private val noteInputState: PublishSubject<Boolean> = PublishSubject.create()
    private val onKnowMoreClicked: PublishSubject<String> = PublishSubject.create()
    private val onImagesChanged: PublishSubject<Pair<DiscountDetailsContract.ImagesInfo, Boolean>> =
        PublishSubject.create()
    private val changedImages: PublishSubject<Triple<ArrayList<TransactionImage>, ArrayList<TransactionImage>, String>> =
        PublishSubject.create()
    internal val whatsAppUsPublishSubject: PublishSubject<Boolean> = PublishSubject.create()
    private var manualText = false
    internal var voiceText = false

    companion object {
        const val DELETE_TXN_REQUEST = 1
    }

    private var voiceIconAnimator: Animator? = null

    @Inject
    internal lateinit var speechRecognizer: SpeechRecognizer

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var imageLoader: IImageLoader

    @Inject
    internal lateinit var smsHelper: SmsHelper

    @Inject
    internal lateinit var tracker: Tracker

    private var alert: Snackbar? = null
    private var mSpeechInput: SpeechInput? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.discount_details_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contextual_help.initDependencies(
            screenName = ScreenName.TxnDetailsScreen.value,
            tracker = tracker,
            legacyNavigator = legacyNavigator
        )
        recycler_view.isNestedScrollingEnabled = false
        add_note_input_field.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_CLASS_TEXT
        add_note_input_field.maxLines = 3
        initListener()
        root_view.setTracker(performanceTracker)
    }

    private fun initListener() {
        llAddedOn.setOnClickListener {
            tracker.trackTransactionDetails(
                eventName = Event.ADDED_ON,
                relation = PropertyValue.CUSTOMER,
                type = getTransactionType()!!,
                accountId = getCurrentState().customer!!.id
            )
        }

        date_container.setOnClickListener {
            tracker.trackTransactionDetails(
                eventName = Event.DATE_CLICKED,
                relation = PropertyValue.CUSTOMER,
                type = getTransactionType()!!,
                accountId = getCurrentState().customer!!.id
            )
        }

        rlAddedAmount.setOnClickListener {
            tracker.trackTransactionDetails(
                eventName = Event.AMOUNT_CLICKED,
                relation = PropertyValue.CUSTOMER,
                type = getTransactionType()!!,
                accountId = getCurrentState().customer!!.id
            )
        }

        tv_contact_us.setOnClickListener {
            Permission.requestContactPermission(
                activity as AppCompatActivity,
                object : IPermissionListener {
                    override fun onPermissionGrantedFirstTime() {
                        tracker.trackRuntimePermission(PropertyValue.TRANSACTION_DETAILS, PropertyValue.CONTACT, true)
                    }

                    @SuppressLint("CheckResult")
                    override fun onPermissionGranted() {

                        // timer is provided because on onResume() of fragment and race condition
                        // when permission dialog dismisses , onResume() is called and fragment reloads every Intent from start
                        // in this scenarios importContactSubject.onNext(showLoading to showRefresh) call gets rejected,causing code to not execute
                        // so,we execute it after 500 milliseconds (after onResume())
                        Observable.timer(500, TimeUnit.MILLISECONDS)
                            .subscribe {
                                whatsAppUsPublishSubject.onNext(true)
                                tracker.trackEvents(Event.CONTACT_OKCREDIT, source = PropertyValue.TRANSACTION_DETAILS)
                            }
                    }

                    @SuppressLint("CheckResult")
                    override fun onPermissionDenied() {
                        Observable.timer(500, TimeUnit.MILLISECONDS)
                            .subscribe {
                                whatsAppUsPublishSubject.onNext(false)
                                tracker.trackRuntimePermission(
                                    PropertyValue.TRANSACTION_DETAILS,
                                    PropertyValue.CONTACT,
                                    false
                                )
                            }
                    }
                }
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == DELETE_TXN_REQUEST) {
            activity?.setResult(Activity.RESULT_OK, data)
            activity?.finish()
        }
        if (requestCode == MULTIPLE_IMAGE_SELECTION) {
            val imagesInfo = DiscountDetailsContract.ImagesInfo()
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
                            TransactionImage(
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

            onImagesChanged.onNext(imagesInfo to (getCurrentState().transaction?.isDirty ?: false))
        } else if (requestCode == CAMERA_ACTIVITY_REQUEST_CODE) {
            if (data?.getSerializableExtra("addedImages") != null) {
                val imagesInfo = DiscountDetailsContract.ImagesInfo()
                val transactionId = getCurrentState().transaction?.id
                imagesInfo.transactionId = transactionId

                getCurrentState().transaction?.receiptUrl?.let {
                    imagesInfo.tempImages.addAll(it)
                }
                val newlyAddedList = data.getSerializableExtra("addedImages") as ArrayList<CapturedImage>

                if (newlyAddedList.size > 0) {
                    newlyAddedList.let {
                        imagesInfo.newAddedImages.addAll(it)
                        it.forEach {
                            val timestamp = DateTimeUtils.currentDateTime()
                            imagesInfo.tempImages.add(
                                TransactionImage(
                                    transactionId,
                                    UUID.randomUUID().toString(),
                                    transactionId,
                                    it.file.path,
                                    timestamp
                                )
                            )
                        }
                    }
                    onImagesChanged.onNext(imagesInfo to (getCurrentState().transaction?.isDirty ?: false))
                }
            }
        }
    }

    override fun loadIntent(): UserIntent {
        return DiscountDetailsContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            syncNow
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { DiscountDetailsContract.Intent.SyncTransaction },

            openSmsApp
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { DiscountDetailsContract.Intent.OpenSmsApp },

            showAlert
                .map { DiscountDetailsContract.Intent.ShowAlert(it) },

            btn_delete.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    tracker.trackDeleteDiscount(
                        getCurrentState().customer?.id,
                        getCurrentState().transaction?.id,
                        PropertyValue.CUSTOMER
                    )
                    DiscountDetailsContract.Intent.Delete
                },

            btn_share.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Analytics.track(
                        AnalyticsEvents.SHARE_TXN,
                        EventProperties
                            .create()
                            .with(PropertyKey.ACCOUNT_ID, getCurrentState().customer?.id)
                            .with("customer_id", getCurrentState().customer?.id)
                            .with("transaction_id", getCurrentState().transaction?.id)
                            .with(PropertyKey.RELATION, PropertyValue.CUSTOMER)
                    )
                    DiscountDetailsContract.Intent.ShareOnWhatsApp
                },
            noteInputState.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    DiscountDetailsContract.Intent.Note(it)
                },
            changedImages
                .map {
                    DiscountDetailsContract.Intent.ImagesChanged(it)
                },
            onNewImagesAdded.map {
                DiscountDetailsContract.Intent.NewImagesAdded(it)
            },
            onNoteSubmitClicked.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    DiscountDetailsContract.Intent.NoteSubmitClicked(it)
                },
            onImagesChanged
                .map {
                    DiscountDetailsContract.Intent.OnImagesChanged(it.first, it.second)
                },
            onKnowMoreClicked.throttleFirst(300, TimeUnit.MILLISECONDS).map {
                DiscountDetailsContract.Intent.OnKnowMoreClicked(it)
            },

            whatsAppUsPublishSubject
                .map {
                    DiscountDetailsContract.Intent.WhatsApp(it)
                }
        )
    }

    @AddTrace(name = Traces.RENDER_TRANSACTION_DETAILS)
    override fun render(state: DiscountDetailsContract.State) {
        contextual_help.setContextualHelpIds(state.contextualHelpIds)
        if (state.transaction != null && state.customer != null && sync_container != null) {

            sync_container.setOnClickListener {

                if (state.transaction.isDirty) {
                    tracker.trackTransactionDetails(
                        eventName = Event.SYNC_CLICKED,
                        relation = PropertyValue.CUSTOMER,
                        type = getTransactionType() ?: "",
                        accountId = state.customer.id,
                        status = PropertyValue.FAILED
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
                    tracker.trackTransactionDetails(
                        eventName = Event.SYNC_CLICKED,
                        relation = PropertyValue.CUSTOMER,
                        type = getTransactionType()!!,
                        accountId = state.customer.id,
                        status = PropertyValue.SUCCESS
                    )
                }
            }

            sms_container.setOnClickListener {
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
                    (
                        if (it.isRegistered()) {
                            PropertyValue.NOTIFICATION
                        } else {
                            PropertyValue.SMS
                        }
                        ).also { channel = it }
                }
                tracker.trackTransactionDetails(
                    eventName = Event.COMMUNICATION_DELIVERED,
                    relation = PropertyValue.CUSTOMER,
                    type = getTransactionType()!!,
                    accountId = state.customer.id,
                    channel = channel
                )
            }

            voice_icon.setOnClickListener {
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

            @ColorRes var color = R.color.tx_discount

            rupee_symbol.setTextColor(ContextCompat.getColor(requireContext(), color))
            amount.setTextColor(ContextCompat.getColor(requireContext(), color))

            amount.text = CurrencyUtil.formatV2(state.transaction.amountV2)

            date.text = getString(R.string.bill_on, DateTimeUtils.formatDateOnly(state.transaction.billDate))
            created_date.text = getString(R.string.added_on_date, DateTimeUtils.formatLong(state.transaction.createdAt))
            screen_title.text = "Discount Details"
            if (state.transaction.isDeleted.not()) {
                delete_text.text = "Delete Discount"
            }

            if (!state.transaction.isDirty) {
                syc_title.text = getString(R.string.txn_synced)
                syc_title.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey700))
                sync_left_icon.setImageResource(R.drawable.ic_single_tick)
                sync_left_icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey900))

                setDeliveryMessage()
            } else {
                syc_title.text = getString(R.string.txn_sync_now)
                syc_title.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
                sync_left_icon.setImageResource(R.drawable.clock_outline)
                sync_left_icon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary))

                if (state.transaction.isSmsSent || state.isSmsSent) {
                    setDeliveryMessage()
                } else {
                    sms_title.text = getString(R.string.sms_from_phone)
                    sms_title.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
                    sms_left_icon.setImageResource(R.drawable.ic_reply_black_24dp)
                    sms_left_icon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary))
                }
            }

            added_by_left_icon.setImageResource(R.drawable.ic_single_tick)

            if (state.customer.mobile.isNullOrEmpty()) {
                sms_container.visibility = View.GONE
                sms_divider.visibility = View.GONE
            } else {
                sms_container.visibility = View.VISIBLE
                sms_divider.visibility = View.VISIBLE
            }

            if (state.transaction.isDeleted) {
                delete_container.visibility = View.VISIBLE
                delete_divider.visibility = View.VISIBLE
                btn_delete.visibility = View.GONE
                btn_share.visibility = View.GONE
                rupee_symbol.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey400))
                amount.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey400))
                deleted_date.text =
                    "${getString(R.string.deleted_on)} : ${DateTimeUtils.formatLong(state.transaction.deleteTime)}"
            } else {
                delete_container.visibility = View.GONE
                delete_divider.visibility = View.GONE
                btn_share.visibility = View.GONE
                if (!state.collection?.id.isNullOrEmpty() || state.customer.status != 1) {
                    btn_delete.visibility = View.GONE
                } else {
                    btn_delete.visibility = View.VISIBLE
                }
            }

            if (state.collection != null) {
                Timber.e("<<<Txn ${state.collection.status} and ${state.collection.id}")
                online_payment.visibility = View.GONE
                txn_id.text = state.collection.id
                upi_id_to_title.text = state.collection.merchantName

                when (state.collection.status) {
                    CollectionStatus.REFUNDED -> {
                        ll_payment_failed_status.visibility = View.VISIBLE
                        ll_payment_failed.visibility = View.VISIBLE
                        payment_status_divider.visibility = View.VISIBLE
                        tv_contact_us.visibility = View.GONE
                        tv_contact_us.text = Html.fromHtml(getString(R.string.contact_us))
                        tv_payment_failed.text = getString(R.string.error_msg_for_paymetn_refund)
                        tv_payment_status.text = getString(R.string.payment_refunded)
                        tv_payment_date.text = DateTimeUtils.formatLong(state.collection.create_time)
                    }
                    CollectionStatus.FAILED -> {
                        ll_payment_failed_status.visibility = View.VISIBLE
                        ll_payment_failed.visibility = View.VISIBLE
                        payment_status_divider.visibility = View.VISIBLE
                        tv_contact_us.visibility = View.VISIBLE
                        tv_contact_us.text = Html.fromHtml(getString(R.string.contact_us))
                        tv_payment_failed.text = getString(R.string.error_msg_for_payment_failed)
                        tv_payment_status.text = getString(R.string.payment_failed)
                        tv_payment_date.text = DateTimeUtils.formatLong(state.collection.create_time)
                    }
                    else -> {
                        ll_payment_failed_status.visibility = View.GONE
                        ll_payment_failed.visibility = View.GONE
                        payment_status_divider.visibility = View.GONE
                    }
                }
            } else {
                online_payment.visibility = View.GONE
            }

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
                btn_share.visibility = View.GONE
            }

            val deletedTransaction = state.transaction.isDeleted
            val addedTransaction = state.transaction.isDeleted.not()

            when {
                addedTransaction && state.transaction.isCreatedByCustomer -> {
                    added_by_container.visibility = View.VISIBLE
                    added_by_title.text =
                        Html.fromHtml(context?.getString(R.string.added_by_v2, state.customer.description))
                }
                deletedTransaction && state.transaction.isDeletedByCustomer -> {
                    added_by_container.visibility = View.VISIBLE
                    added_by_title.text =
                        Html.fromHtml(context?.getString(R.string.deleted_by_v2, state.customer.description))
                }
                else -> {
                    added_by_container.visibility = View.GONE
                }
            }
        }

        setToolbarInfo(state)

        recycler_view.withModels {

            imageCarousel {
                id("bill_carousel")
                models(
                    mutableListOf<EpoxyModel<*>>().apply {
                        add(
                            AddBillsViewModel_()
                                .id("add_bill_view")
                                .listener(this@DiscountDetailsFragment)
                        )
                        state.transaction?.receiptUrl?.let {
                            for (i in it.indices) {
                                add(
                                    ImageCarouselItemModel_()
                                        .id(state.transaction.receiptUrl!![i].url)
                                        .data(state.transaction.receiptUrl!![i])
                                        .listener(this@DiscountDetailsFragment)
                                )
                            }
                        }
                    }
                )
            }
        }

        if (state.deleteStatus == DiscountDetailsContract.DeleteLayoutStatus.InActive) {
            delete_iv?.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.grey900)
            delete_text.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey900))
            delete_text.setTextAppearance(requireContext(), R.style.TextAppearance_OKCTheme_Body2)
            delete_text.text = Html.fromHtml(getString(R.string.cannot_delete_customer_transaction))
        }
        state.transaction?.let {
            if (it.note.isNotNullOrBlank()) {
                note_super_layout.visibility = View.VISIBLE
                note.text = state.transaction.note
            } else {
                note_super_layout.visibility = View.GONE
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RECORD_AUDIO_PERMISSION -> {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
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
        if (mSpeechInput == null) {
            mSpeechInput = SpeechInput(
                speechRecognizer, requireActivity(),
                object : SpeechInput.OnSpeechInputListener {
                    override fun showAlertMessage(alertMessage: String) {
                        showAlert.onNext(alertMessage)
                    }

                    override fun onTextResult(result: String) {
                        if (add_note_input_field == null) {
                            return
                        }
                        voiceText = true
                        val noteText = add_note_input_field.text.toString() + " " + result
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
                        voice_animation_view.visibility = View.VISIBLE
                    }

                    override fun stopVoiceIconAnimation() {
                        voiceIconAnimator?.cancel()
                        if (voice_animation_view == null) {
                            return
                        }
                        voice_animation_view.visibility = View.GONE
                        voice_icon.setBackgroundColor(Color.TRANSPARENT)
                        add_note_input_field.hint = requireContext().resources.getString(R.string.add_note_optional)
                        voice_icon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary))
                    }
                }
            )
        }

        if (mSpeechInput!!.isUserSpeaking) {
            mSpeechInput!!.stopListening()
        } else {
            mSpeechInput!!.startListening()
        }
    }

    private fun getInputMethod(): String? {
        val inputMethod: String?
        if (voiceText && manualText) {
            inputMethod = "Fab & Voice"
        } else if (voiceText) {
            inputMethod = "Voice"
        } else {
            inputMethod = "Fab"
        }
        return inputMethod
    }

    private fun getPaymentType(): String {
        var txnType = Transaction.CREDIT
        try {
            txnType = getCurrentState().transaction?.type ?: Transaction.CREDIT
        } catch (e: Exception) {
        }

        var type = "Credit"
        if (txnType == Transaction.PAYMENT) {
            type = "Payment"
        } else if (txnType == Transaction.CREDIT) {
            type = "Credit"
        }
        return type
    }

    private fun setDeliveryMessage() {
        sms_title.text = getString(R.string.discount_sms_update_sent)
        sms_title.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey700))
        sms_left_icon.setImageResource(R.drawable.ic_sms)
        sms_left_icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey900))
    }

    private fun setToolbarInfo(state: DiscountDetailsContract.State) {
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
                .into(profile_image)
        }
    }

    /****************************************************************
     * Methods
     ****************************************************************/

    private fun animateMe(voiceIcon: ImageView): Animator {
        val animator = ObjectAnimator.ofFloat(voiceIcon, "translationX", -(requireContext().dpToPixel(14f)))
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
        if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
            KeyboardVisibilityEvent.hideKeyboard(activity)
        }
        return false
    }

    /****************************************************************
     * Navigation
     ****************************************************************/
    @UiThread
    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    @UiThread
    override fun goToSmsApp(mobile: String, smsContent: String) {
        activity?.runOnUiThread {
            smsHelper.openSmsAppForReminders(smsContent, mobile)
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
        transaction: Transaction
    ) {
    }

    override fun onPictureClicked(transactionImage: TransactionImage) {

        val selectedImage = CapturedImage(File(transactionImage.url))
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
                selectedImage,
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
        var type = "Credit"
        if (getCurrentState().transaction?.type == Transaction.PAYMENT) {
            type = "Payment"
        } else if (getCurrentState().transaction?.type == Transaction.CREDIT) {
            type = "Credit"
        }
        activity?.runOnUiThread {
            tracker.trackAddReceiptStarted(
                "Edit Transaction",
                "Customer",
                type,
                "Add Screen",
                getCurrentState().customer?.id,
                getCurrentState().transaction?.id
            )

            legacyNavigator.goToCameraActivity(
                requireActivity(),
                CAMERA_ACTIVITY_REQUEST_CODE,
                "Edit Transaction",
                "Customer",
                type,
                "Edit Screen",
                getCurrentState().customer?.id,
                getCurrentState().customer?.mobile,
                0
            )
        }
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
            return@let if (it.type == Transaction.CREDIT) {
                PropertyKey.CREDIT
            } else {
                PropertyKey.PAYMENT
            }
        }
    }
}
