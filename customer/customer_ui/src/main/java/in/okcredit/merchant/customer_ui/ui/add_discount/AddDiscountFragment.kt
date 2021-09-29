package `in`.okcredit.merchant.customer_ui.ui.add_discount

import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.local.TapTargetLocal
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.ui.add_discount.AddDiscountContract.*
import `in`.okcredit.merchant.customer_ui.ui.add_discount.views.AddBillsView
import `in`.okcredit.merchant.customer_ui.ui.add_discount.views.MediaView
import `in`.okcredit.merchant.customer_ui.ui.add_discount.views.PermissionView
import `in`.okcredit.merchant.customer_ui.ui.add_discount.views.PictureView
import `in`.okcredit.merchant.customer_ui.utils.SpeechInput
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.calculator.CalculatorView
import `in`.okcredit.shared.calculator.calculatorView
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderScreen
import `in`.okcredit.shared.utils.CommonUtils
import `in`.okcredit.shared.utils.addTo
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.snackbar.Snackbar
import com.google.common.base.Strings
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.add_discount_fragment.*
import kotlinx.android.synthetic.main.transaction_item.*
import kotlinx.android.synthetic.main.txn_voice_input_layout.*
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.executeIfFragmentViewAvailable
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.KeyboardUtil
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import tech.okcredit.camera_contract.CapturedImage
import timber.log.Timber
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import zendesk.belvedere.Belvedere
import zendesk.belvedere.Callback
import zendesk.belvedere.MediaResult
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class AddDiscountFragment :
    BaseFragment<State, ViewEvent, AddDiscountContract.Intent>("AddDiscountScreen"),
    CalculatorView.CalcListener,
    DatePickerDialog.OnDateSetListener,
    DialogInterface.OnCancelListener,
    PermissionView.Listener,
    MediaView.Listener,
    PictureView.Listener,
    AddBillsView.Listener,
    InternetBottomSheet.InternetSheetListener,
    BottomSheetLoaderScreen.Listener {

    companion object {
        private const val STORAGE_AND_CAMERA_PERMISSIONS: Int = 3
        private const val MULTIPLE_IMAGE_REQUEST_CODE = 2
        private const val CAMERA_REQUEST_CODE = 1
    }

    private var imageList: ArrayList<CapturedImage> = ArrayList()
    private var mMobile: String? = null

    private val onDigitClicked: PublishSubject<Int> = PublishSubject.create()
    private val onOperatorClicked: PublishSubject<String> = PublishSubject.create()
    private val onEqualsClicked: PublishSubject<Unit> = PublishSubject.create()
    private val onDotClicked: PublishSubject<Unit> = PublishSubject.create()
    private val onLongBackPress: PublishSubject<Unit> = PublishSubject.create()
    private val onBackPressClicked: PublishSubject<Unit> = PublishSubject.create()
    private val onChangeInputMode: PublishSubject<Int> = PublishSubject.create()
    private val onChangeDate: PublishSubject<DateTime> = PublishSubject.create()
    private val tryDiscountAddAgain: PublishSubject<Unit> = PublishSubject.create()
    private val onChangeImage: PublishSubject<ArrayList<CapturedImage>> = PublishSubject.create()
    private val onDeleteImage: PublishSubject<Unit> = PublishSubject.create()
    private val hideDateDialogueVisibility: PublishSubject<Unit> = PublishSubject.create()
    private val showAlert: PublishSubject<String> = PublishSubject.create()
    private val setSpeechAnimationVisibility: PublishSubject<Boolean> = PublishSubject.create()
    private var alert: Snackbar? = null
    private var datePickerDialog: DatePickerDialog? = null
    private var imageEditDialog: AlertDialog? = null
    private var activeInputMode: Int = -1
    private var isPasswordVisible: Boolean = false
    private var disposable: Disposable? = null
    private var isShowedNoteTutorial = false
    private var manualText = false
    private var voiceText = false

    private var alertDialog: DatePickerDialog? = null
    private var mSpeechInput: SpeechInput? = null

    private var bottomSheetLoader: BottomSheetLoaderScreen? = null
    private var isBottomSheetShown: Boolean = false

    @Inject
    internal lateinit var imageLoader: Lazy<IImageLoader>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var localeManager: Lazy<LocaleManager>

    @Inject
    lateinit var localInAppNotificationHandler: Lazy<LocalInAppNotificationHandler>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_discount_fragment, container, false)
    }

    override fun loadIntent(): UserIntent {
        return AddDiscountContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            amount_box.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                        KeyboardVisibilityEvent.hideKeyboard(context, add_note_input_field)
                    }
                }
                .delay(200, TimeUnit.MILLISECONDS)
                .map {
                    AddDiscountContract.Intent.OnChangeInputMode(AddDiscountContract.INPUT_MODE_AMOUNT)
                },

            btn_forgot_password.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    AddDiscountContract.Intent.OnForgotPasswordClicked
                },

            profile_image.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    AddDiscountContract.Intent.GoToCustomerProfile
                },

            profile_name.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    AddDiscountContract.Intent.GoToCustomerProfile
                },

            btn_submit.clicks()
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .map {

                    if (add_note_input_field.text.toString().isNotBlank())
                        tracker.get().trackAddNoteCompleted(
                            "Add Discount",
                            "Customer",
                            getPaymentType(),
                            getInputMethod(),
                            getCurrentState().customer?.id
                        )
                    mSpeechInput?.destroy()

                    val recieptImageList =
                        if (getCurrentState().imageList.isNullOrEmpty()) null else getCurrentState().imageList
                    if (!isShowedNoteTutorial && getCurrentState().showNoteTutorial) {
                        showNoteTutorial()
                        AddDiscountContract.Intent.NoChangeIntent
                    } else if (getCurrentState().isPassWordEnable && getCurrentState().isPasswordSet && getCurrentState().activeInputMode != AddDiscountContract.INPUT_MODE_PASSWORD &&
                        getCurrentState().txType == merchant.okcredit.accounting.model.Transaction.PAYMENT
                    ) {
                        AddDiscountContract.Intent.AddTransaction(note = add_note_input_field.text.toString())
                    } else if (getCurrentState().activeInputMode == AddDiscountContract.INPUT_MODE_PASSWORD) {
                        initBottomLoader()
                        AddDiscountContract.Intent.SubmitPassword(
                            amount = getCurrentState().amount,
                            password = add_note_input_field.text.toString(),
                            phtoList = recieptImageList,
                            billDate = getCurrentState().date,
                            txType = getCurrentState().txType,
                            note = getCurrentState().note,
                            isPasswordVerifyRequired = true
                        )
                    } else {
                        initBottomLoader()
                        AddDiscountContract.Intent.SubmitPassword(
                            amount = getCurrentState().amount,
                            password = "",
                            phtoList = recieptImageList,
                            billDate = getCurrentState().date,
                            txType = getCurrentState().txType,
                            note = add_note_input_field.text.toString(),
                            isPasswordVerifyRequired = false
                        )
                    }
                },

            setSpeechAnimationVisibility
                .map {
                    AddDiscountContract.Intent.SetSpeechAnimationVisibility(it)
                },

            onDigitClicked
                .map { AddDiscountContract.Intent.OnDigitClicked(it) },

            onOperatorClicked
                .map { AddDiscountContract.Intent.OnOperatorClicked(it) },

            onEqualsClicked
                .map { AddDiscountContract.Intent.OnEqualClicked },

            onDotClicked
                .map { AddDiscountContract.Intent.OnDotClicked },

            onLongBackPress
                .map { AddDiscountContract.Intent.OnLongPressBackSpace },

            onBackPressClicked
                .map { AddDiscountContract.Intent.OnBackSpaceClicked },

            onChangeInputMode
                .map { AddDiscountContract.Intent.OnChangeInputMode(it) },

            onChangeDate
                .map { AddDiscountContract.Intent.OnChangeDate(it) },

            onChangeImage
                .map { AddDiscountContract.Intent.OnChangeImage(it) },

            onDeleteImage
                .map { AddDiscountContract.Intent.OnDeleteImage },

            showAlert
                .map { AddDiscountContract.Intent.ShowAlert(it) },

            tryDiscountAddAgain.map {
                AddDiscountContract.Intent.TryDiscountAddAgain
            }

        )
    }

    private fun initBottomLoader() {
        if (bottomSheetLoader == null) {
            bottomSheetLoader = BottomSheetLoaderScreen()
            bottomSheetLoader?.setListener(this)
            bottomSheetLoader?.isCancelable = false
        }
    }

    private fun getInputMethod(): String? {
        return if (voiceText && manualText) {
            "Fab & Voice"
        } else if (voiceText) {
            "Voice"
        } else {
            "Fab"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        date_text_new.setOnClickListener {
            Analytics.track(
                AnalyticsEvents.SELECT_BILL_DATE,
                EventProperties
                    .create()
                    .with(PropertyKey.RELATION, PropertyValue.CUSTOMER)
            )

            if (activity != null) {
                if (!requireActivity().isFinishing) {
                    alertDialog = datePickerDialog
                    datePickerDialog?.show()
                }
            }
        }

        image_container.setOnClickListener {
            if (activity != null) {
                if (!requireActivity().isFinishing) {
                    imageEditDialog?.show()
                }
            }
        }

        bottom_container_right_icon.setOnClickListener {
            if (getCurrentState().activeInputMode == AddDiscountContract.INPUT_MODE_PASSWORD) {
                if (isPasswordVisible) {
                    add_note_input_field.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                    bottom_container_right_icon.setImageResource(R.drawable.ic_remove_eye)
                } else {
                    add_note_input_field.inputType = InputType.TYPE_CLASS_NUMBER
                    bottom_container_right_icon.setImageResource(R.drawable.ic_eye_off)
                }
                add_note_input_field.post {
                    add_note_input_field.text?.length?.let { it1 ->
                        add_note_input_field.setSelection(
                            it1
                        )
                    }
                }
                isPasswordVisible = !isPasswordVisible
            } else if (getCurrentState().activeInputMode == AddDiscountContract.INPUT_MODE_PERMISSION ||
                getCurrentState().activeInputMode == AddDiscountContract.INPUT_MODE_MEDIA
            ) {
                onChangeInputMode.onNext(AddDiscountContract.INPUT_MODE_AMOUNT)
            }
        }

        add_note_btn.setOnClickListener {
            if (activeInputMode != AddDiscountContract.INPUT_MODE_PASSWORD) {
                KeyboardVisibilityEvent.showKeyboard(context, add_note_input_field, root_view)
                onChangeInputMode.onNext(AddDiscountContract.INPUT_MODE_NOTE)
            }
        }
        add_note_input_field.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && activeInputMode != AddDiscountContract.INPUT_MODE_PASSWORD) {
                KeyboardVisibilityEvent.showKeyboard(context, add_note_input_field, root_view)
                onChangeInputMode.onNext(AddDiscountContract.INPUT_MODE_NOTE)
            }
        }

        try {
            KeyboardVisibilityEvent.setEventListener(requireActivity()) { isOpen ->
                if (isOpen) {
                    if (activeInputMode != AddDiscountContract.INPUT_MODE_PASSWORD) {
                        onChangeInputMode.onNext(AddDiscountContract.INPUT_MODE_NOTE)
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            RecordException.recordException(e)
        }
        root_view.setTracker(performanceTracker)
    }

    @SuppressLint("ObsoleteSdkInt")
    @AddTrace(name = Traces.RENDER_ADD_Transaction)
    override fun render(state: State) {
        Timber.i("amountCalculation value :%s", state.amountCalculation)

        mMobile = state.customer?.mobile

        amount_box.visibility = View.VISIBLE
        amount_divider.visibility = View.VISIBLE
        text_amount_calculation.visibility = View.VISIBLE
        new_date_container.visibility = View.GONE
        picture_list_rv.visibility = View.GONE
        tx_container.visibility = View.GONE
        btn_forgot_password.visibility = View.GONE
        add_note_input_field.error = null

        add_note_btn.setImageResource(R.drawable.ic_note_add)
        add_note_input_field.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_CLASS_TEXT
        add_note_input_field.maxLines = 3
        add_note_input_field.hint = getString(R.string.add_note_optional)

        text_amount_calculation.text = state.amountCalculation?.replace("*", "x")

        // Set Add TRANSACTION info
        if (state.amount == 0L) {
            bottom_text_container.visibility = View.GONE
            info.visibility = View.GONE
            text_amount.visibility = View.GONE
            amount_divider.visibility = View.GONE
            text_amount.visibility = View.GONE
            new_date_container.visibility = View.GONE
            picture_list_rv.visibility = View.GONE
            add_credit_text.visibility = View.VISIBLE
        } else {
            val autoTransition = AutoTransition()
            autoTransition.duration = 150
            if (text_amount.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(amount_container, autoTransition)
            }

            if (bottom_text_container.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(bottom_container)
            }

            bottom_text_container.visibility = View.VISIBLE
            info.visibility = View.GONE
            text_amount.visibility = View.VISIBLE
            amount_divider.visibility = View.VISIBLE
            text_amount.visibility = View.VISIBLE
            new_date_container.visibility = View.GONE
            picture_list_rv.visibility = View.GONE

            text_amount.text = CurrencyUtil.formatV2(state.amount)

            add_credit_text.visibility = View.GONE

            state.amountCalculation?.let {
                if (state.amountCalculation.contains("*") ||
                    state.amountCalculation.contains("+") ||
                    state.amountCalculation.contains("-") ||
                    state.amountCalculation.contains("/")
                ) {
                    text_amount.visibility = View.VISIBLE
                } else {
                    text_amount.visibility = View.GONE
                }
            }
        }

        if (activeInputMode != state.activeInputMode ||
            state.activeInputMode == AddDiscountContract.INPUT_MODE_PASSWORD
        ) {
            recycler_view.withModels {
                when (state.activeInputMode) {
                    AddDiscountContract.INPUT_MODE_AMOUNT -> {
                        add_note_input_field.clearFocus()
                        if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                            KeyboardVisibilityEvent.hideKeyboard(context, add_note_input_field)
                        }
                        calculatorView {
                            id("calculatorView")
                            listener(this@AddDiscountFragment)
                        }

                        if (activeInputMode == AddDiscountContract.INPUT_MODE_PASSWORD) {
                            add_note_input_field.setText(state.note)
                        }
                    }

                    AddDiscountContract.INPUT_MODE_MEDIA -> {
                        add_note_input_field.clearFocus()
                        if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                            KeyboardVisibilityEvent.hideKeyboard(context, add_note_input_field)
                        }

                        val type = getPaymentType()

                        tracker.get().trackAddReceiptStarted(
                            "Add Discount",
                            "Customer",
                            type,
                            "Add Screen",
                            state.customer?.id
                        )
                        val existingImageListSize = getCurrentState().imageList.size
                        legacyNavigator.get().goToCameraActivity(
                            requireActivity(),
                            CAMERA_REQUEST_CODE,
                            "Add Discount",
                            "Customer",
                            type,
                            "Add Screen",
                            state.customer?.id,
                            state.customer?.mobile,
                            existingImageListSize
                        )
                        onChangeInputMode.onNext(AddDiscountContract.INPUT_MODE_DEFAULT)
                    }

                    AddDiscountContract.INPUT_MODE_PASSWORD -> {
                        if (activeInputMode == AddDiscountContract.INPUT_MODE_NOTE ||
                            activeInputMode == AddDiscountContract.INPUT_MODE_PASSWORD
                        ) {
                            Observable
                                .timer(200, TimeUnit.MILLISECONDS)
                                .subscribe {
                                    if (!state.isIncorrectPassword) {
                                        if (!KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                                            KeyboardVisibilityEvent.showKeyboard(
                                                context,
                                                add_note_input_field,
                                                root_view
                                            )
                                        }
                                    }
                                }.addTo(autoDisposable)
                        }
                        activeInputMode = state.activeInputMode

                        // Hack fix for crash. kotlinx synthetic view return null when user is returning from the page.
                        // layout that’s actually inflated with no checks against invalid lookups and don't expose nullability
                        // when views are only present in some configuration. here it could be
                        // because of it's inside build models of recyclerview
                        if (add_note_btn == null || add_note_input_field == null) {
                            return@withModels
                        }

                        add_note_btn.setImageResource(R.drawable.ic_lock)
                        bottom_container_right_icon.setImageResource(R.drawable.ic_remove_eye)

                        add_note_input_field.inputType =
                            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                        isPasswordVisible = false
                        add_note_input_field.hint = getString(R.string.login_enter_password)
                        add_note_input_field.text = null
                        btn_forgot_password.visibility = View.VISIBLE
                        bottom_container_right_icon.visibility = View.GONE
                        voice_icon_container.visibility = View.GONE

                        if (!KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                            KeyboardVisibilityEvent.showKeyboard(context, add_note_input_field, root_view)
                        }

                        tx_container.visibility = View.VISIBLE
                        amount_box.visibility = View.GONE
                        amount_divider.visibility = View.GONE
                        text_amount_calculation.visibility = View.GONE
                        new_date_container.visibility = View.GONE
                        picture_list_rv.visibility = View.GONE
                        image_container.visibility = View.GONE

                        tx_container.requestLayout()

                        CurrencyUtil.renderV2(state.amount, tx_amount, state.txType)
                        tx_date.text = DateTimeUtils.format(CommonUtils.currentDateTime())

                        if (Strings.isNullOrEmpty(state.note)) {
                            tx_note.visibility = View.GONE
                        } else {
                            tx_note.visibility = View.VISIBLE
                            tx_note.text = state.note
                        }

                        if (state.imageList.isNotEmpty() && Strings.isNullOrEmpty(state.imageList.get(0).file.path)) {
                            tx_image_container.visibility = View.GONE
                        } else {

                            if (state.imageList.isNotEmpty()) {
                                tx_image_container.visibility = View.VISIBLE
                                context?.let {
                                    Glide.with(it.applicationContext)
                                        .load(Uri.fromFile(File(state.imageList.get(0).file.path))).into(tx_bill)
                                }
                            }
                        }

                        if (state.customer != null) {
                            val totalDue: Long = state.customer.balanceV2 + state.amount

                            if (totalDue < 0) {
                                total_amount.text = getString(R.string.due) + " " + CurrencyUtil.formatV2(totalDue)
                            } else {
                                total_amount.text = getString(R.string.advance) + " " + CurrencyUtil.formatV2(totalDue)
                            }
                        }

                        if (activeInputMode != AddDiscountContract.INPUT_MODE_PASSWORD) {
                            add_note_input_field.setText(state.password)
                        }
                    }
                }
            }
            activeInputMode = state.activeInputMode
        }

        date_text_new?.postDelayed(
            {
                date_text_new?.setText(DateTimeUtils.formatDateOnly(state.date), TextView.BufferType.NORMAL)
            },
            400
        )

        // Set color based on tx type
        text_amount_calculation.setTextColor(ContextCompat.getColor(requireContext(), R.color.tx_discount))
        rupee_symbol.setTextColor(ContextCompat.getColor(requireContext(), R.color.tx_discount))
        add_credit_text.setTextColor(ContextCompat.getColor(requireContext(), R.color.tx_discount))
        image_divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tx_discount))
        tx_image_divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tx_discount))
        cursor.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tx_discount))
        add_credit_text.text = getString(R.string.add_discount)

        // Set Amount Error Visibility
        if (state.amountError) {
            error_amount.visibility = View.VISIBLE
            error_amount.text = getString(R.string.txn_invalid_amount)
            amount_divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red_primary))

            TransitionManager.beginDelayedTransition(amount_container)
            AnimationUtils.shake(amount_container)
        } else {
            TransitionManager.beginDelayedTransition(amount_container)

            error_amount.visibility = View.GONE
            amount_divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.divider))
        }

        if (state.activeInputMode != AddDiscountContract.INPUT_MODE_AMOUNT) {
            cursor.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        if (localeManager.get().getLanguage() == LocaleManager.LANGUAGE_HINGLISH) {
            Locale.setDefault(LocaleManager.englishLocale)
        }

        // Set Date Picker dialog
        datePickerDialog = DatePickerDialog(
            requireContext(),
            this@AddDiscountFragment,
            state.date.year,
            state.date.monthOfYear.minus(1),
            state.date.dayOfMonth
        )
        if (datePickerDialog?.datePicker != null) {
            datePickerDialog?.datePicker?.maxDate = CommonUtils.currentDateTime().millis
        }

        datePickerDialog?.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.ok), datePickerDialog)
        datePickerDialog?.setButton(DatePickerDialog.BUTTON_NEGATIVE, getString(R.string.cancel), datePickerDialog)
        datePickerDialog?.setOnCancelListener(this@AddDiscountFragment)

        // Set Image
        if (!state.imageList.isNullOrEmpty()) {
            context?.let {
                if (state.activeInputMode != AddDiscountContract.INPUT_MODE_PASSWORD) {
                    image_container.visibility = View.GONE
                    setTxImageDialogueInfo(state.imageList[0].file.path)

                    Glide
                        .with(it)
                        .load(Uri.fromFile(File(state.imageList[0].file.path)))
                        .centerCrop()
                        .into(image_view)
                }

                if (state.activeInputMode == AddDiscountContract.INPUT_MODE_PASSWORD) {
                    bottom_container_right_icon.visibility = View.GONE
                    voice_icon_container.visibility = View.GONE
                } else {
                    bottom_container_right_icon.visibility = View.GONE
                    voice_icon_container.visibility = View.GONE
                }
            }
        } else {
            imageEditDialog?.dismiss()
            bottom_container_right_icon.visibility = View.GONE
            voice_icon_container.visibility = View.GONE
            image_container.visibility = View.GONE
        }

        // Set Customer info
        setToolbarInfo(state)

        if (state.isAlertVisible || state.error) {
            alert = if (state.isAlertVisible) {
                view?.snackbar(state.alertMessage, Snackbar.LENGTH_LONG)
            } else {
                view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_LONG)
            }
            alert?.show()
        } else {
            alert?.dismiss()
        }
        this.imageList = state.imageList

        bottomSheetLoader?.let {
            if (state.isSubmitLoading.not() && state.isSubmitSuccess) {
                it.success()
            } else if (state.isSubmitLoading.not() && state.isSubmitSuccess.not()) {
                it.failed(state.isSubmitFailureMessage)
            } else if (state.isSubmitLoading) {
                it.load()
            }
            if (it.isAdded.not() && isBottomSheetShown.not()) {
                isBottomSheetShown = true
                it.show(childFragmentManager, BottomSheetLoaderScreen.TAG)
            }
            hideSoftKeyboard()
        }
    }

    private fun getPaymentType(): String {
        return "Payment"
    }

    private fun showNoteTutorial() {
        isShowedNoteTutorial = true
        Analytics.track(
            AnalyticsEvents.IN_APP_NOTI_DISPLAYED,
            EventProperties.create()
                .with(PropertyKey.TYPE, "note")
                .with(PropertyKey.SCREEN, "add_txn_screen")
        )

        lifecycleScope.launch {
            localInAppNotificationHandler.get()
                .generateTapTarget(
                    weakScreen = WeakReference(requireActivity()),
                    tapTarget = TapTargetLocal(
                        screenName = label,
                        targetView = WeakReference(add_note_btn),
                        title = getString(R.string.note_tutorial),
                        titleTextSize = 16f,
                        titleGravity = Gravity.END,
                        listener = { _, state ->
                            executeIfFragmentViewAvailable {
                                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                    Analytics.track(
                                        AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                        EventProperties.create()
                                            .with(PropertyKey.TYPE, "note")
                                            .with("focal_area", true)
                                            .with(PropertyKey.SCREEN, "add_txn_screen")
                                    )
                                } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                    Analytics.track(
                                        AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                        EventProperties.create()
                                            .with(PropertyKey.TYPE, "note")
                                            .with("focal_area", false)
                                            .with(PropertyKey.SCREEN, "add_txn_screen")
                                    )
                                }
                            }
                        }
                    )
                )
        }
    }

    override fun onResume() {
        super.onResume()

        var eventSent = false
        add_note_input_field.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    manualText = true
                    if (!eventSent) {
                        eventSent = true
                        tracker.get().trackAddNoteStarted(
                            "Add Discount",
                            "Customer",
                            getPaymentType(),
                            "Fab",
                            getCurrentState().customer?.id
                        )
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        val animation = AlphaAnimation(0.5f, 0f)
        animation.duration = 500
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = Animation.INFINITE
        animation.repeatMode = Animation.REVERSE
        cursor.startAnimation(animation)
    }

    override fun onStop() {
        super.onStop()
        if (disposable != null) {
            if (!disposable!!.isDisposed) {
                disposable!!.dispose()
            }
        }
        if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
            KeyboardVisibilityEvent.hideKeyboard(context, add_note_input_field)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (alertDialog != null && alertDialog?.isShowing == true) {
            alertDialog?.dismiss()
        }
    }

    override fun onBackPressed(): Boolean {

        var originScreen = AddDiscountContract.ORIGIN_CUSTOMER_SCREEN
        try {
            originScreen = getCurrentState().originScreen
        } catch (e: Exception) {
        }

        if (activeInputMode == AddDiscountContract.INPUT_MODE_PASSWORD) {
            recycler_view?.clear()
            onChangeInputMode.onNext(AddDiscountContract.INPUT_MODE_AMOUNT)
        } else {
            activity?.finish()
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (data?.getSerializableExtra("addedImages") != null) {
                val listPhotos = data.getSerializableExtra("addedImages") as ArrayList<CapturedImage>
                imageList.addAll(listPhotos)
                onChangeImage.onNext(imageList)
                onChangeInputMode.onNext(AddDiscountContract.INPUT_MODE_AMOUNT)
            }
        } else if (requestCode == MULTIPLE_IMAGE_REQUEST_CODE) {
            if (data?.getSerializableExtra("finalSelectedImagelist") != null) {
                val finalSelectedImagelist =
                    data.getSerializableExtra("finalSelectedImagelist") as ArrayList<CapturedImage>
                onChangeImage.onNext(finalSelectedImagelist)
                onChangeInputMode.onNext(AddDiscountContract.INPUT_MODE_AMOUNT)
            }
        } else {
            context?.let {
                Belvedere.from(it).getFilesFromActivityOnResult(
                    requestCode,
                    resultCode,
                    data,
                    object : Callback<List<MediaResult>>() {
                        override fun success(result: List<MediaResult>) {
                            if (result.isNotEmpty()) {
                                Glide
                                    .with(it)
                                    .load(Uri.fromFile(result[0].file))
                                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                    .into(profile_image)

                                result[0].file?.let { file ->
                                    // Timber.i("""XYZ:file path ${result[0].file?.path}""")
                                    val list = ArrayList<CapturedImage>()
                                    list.add(CapturedImage(file))
                                    onChangeImage.onNext(list)
                                    onChangeInputMode.onNext(AddDiscountContract.INPUT_MODE_AMOUNT)
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    private fun askCameraAndGalleryPermission() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.CAMERA
                )
            != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CAMERA
                ),
                STORAGE_AND_CAMERA_PERMISSIONS
            )
        } else {
            if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                KeyboardVisibilityEvent.hideKeyboard(context, add_note_input_field)
            }
            Observable
                .timer(300, TimeUnit.MILLISECONDS)
                .subscribe {
                    onChangeInputMode.onNext(AddDiscountContract.INPUT_MODE_MEDIA)
                }
        }
    }

    private fun setToolbarInfo(state: State) {
        if (state.customer != null) {
            profile_name.text = state.customer.description

            CurrencyUtil.renderV2(state.customer.balanceV2, due, 0)

            val defaultPic = TextDrawable
                .builder()
                .buildRound(
                    state.customer.description.substring(0, 1).toUpperCase(),
                    ColorGenerator.MATERIAL.getColor(state.customer.description)
                )

            if (Strings.isNullOrEmpty(state.customer.profileImage)) {
                profile_image.setImageDrawable(defaultPic)
            } else {
                disposable = imageLoader.get().context(this)
                    .load(state.customer.profileImage)
                    .placeHolder(defaultPic)
                    .scaleType(IImageLoader.CIRCLE_CROP)
                    .into(profile_image)
                    .build()
            }
        }
    }

    private fun setTxImageDialogueInfo(receiptUrl: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_txn_image_dialog, null)
        builder.setView(dialogView)
        dialogView.setPadding(0, 0, 0, 0)

        imageEditDialog = builder.create()

        val dialogLoading = dialogView.findViewById<ProgressBar>(R.id.dialog_loading)
        val dialogReceipt = dialogView.findViewById<PhotoView>(R.id.dialog_receipt)
        val dialogCameraBtn = dialogView.findViewById<LinearLayout>(R.id.dialog_camera)
        val dialogGalleryBtn = dialogView.findViewById<LinearLayout>(R.id.dialog_gallery)
        val dialogDeleteBtn = dialogView.findViewById<LinearLayout>(R.id.dialog_delete)

        dialogLoading.visibility = View.VISIBLE
        dialogReceipt.visibility = View.VISIBLE
        Glide.with(requireContext())
            .load(Uri.fromFile(File(receiptUrl)))
            .error(R.drawable.ic_no_recipt)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    dialogLoading.visibility = View.GONE
                    dialogReceipt.visibility = View.VISIBLE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    dialogLoading.visibility = View.GONE
                    dialogReceipt.visibility = View.VISIBLE
                    return false
                }
            })
            .into(dialogReceipt)

        dialogDeleteBtn.setOnClickListener {
            Analytics.track(
                AnalyticsEvents.DELETE_RECEIPT,
                EventProperties
                    .create()
                    .with(PropertyKey.RELATION, PropertyValue.CUSTOMER)
            )
            onDeleteImage.onNext(Unit)
        }

        dialogCameraBtn.setOnClickListener {
            Analytics.track(
                AnalyticsEvents.ADD_RECEIPT,
                EventProperties.create()
                    .with(PropertyKey.TYPE, "camera")
                    .with(PropertyKey.SOURCE, "dialog")
                    .with(PropertyKey.RELATION, PropertyValue.CUSTOMER)
            )
            onCameraClickedFromBottomSheet()
        }

        dialogGalleryBtn.setOnClickListener {
            Analytics.track(
                AnalyticsEvents.ADD_RECEIPT,
                EventProperties.create()
                    .with(PropertyKey.TYPE, "gallery")
                    .with(PropertyKey.SOURCE, "dialog")
                    .with(PropertyKey.RELATION, PropertyValue.CUSTOMER)
            )
            onGalleryClickedFromBottomSheet()
        }
    }

    override fun onDigitClicked(d: Int) {
        onDigitClicked.onNext(d)
    }

    override fun onOperatorClicked(d: String) {
        onOperatorClicked.onNext(d)
    }

    override fun onEqualsClicked() {
        onEqualsClicked.onNext(Unit)
    }

    override fun onDotClicked() {
        onDotClicked.onNext(Unit)
    }

    override fun onBackspaceLongPress() {
        onLongBackPress.onNext(Unit)
    }

    override fun onBackspaceClicked() {
        onBackPressClicked.onNext(Unit)
    }

    // On Date Changed
    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)

        val newDate = DateTime(calendar.timeInMillis)

        Analytics.track(
            AnalyticsEvents.UPDATE_BILL_DATE,
            EventProperties
                .create()
                .with("default", getCurrentState().date.withTimeAtStartOfDay() == newDate.withTimeAtStartOfDay())
                .with(PropertyKey.RELATION, PropertyValue.CUSTOMER)
        )

        onChangeDate.onNext(newDate)
        hideDateDialogueVisibility.onNext(Unit)
    }

    // Dismissing billDate dialog
    override fun onCancel(p0: DialogInterface?) {
        hideDateDialogueVisibility.onNext(Unit)
    }

    // Clicking camera allow on bottom sheet
    override fun onCameraClickedFromBottomSheet() {
        imageEditDialog?.dismiss()
        Belvedere.from(requireContext())
            .camera()
            .open(requireActivity())
    }

    // Clicking gallery allow on bottom sheet
    override fun onGalleryClickedFromBottomSheet() {
        imageEditDialog?.dismiss()
        Belvedere.from(requireContext())
            .document()
            .contentType("image/*")
            .allowMultiple(false)
            .open(requireActivity())
    }

    // Clicking permission allow on bottom sheet
    override fun onAllowPermissionClicked() {
        askCameraAndGalleryPermission()
    }

    private fun showPasswordError() {
        if (add_note_input_field != null) {
            add_note_input_field.setError(getString(R.string.txn_incorrect_password), null)
            btn_forgot_password.strokeColor = requireContext().getColorFromAttr(R.attr.colorPrimary)
            forgot_password_icon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary))
            forgot_password_text.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
        }
    }

    private fun gotoInternetPopup() {
        val internetBottomSheet = InternetBottomSheet.netInstance()
        if (!internetBottomSheet.isVisible) {
            internetBottomSheet.show(requireActivity().supportFragmentManager, InternetBottomSheet.TAG)
            internetBottomSheet.initialise(this@AddDiscountFragment)
        }
    }

    private fun dismissBottomLoader() {
        bottomSheetLoader?.dismiss()
        bottomSheetLoader = null
        isBottomSheetShown = false
    }

    private fun gotoTxSuccessScreen() {

        tracker.get().trackAddDiscountCompleted(
            "Relationship Screen",
            PropertyValue.CUSTOMER,
            getCurrentState().customer?.id,
            "Discount",
            getCurrentState().amount
        )

        KeyboardUtil.hideKeyboard(context as Activity)
        activity?.finish()
    }

    private fun gotoForgotPasswordScreen(mobile: String) {
        legacyNavigator.get().goToForgotPasswordScreen(requireActivity(), mobile)
    }

    private fun gotoCustomerProfile(customerId: String?) {
        tracker.get().trackViewProfile(
            PropertyValue.ADD_TXN,
            PropertyValue.CUSTOMER,
            PropertyValue.CUSTOMER,
            customerId
        )
        legacyNavigator.get().gotoCustomerProfile(requireActivity(), customerId!!)
    }

    override fun onPictureClicked(capturedImage: CapturedImage) {
        tracker.get().trackEditReceipt(
            "Add Discount",
            "Customer",
            getPaymentType(),
            getCurrentState().customer?.id
        )

        legacyNavigator.get().goToMultipleImageSelectedScreen(
            requireActivity(),
            MULTIPLE_IMAGE_REQUEST_CODE,
            capturedImage,
            imageList,
            "Add Discount",
            "Customer",
            getPaymentType(),
            "Add Screen",
            getCurrentState().customer?.id,
            getCurrentState().customer?.mobile
        )
    }

    override fun onAddBillsClicked() {
        askCameraAndGalleryPermission()
    }

    override fun onTryAgainClicked() {
        tryDiscountAddAgain.onNext(Unit)
    }

    override fun onRetry() {
        bottomSheetLoader?.dismiss()
        bottomSheetLoader = null
        isBottomSheetShown = false
        btn_submit.performClick()
    }

    override fun onCancel() {
        bottomSheetLoader?.dismiss()
        bottomSheetLoader = null
        isBottomSheetShown = false
    }

    override fun onSuccess() {
        bottomSheetLoader?.dismiss()
        bottomSheetLoader = null
        isBottomSheetShown = false
        gotoTxSuccessScreen()
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToForgotPasswordScreen -> gotoForgotPasswordScreen(event.mobile)
            ViewEvent.GoToTxSuccessScreen -> gotoTxSuccessScreen()
            is ViewEvent.GoToCustomerProfile -> gotoCustomerProfile(event.customerId)
            ViewEvent.ShowPasswordError -> showPasswordError()
            ViewEvent.GoToInternetPopup -> gotoInternetPopup()
            ViewEvent.DismissBottomLoader -> dismissBottomLoader()
        }
    }
}
