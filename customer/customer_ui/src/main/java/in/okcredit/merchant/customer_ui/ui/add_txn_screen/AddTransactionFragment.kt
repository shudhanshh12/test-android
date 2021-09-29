package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.SuperProperties
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.service.in_app_notification.MixPanelInAppNotificationTypes.CALCULATOR_EDUCATION
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.local.TapTargetLocal
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.*
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.customer_ui.databinding.AddTransactionScreenOldBinding
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTransactionContract.AddTxnViewEvent
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTransactionContract.State
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Source.ADD_TRANSACTION_SHORTCUT_SCREEN
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.controller.AddBillController
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.controller.models.AddBillModel
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.AddBillsView
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.PictureView
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.RoboflowPictureView
import `in`.okcredit.merchant.customer_ui.ui.dialogs.RemoveDetailsDialog
import `in`.okcredit.merchant.customer_ui.ui.dialogs.RemoveDetailsDialog.*
import `in`.okcredit.merchant.customer_ui.ui.payment.AmountFocusChangeListener
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker.Companion.EDIT_AMOUNT_CLICKED
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker.Companion.ENTER_AMOUNT_MANUALLY_CLICKED
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker.Companion.LOADING
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker.Companion.PREDICTION_FAILED
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker.Companion.PREDICTION_SUCCESS
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker.Companion.UPLOAD_FAILURE
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.models.RoboflowState
import `in`.okcredit.merchant.customer_ui.utils.SpeechInput
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.calculator.CalculatorContract
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderScreen
import `in`.okcredit.shared.utils.CommonUtils
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.transition.TransitionManager
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.camera.CameraActivity
import com.camera.selected_image.MultipleImageActivity
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.OnBalloonClickListener
import com.skydoves.balloon.createBalloon
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import merchant.okcredit.accounting.model.Transaction.Companion.CREDIT
import merchant.okcredit.accounting.model.Transaction.Companion.PAYMENT
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.*
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.contract.AppLock
import tech.okcredit.contract.Constants
import tech.okcredit.contract.OnUpdatePinClickListener
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.lang.ref.WeakReference
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AddTransactionFragment :
    BaseFragment<State, AddTxnViewEvent, AddTransactionContract.Intent>(
        "AddTxnFragment", R.layout.add_transaction_screen_old
    ),
    DatePickerDialog.OnDateSetListener,
    CalculatorContract.Callback,
    PictureView.Listener,
    RoboflowPictureView.Listener,
    AddBillsView.Listener,
    BottomSheetLoaderScreen.Listener,
    OnUpdatePinClickListener,
    SpeechInput.OnSpeechInputListener {

    private var calculatorOperatorsUsed: String? = null
    internal var bottomSheetLoader: BottomSheetLoaderScreen? = null
    private var isBottomSheetShown: Boolean = false
    private var txnType = -1
    private var voiceIconAnimator: Animator? = null
    private var speechInput: SpeechInput? = null
    private var isAlreadyClearedFocus: Boolean = false
    private var isToolTipAlreadyShown: Boolean = false

    private var amountFocusChangeListener: AmountFocusChangeListener? = null

    @Inject
    lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var customerEventTracker: Lazy<CustomerEventTracker>

    @Inject
    lateinit var roboflowEventTracker: Lazy<RoboflowEventTracker>

    @Inject
    internal lateinit var speechRecognizer: Lazy<SpeechRecognizer>

    @Inject
    lateinit var controller: Lazy<AddBillController>

    // using only for navigate to customerProfileActivity,
    // before using legacyNavigator here contact (Harshit)
    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var appLock: Lazy<AppLock>

    @Inject
    lateinit var localInAppNotificationHandler: Lazy<LocalInAppNotificationHandler>

    private var noteStartedEventSent = false

    private val binding: AddTransactionScreenOldBinding by viewLifecycleScoped(AddTransactionScreenOldBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAmountFormatter()
        initListeners()
        initBottomLoader()
        initAddNote()

        binding.pictureListRv.setController(controller.get())
    }

    private fun initListeners() = binding.apply {
        dateTextNew.debounceClickListener {
            pushIntent(AddTransactionContract.Intent.SelectBillDateClicked)
        }
        btnSubmit.debounceClickListener {
            if (calculatorOperatorsUsed.isNotNullOrBlank()) {
                customerEventTracker.get().trackInputCalculator(
                    calculatorOperatorsUsed!!,
                    getCurrentState().customer?.id ?: "",
                )
            }
            pushIntent(
                AddTransactionContract.Intent.SubmitClicked(
                    binding.noteEditText.text.toString(),
                    getCurrentState().amount
                )
            )
        }
        sendOrReceivePayment.addCreditBtn.debounceClickListener {
            pushIntent(
                AddTransactionContract.Intent.AddTransactionThroughShortCut(
                    note = binding.noteEditText.text.toString(),
                    txnType = CREDIT
                )
            )
        }
        sendOrReceivePayment.addPaymentBtn.debounceClickListener {
            pushIntent(
                AddTransactionContract.Intent.AddTransactionThroughShortCut(
                    note = binding.noteEditText.text.toString(),
                    txnType = PAYMENT
                )
            )
        }
        roboflowEnterAmountManually.debounceClickListener {
            if (isStateInitialized() &&
                getCurrentState().roboflowState is RoboflowState.RoboflowFetchSuccess
            ) {
                pushIntent(AddTransactionContract.Intent.EditAmount)
            } else {
                pushIntent(AddTransactionContract.Intent.EnterAmountManuallyCancelUploadReceipt)
            }
        }

        textInputAmount.debounceClickListener {
            if (getCurrentState().isRoboflowEnabled) {
                calculatorLayout.visible()
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    pushIntent(AddTransactionContract.Intent.AddedAmountFirst)
                }
            }
        }
        voiceIcon.debounceClickListener { requestRecordAudioPermission() }
    }

    private fun initAmountFormatter() = binding.apply {
        etAmount.setOnFocusChangeListener { _, hasFocus ->
            amountFocusChangeListener?.onAmountFocusChange(hasFocus)
            if (hasFocus) {
                hideSoftKeyboard()
                binding.calculatorLayout.visible()
            } else {
                binding.calculatorLayout.gone()
            }
        }
        etAmount.showSoftInputOnFocus = false
        calculatorLayout.setData(this@AddTransactionFragment, 0, "")
        etAmount.disableCopyPaste()
        etAmount.requestFocus()
    }

    private fun initAddNote() = binding.apply {
        noteEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                calculatorLayout.gone()
                showSoftKeyboard(noteEditText)
            } else {
                hideSoftKeyboard(noteEditText)
                calculatorLayout.visible()
            }
        }

        noteEditText.doAfterTextChanged {
            val state = if (isStateInitialized()) getCurrentState() else null
            if (it != null && it.isNotEmpty() && !noteStartedEventSent && isVisible) {
                noteStartedEventSent = true
                val paymentType = state?.getPaymentType() ?: ""
                customerEventTracker.get().trackAddNoteStarted(
                    type = paymentType,
                    screenView = "Add Transaction",
                    accountId = state?.customer?.id ?: "",
                )
            }
        }
    }

    private fun hideErrorAfterDelay() {
        lifecycleScope.launchWhenCreated {
            binding.apply {
                delay(5000)
                errorAmount.gone()
                viewAmountBorder.setBackgroundColor(getColorCompat(R.color.grey300))
            }
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun render(state: State) {
        txnType = state.txType
        checkForTxnType(state.txType)
        checkForBillImages(state)
        checkForAmountAdded(state)
        checkForNoteAndSubmitButton(state)
        checkForBillDateAdded(state)
        checkForRoboflowFeature(state)
        checkForAddTransactionShortcut(state)
        checkAddNoteFocused()
    }

    private fun checkAddNoteFocused() {
        if (binding.noteEditText.hasFocus() && binding.calculatorLayout.isVisible) {
            binding.calculatorLayout.gone()
            binding.noteEditText.postDelayed(
                {
                    showSoftKeyboard(binding.noteEditText)
                },
                200
            )
        }
    }

    private fun checkForRoboflowFeature(state: State) {
        binding.apply {
            if (state.isRoboflowEnabled.not()) return

            binding.pictureListRv.visible()

            if (!isAlreadyClearedFocus) {
                binding.etAmount.clearFocus()
                isAlreadyClearedFocus = true
            }

            doImageOnboardingChanges(state)

            if (state.amountAdded() &&
                !state.isImageAdded
            ) {
                return
            }

            if (state.billDate.isNotBlank()) {
                dateTextNew.setText(state.billDate, TextView.BufferType.NORMAL)
            }
            checkForRoboflowState(state)
        }
    }

    private fun checkForRoboflowState(state: State) = binding.apply {
        state.roboflowState?.also {
            when (it) {
                is RoboflowState.RoboflowFetchInProgress -> {
                    roboflowEnterAmountManually.visible()
                    textAmount.visible()
                    roboflowLoadingGroup.visible()
                    textInputAmount.invisible()
                    viewAmountBorder.gone()
                    calculatorLayout.gone()
                    newDateContainer.visible()
                    noteAndAmountGroup.visible()
                    etAmount.isEnabled = false
                    etAmount.clearFocus()

                    val image = getCurrentState().addBillModels[1] as? AddBillModel.PictureView
                    startVectorDrawableAnimation(roboflowLoading, image?.image)
                    textAmount.text = getString(R.string.amount_will_be_updated_in_a_few_seconds)
                    btnSubmit.isEnabled = false
                }
                is RoboflowState.RoboflowFetchSuccess -> {
                    roboflowEnterAmountManually.visible()
                    textInputAmount.visible()
                    viewAmountBorder.gone()
                    newDateContainer.visible()
                    noteAndAmountGroup.visible()
                    textAmount.visible()
                    calculatorLayout.gone()
                    roboflowLoadingGroup.gone()
                    enabledSubmit()
                    etAmount.clearFocus()
                    etAmount.isEnabled = false

                    clearVectorDrawableAnimationCallbacks(roboflowLoading)

                    etAmount.setText(CurrencyUtil.formatV2(state.predictedAmount ?: 0))
                    textAmount.text = getString(R.string.amount_updated)
                    roboflowEnterAmountManually.text = getString(R.string.edit_amount)
                }
                is RoboflowState.RoboflowFetchFailed -> {
                    clearVectorDrawableAnimationCallbacks(roboflowLoading)
                    textInputAmount.visible()
                    viewAmountBorder.visible()
                    calculatorLayout.visible()
                    textAmount.visible()
                    roboflowLoadingGroup.gone()
                    roboflowEnterAmountManually.gone()
                    newDateContainer.visible()
                    noteAndAmountGroup.visible()
                    btnSubmit.isEnabled = true
                    etAmount.isEnabled = true
                    etAmount.requestFocus()

                    textAmount.text = getString(R.string.roboflow_fetch_failed_message)
                }
                is RoboflowState.EnterAmountManuallyCancelUploadReceipt,
                is RoboflowState.EditAmount,
                -> {
                    hideSoftKeyboard()
                    textInputAmount.visible()
                    viewAmountBorder.visible()
                    calculatorLayout.visible()
                    textAmount.visible()
                    roboflowLoadingGroup.gone()
                    roboflowEnterAmountManually.gone()
                    newDateContainer.visible()
                    noteAndAmountGroup.visible()
                    enabledSubmit()
                    etAmount.isEnabled = true
                    etAmount.requestFocus()

                    clearVectorDrawableAnimationCallbacks(roboflowLoading)
                    textAmount.text = getString(R.string.always_make_sure_that_photo_is_easy_to_read)
                }
                is RoboflowState.MultipleReceiptAreAdded -> {
                    textAmount.visible()
                    calculatorLayout.visible()
                    viewAmountBorder.visible()
                    textInputAmount.visible()
                    newDateContainer.visible()
                    noteAndAmountGroup.visible()
                    roboflowLoadingGroup.gone()
                    etAmount.requestFocus()
                    etAmount.isEnabled = true
                    clearVectorDrawableAnimationCallbacks(roboflowLoading)
                    textAmount.text =
                        getString(R.string.currently_automatic_amount_entry_supported)
                }
                is RoboflowState.ShowAddBillToolTip -> {
                    pictureListRv.post { showRoboflowAddBillTooltip() }
                }
                is RoboflowState.InternetNotAvailable -> {
                    clearVectorDrawableAnimationCallbacks(roboflowLoading)
                    textInputAmount.visible()
                    viewAmountBorder.visible()
                    calculatorLayout.visible()
                    textAmount.visible()
                    roboflowLoadingGroup.gone()
                    roboflowEnterAmountManually.gone()
                    newDateContainer.visible()
                    noteAndAmountGroup.visible()
                    btnSubmit.isEnabled = true
                    etAmount.isEnabled = true
                    etAmount.requestFocus()

                    textAmount.text = getString(R.string.upload_failure_message)
                    if (!state.amountAdded()) {
                        disableSubmit()
                    } else {
                        enabledSubmit()
                    }
                }
            }
            if (!state.amountAdded()) {
                disableSubmit()
            } else {
                enabledSubmit()
            }
        }
    }

    private fun doImageOnboardingChanges(state: State) {
        val constraints = ConstraintSet()
        constraints.clone(binding.amountDateBillContainer)
        constraints.clear(R.id.picture_list_rv, ConstraintSet.TOP)
        constraints.clear(R.id.text_input_amount, ConstraintSet.TOP)
        constraints.clear(R.id.new_date_container, ConstraintSet.TOP)
        constraints.clear(binding.newDateContainer.id, ConstraintSet.TOP)
        if (state.roboflowState !is RoboflowState.ShowAddBillToolTip && state.isImageAdded) {
            constraints.connect(
                R.id.picture_list_rv,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                dpToPixel(16f).toInt()
            )
        } else {
            constraints.connect(
                R.id.picture_list_rv,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                dpToPixel(56f).toInt()
            )
        }
        constraints.connect(
            binding.newDateContainer.id,
            ConstraintSet.TOP,
            R.id.roboflow_enter_amount_manually,
            ConstraintSet.BOTTOM,
            24
        )

        constraints.connect(
            binding.textInputAmount.id,
            ConstraintSet.TOP,
            R.id.picture_list_rv,
            ConstraintSet.BOTTOM,
            16
        )
        constraints.connect(
            binding.newDateContainer.id,
            ConstraintSet.TOP,
            R.id.roboflow_enter_amount_manually,
            ConstraintSet.BOTTOM,
            dpToPixel(32f).toInt()
        )
        constraints.applyTo(binding.amountDateBillContainer)
    }

    private fun startVectorDrawableAnimation(view: ImageView, capturedImage: CapturedImage?) {
        GlideApp.with(requireContext())
            .load(capturedImage?.file?.path ?: "")
            .placeholder(R.drawable.ic_bill)
            .error(R.drawable.ic_bill)
            .fallback(R.drawable.ic_bill)
            .thumbnail(0.25f)
            .into(binding.ivRoboflow)

        val vectorDrawable = AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.roboflow_loader)
        vectorDrawable?.apply {
            view.setImageDrawable(this)
            start()
            val callback = getVectorDrawableAnimationLooperCallback(this)
            registerAnimationCallback(callback)
        }
    }

    private fun getVectorDrawableAnimationLooperCallback(
        animatedVectorDrawableCompat: AnimatedVectorDrawableCompat,
    ): Animatable2Compat.AnimationCallback {
        return object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                animatedVectorDrawableCompat.start()
            }
        }
    }

    private fun clearVectorDrawableAnimationCallbacks(view: ImageView) {
        when (val drawable = view.drawable) {
            is AnimatedVectorDrawableCompat -> drawable.clearAnimationCallbacks()
            is AnimatedVectorDrawable ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    drawable.clearAnimationCallbacks()
                }
        }
    }

    private fun checkForNoteAndSubmitButton(state: State) = binding.apply {
        if (state.amountAdded()) {
            noteAndAmountGroup.visible()
            AnimationUtils.fadeInOnViewVisible(noteAndAmountGroup)
        } else {
            noteAndAmountGroup.gone()
        }
    }

    private fun checkForAddTransactionShortcut(state: State) {
        val isFromAddTransactionShortcut = state.source == ADD_TRANSACTION_SHORTCUT_SCREEN
        binding.sendOrReceivePayment.root.isVisible = isFromAddTransactionShortcut && state.amountAdded()
        if (isFromAddTransactionShortcut) binding.btnSubmit.gone()
    }

    override fun onResume() {
        super.onResume()
        hideSoftKeyboard()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun checkForBillDateAdded(state: State) = binding.apply {
        if (state.amountAdded()) {
            AnimationUtils.fadeInOnViewVisible(newDateContainer)
            newDateContainer.visible()
            if (state.billDate.isNotBlank()) {
                dateTextNew.setText(state.billDate, TextView.BufferType.NORMAL)
            }
        } else {
            newDateContainer.gone()
        }
    }

    private fun checkForBillImages(state: State) = binding.apply {
        if (state.amountAdded() ||
            state.isRoboflowEnabled
        ) {
            AnimationUtils.fadeInOnViewVisible(pictureListRv)
            pictureListRv.visible()
            controller.get().setData(state.addBillModels)
        } else {
            pictureListRv.gone()
        }
    }

    private val balloon by lazy {
        createBalloon(requireContext()) {
            setArrowSize(8)
            setWidthRatio(0.7f)
            setArrowPosition(0.5f)
            setArrowOrientation(ArrowOrientation.BOTTOM)
            setCornerRadius(8f)
            setMarginBottom(4)
            setAutoDismissDuration(7000L)
            setAlpha(1f)
            setPadding(8)
            setText(getString(R.string.roboflow_tooltip_message, getCurrentState().getPaymentType()))
            setTextColorResource(R.color.white)
            setBackgroundColorResource(R.color.indigo_1)
            setBalloonAnimation(BalloonAnimation.FADE)
            setLifecycleOwner(lifecycleOwner)
        }
    }

    private val calculatorEducationBalloon by lazy {
        createBalloon(requireContext()) {
            setLayout(R.layout.layout_calculator_education)
            setWidthRatio(0.9f)
            setArrowOrientation(ArrowOrientation.BOTTOM)
            setArrowPosition(0.5f)
            setCornerRadius(8f)
            setDismissWhenTouchOutside(true)
            setDismissWhenClicked(false)
            setBackgroundColorResource(R.color.indigo_primary)
            setBalloonAnimation(BalloonAnimation.FADE)
            setLifecycleOwner(lifecycleOwner)
        }
    }

    private fun showRoboflowAddBillTooltip() {
        if (isToolTipAlreadyShown) return

        balloon.apply {
            this.showAlignTop(binding.pictureListRv, 0, 0)
        }
        balloon.setOnBalloonClickListener { OnBalloonClickListener { balloon.dismiss() } }
        isToolTipAlreadyShown = true
    }

    private fun checkForTxnType(txnType: Int) = binding.apply {
        val colorStateList = if (txnType == CREDIT) {
            ColorStateList.valueOf(getColorCompat(R.color.tx_credit))
        } else {
            ColorStateList.valueOf(getColorCompat(R.color.tx_payment))
        }
        TextViewCompat.setCompoundDrawableTintList(ruppeeSymbolRoboflow, colorStateList)
        TextViewCompat.setCompoundDrawableTintList(etAmount, colorStateList)
    }

    private fun checkForAmountAdded(state: State) {
        state.amount?.let {
            val amount = state.amountCalculation?.replace("*", "x") ?: ""
            enabledSubmit()
            binding.etAmount.setText(amount)
            if (amount.isNotNullOrBlank()) {
                binding.etAmount.setSelection(amount.length)
            }
            CurrencyUtil.renderAsSubtitle(binding.textAmount, it)
        }

        binding.textAmount.isVisible = state.amountCalculation?.let {
            it.contains("*") ||
                it.contains("x") ||
                it.contains("+") ||
                it.contains("-") ||
                it.contains("/")
        } ?: false
    }

    private fun requestRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Permission.requestRecordAudioPermission(
                activity as AppCompatActivity,
                object : IPermissionListener {
                    override fun onPermissionGrantedFirstTime() {}

                    override fun onPermissionGranted() {
                        onRecordAudioPermissionGranted()
                    }

                    override fun onPermissionDenied() {}
                }
            )
        } else {
            onRecordAudioPermissionGranted()
        }
    }

    internal fun onRecordAudioPermissionGranted() {
        val state = if (isStateInitialized()) getCurrentState() else null
        val canCollectVoiceSamplesFromNotes = state?.canCollectVoiceSamplesFromNotes ?: false
        customerEventTracker.get()
            .trackAddNoteVoiceClicked(state?.customer?.id.itOrBlank(), canCollectVoiceSamplesFromNotes)
        val inputTextLength = binding.noteEditText.text.toString().length
        val maxInputLimit = requireContext().resources.getInteger(R.integer.max_transaction_note_input_limit)
        if (inputTextLength >= maxInputLimit) {
            return
        }

        if (canCollectVoiceSamplesFromNotes) {
            pushIntent(AddTransactionContract.Intent.StartSpeechRecognition)
            binding.calculatorLayout.gone()
        } else {
            binding.noteEditText.requestFocus()
            showSoftKeyboard(binding.noteEditText)
            binding.calculatorLayout.gone()
            if (speechInput == null) {
                speechInput = SpeechInput(speechRecognizer.get(), requireContext(), this)
            }

            if (speechInput?.isUserSpeaking == true) {
                speechInput?.stopListening()
            } else {
                speechInput?.startListening()
            }
        }
    }

    private fun renderNoteAddedWithVoice(data: Intent?) {
        val bundle = data?.extras
        val matches = bundle?.getStringArrayList(RecognizerIntent.EXTRA_RESULTS)
        val maxInputLimit = requireContext().resources.getInteger(R.integer.max_transaction_note_input_limit)
        val result = matches?.getOrNull(0) ?: ""

        binding.apply {
            val noteText = "${noteEditText.text} $result".take(maxInputLimit)
            noteEditText.setText(noteText)
            noteEditText.setSelection(noteText.length)
        }

        scheduleAudioSampleUpload(data, result)
    }

    private fun scheduleAudioSampleUpload(data: Intent?, transcribedText: String) {
        try {
            data?.data?.let { pushIntentWithDelay(AddTransactionContract.Intent.SetAudioSample(it, transcribedText)) }
        } catch (e: Exception) {
            RecordException.recordException(e)
        }
    }

    override fun loadIntent(): UserIntent = AddTransactionContract.Intent.Load

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                data?.getSerializableExtra(CameraActivity.ADDED_IMAGES)?.let {
                    viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                        binding.calculatorLayout.visible()
                        pushIntent(
                            AddTransactionContract.Intent.BillImagesAdded(
                                it as ArrayList<CapturedImage>, false
                            )
                        )
                    }
                }
            }
            MULTIPLE_IMAGE_REQUEST_CODE -> {
                data?.getSerializableExtra(FINAL_SELECTED_IMAGE_LIST)?.let {
                    viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                        binding.calculatorLayout.visible()
                        pushIntent(
                            AddTransactionContract.Intent.BillImagesAdded(
                                it as ArrayList<CapturedImage>, true
                            )
                        )
                    }
                }
            }
            ADD_NOTE_WITH_VOICE -> {
                if (resultCode == Activity.RESULT_OK) {
                    renderNoteAddedWithVoice(data)
                } else {
                    pushIntentWithDelay(AddTransactionContract.Intent.OptOutFromVoiceSamplesCollection)
                }
            }
            ADD_NEW_TRANSACTION_UPDATE_PIN,
            ADD_NEW_TRANSACTION,
            -> {
                data?.let {
                    if (data.getBooleanExtra(Constants.IS_AUTHENTICATED, false)) {
                        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                            pushIntent(
                                AddTransactionContract.Intent.AddTransaction(
                                    getCurrentState().amount,
                                    false,
                                    "",
                                    binding.noteEditText.text.toString(),
                                    txnType
                                )
                            )
                        }
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val selectedCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        val newBillDate = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM).format(selectedCalendar.time)
        customerEventTracker.get().trackUpdateBillDate(
            default = getCurrentState().billDate == newBillDate,
            accountId = getCurrentState().customer?.id ?: "",
        )
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            pushIntent(AddTransactionContract.Intent.BillDateSelected(selectedCalendar))
        }
    }

    override fun callbackData(amountCalculation: String?, amount: Long, calculatorOperatorsUsed: String?) {
        this.calculatorOperatorsUsed = calculatorOperatorsUsed
        // hide operator education if merchant has used operators
        if (calculatorOperatorsUsed.isNotNullOrBlank()) {
            val customerId = if (isStateInitialized()) (getCurrentState().customer?.id ?: "") else ""
            customerEventTracker.get().trackOperatorsClicked(customerId)
            calculatorEducationBalloon.dismiss()
            pushIntent(AddTransactionContract.Intent.CalculatorEducationDismissed)
        }
        pushIntent(AddTransactionContract.Intent.CalculatorData(amountCalculation, amount))
    }

    override fun isInvalidAmount() {
        if (binding.errorAmount.isVisible()) return
        customerEventTracker.get().trackInputCalculatorError(
            operatorsUsed = calculatorOperatorsUsed ?: "",
            accountId = getCurrentState().customer?.id ?: "",
        )
        binding.errorAmount.visible()
        binding.viewAmountBorder.setBackgroundColor(getColorCompat(R.color.tx_credit))
        TransitionManager.beginDelayedTransition(binding.textInputAmount)
        AnimationUtils.shake(binding.textInputAmount)
        hideErrorAfterDelay()
    }

    override fun onRoboflowPictureClicked(roboflowImage: AddBillModel.RoboflowPicture) {
        onPictureClicked(roboflowImage.image)
    }

    override fun onPictureClicked(capturedImage: CapturedImage) {
        val state = getCurrentState()
        tracker.get().trackEditReceipt(
            "Add Transaction",
            "Customer",
            state.getPaymentType(),
            state.customer?.id
        )
        val images = getCurrentState().addBillModels.mapNotNull { model ->
            when (model) {
                is AddBillModel.RoboflowPicture -> model.image
                is AddBillModel.PictureView -> model.image
                is AddBillModel.AddBill -> null
            }
        }

        this@AddTransactionFragment.startActivityForResult(
            MultipleImageActivity.createSelectedImagesIntent(
                requireContext(),
                capturedImage,
                images,
                "Add Transaction",
                "Customer",
                state.getPaymentType(),
                "Add Screen",
                state.customer?.id,
                state.customer?.mobile
            ),
            MULTIPLE_IMAGE_REQUEST_CODE
        )
    }

    internal fun transactionSuccessful(needDelay: Boolean = true) {
        customerEventTracker.get().trackAddTransactionConfirmed(
            type = getCurrentState().getPaymentType(),
            amount = (getCurrentState().amount ?: 0L).toString(),
            customerId = getCurrentState().customer?.id ?: "",
            calculatorUsed = calculatorOperatorsUsed.isNotNullOrBlank(),
            txnId = getCurrentState().tx?.id ?: "",
            source = getCurrentState().source.value,
            commonLedger = getCurrentState().isSupplierCreditEnabledCustomer,
            customerSyncStatus = getCustomerSyncStatus(getCurrentState().customer),
            notes = getCurrentState().tx?.note ?: "",
        )

        lifecycleScope.launchWhenResumed {
            if (needDelay) {
                delay(1000L) // added as per requirement from product need to show bottomSheetLoader
            }
            voiceIconAnimator?.cancel()

            tracker.get().incrementSuperProperty(SuperProperties.CUSTOMER_TRANSACTION_COUNT)

            dismissBottomLoader()
            if (requireActivity().callingActivity != null) {
                val resultData = Intent().apply { putExtra(KEY_TRANSACTION_ID, getCurrentState().tx?.id) }
                requireActivity().setResult(Activity.RESULT_OK, resultData)
            }
            requireActivity().finish()
        }
    }

    private fun initBottomLoader() {
        bottomSheetLoader =
            childFragmentManager.findFragmentByTag(BottomSheetLoaderScreen.TAG) as? BottomSheetLoaderScreen
        if (bottomSheetLoader == null) {
            bottomSheetLoader = BottomSheetLoaderScreen()
            bottomSheetLoader?.setListener(this)
            bottomSheetLoader?.isCancelable = false
        }
    }

    private fun openImageSelector() {
        Permission.requestStorageAndCameraPermission(
            requireActivity(),
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {}

                override fun onPermissionGranted() {
                    tracker.get().trackRuntimePermission(PropertyValue.CUSTOMER, PropertyValue.STORAGE, true)
                    val state = getCurrentState()
                    tracker.get().trackAddReceiptStarted(
                        "Add Transaction",
                        "Customer",
                        state.getPaymentType(),
                        "Add Screen",
                        state.customer?.id
                    )
                    CameraActivity.startActivityForResultFromFragment(
                        context = requireContext(),
                        fragment = this@AddTransactionFragment,
                        requestCode = CAMERA_REQUEST_CODE,
                        flow = "Add Transaction",
                        relation = "Customer",
                        type = state.getPaymentType(),
                        screen = "Add Screen",
                        account = state.customer?.id ?: "",
                        mobile = state.customer?.mobile,
                        existingImagesCount = state.addBillModels.size - 1 // account for addBills button
                    )
                }

                override fun onPermissionDenied() {
                    tracker.get().trackRuntimePermission(PropertyValue.CUSTOMER, PropertyValue.STORAGE, false)
                }

                override fun onPermissionPermanentlyDenied() {}
            }
        )
    }

    override fun onAddBillsClicked() {
        pushIntent(AddTransactionContract.Intent.AddBillImageClicked)
    }

    override fun onRetry() {
        dismissBottomLoader()
        binding.btnSubmit.performClick()
    }

    override fun onCancel() {
        dismissBottomLoader()
    }

    override fun onSuccess() {
        transactionSuccessful()
    }

    private fun dismissBottomLoader() {
        lifecycleScope.launchWhenCreated {
            bottomSheetLoader?.dismissAllowingStateLoss()
            isBottomSheetShown = false
            bottomSheetLoader = null
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            clearVectorDrawableAnimationCallbacks(binding.roboflowLoading)
            voiceIconAnimator?.cancel()
            speechRecognizer.get().destroy()
        } catch (e: Exception) {
        }
    }

    override fun onSetNewPinClicked(requestCode: Int) {
        this@AddTransactionFragment.startActivityForResult(
            appLock.get().appLock(getString(R.string.changepin_screen_deeplink), requireActivity(), TXN_SCREEN),
            ADD_NEW_TRANSACTION
        )
    }

    override fun onUpdateDialogDismissed() {}

    private fun showCalendar(selectedDate: Calendar?) {
        customerEventTracker.get().trackSelectBillDate(
            accountId = getCurrentState().customer?.id ?: "",
        )
        val currentCalendar = Calendar.getInstance().apply { timeInMillis = CommonUtils.currentDateTime().millis }
        DatePickerDialog(
            requireContext(),
            this@AddTransactionFragment,
            selectedDate?.get(Calendar.YEAR) ?: currentCalendar.get(Calendar.YEAR),
            selectedDate?.get(Calendar.MONTH) ?: currentCalendar.get(Calendar.MONTH),
            selectedDate?.get(Calendar.DAY_OF_MONTH)
                ?: currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = currentCalendar.timeInMillis
            setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.ok), this)
            setButton(DatePickerDialog.BUTTON_NEGATIVE, getString(R.string.cancel), this)
            show()
        }
    }

    private fun goToEnterPassword() {
        hideSoftKeyboard()
        this@AddTransactionFragment.startActivityForResult(
            appLock.get()
                .appLock(getString(R.string.enterpin_screen_deeplink), requireActivity(), TXN_SCREEN),
            ADD_NEW_TRANSACTION
        )
    }

    override fun handleViewEvent(event: AddTxnViewEvent) {
        when (event) {
            is AddTxnViewEvent.HideKeyboardWhenRoboflowEnabled -> binding.calculatorLayout.gone()
            is AddTxnViewEvent.SelectBillImage -> openImageSelector()
            is AddTxnViewEvent.ShowCalendar -> showCalendar(event.selectedDate)
            is AddTxnViewEvent.GoToEnterPassword -> goToEnterPassword()
            is AddTxnViewEvent.ShowUpdatePinDialog -> showUpdatePinDialog()
            is AddTxnViewEvent.ShowError -> showError(event.error, event.errorCode)
            is AddTxnViewEvent.ShowNoteTutorial -> showNoteTutorial()
            is AddTxnViewEvent.ShowBottomSheetLoader -> showBottomSheetLoader()
            is AddTxnViewEvent.DismissBottomSheetLoader -> dismissBottomLoader()
            is AddTxnViewEvent.ShowNewSuccess -> goToNewTransactionSuccessScreen()
            is AddTxnViewEvent.AddTransactionSuccess -> transactionSuccessful()
            is AddTxnViewEvent.InvalidAmountError -> {
                binding.errorAmount.text = getString(event.message)
                isInvalidAmount()
            }
            is AddTxnViewEvent.TrackAddNoteCompleted -> {
                val state = if (isStateInitialized()) getCurrentState() else null
                tracker.get().trackAddNoteCompleted(
                    flow = "Add Transaction",
                    relation = "Customer",
                    type = state?.getPaymentType() ?: "",
                    method = "",
                    account = state?.customer?.id,
                    note = binding.noteEditText.text.toString(),
                )
            }
            is AddTxnViewEvent.GoToCustomerProfile -> goToCustomerProfile()
            is AddTxnViewEvent.TrackRoboflowState -> trackRoboflowState(event.currentState)
            is AddTxnViewEvent.TrackAddedImagesCount -> roboflowEventTracker.get().trackAddedImagesCount(event.count)
            AddTxnViewEvent.ShowCalculatorEducation -> showCalculatorEducation()
            is AddTxnViewEvent.StartSpeechRecognition -> {
                val intent = event.intent
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, requireContext().packageName)
                startActivityForResult(intent, ADD_NOTE_WITH_VOICE)
            }
        }
    }

    private fun showCalculatorEducation() {
        lifecycleScope.launchWhenResumed {
            delay(500)
            binding.calculatorLayout.doOnLayout {
                binding.calculatorLayout.highlightOperators()
                val imageClose = calculatorEducationBalloon.getContentView().findViewById<ImageView>(R.id.image_close)
                imageClose.debounceClickListener {
                    pushIntent(AddTransactionContract.Intent.CalculatorEducationDismissed)
                    customerEventTracker.get().trackPopUpClosed(type = CALCULATOR_EDUCATION)
                    calculatorEducationBalloon.dismiss()
                }
                calculatorEducationBalloon.showAlignTop(binding.calculatorLayout)

                customerEventTracker.get().trackInAppNotificationDisplayed(type = CALCULATOR_EDUCATION)
            }
        }
    }

    private fun trackRoboflowState(currentState: RoboflowState) {
        when (currentState) {
            is RoboflowState.RoboflowFetchInProgress -> roboflowEventTracker.get().trackRoboflowState(LOADING)
            is RoboflowState.RoboflowFetchSuccess -> roboflowEventTracker.get().trackRoboflowState(
                PREDICTION_SUCCESS,
                currentState.amountBox.amount.toString()
            )
            is RoboflowState.RoboflowFetchFailed -> roboflowEventTracker.get().trackRoboflowState(PREDICTION_FAILED)
            is RoboflowState.EditAmount -> roboflowEventTracker.get().trackRoboflowState(EDIT_AMOUNT_CLICKED)
            is RoboflowState.EnterAmountManuallyCancelUploadReceipt ->
                roboflowEventTracker.get().trackRoboflowState(ENTER_AMOUNT_MANUALLY_CLICKED)
            is RoboflowState.InternetNotAvailable -> roboflowEventTracker.get().trackRoboflowState(UPLOAD_FAILURE)
            else -> {
            }
        }
    }

    private fun showError(error: Int, errorCode: Int?) {
        binding.btnSubmit.enable()
        if (errorCode != null) {
            longToast(getString(errorCode, error))
        } else {
            longToast(error)
        }
    }

    private fun goToCustomerProfile() {
        val state = getCurrentState()
        tracker.get().trackViewProfile(
            PropertyValue.ADD_TXN,
            PropertyValue.CUSTOMER,
            PropertyValue.CUSTOMER,
            state.customer?.id
        )
        legacyNavigator.get().gotoCustomerProfile(requireActivity(), state.customer?.id!!)
    }

    private fun goToNewTransactionSuccessScreen() {
        val navController = findNavController(this)
        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>(NewTransactionSuccessFragment.ANIMATION_FINISHED)
            .observe(
                currentBackStackEntry,
                {
                    if (it == true) {
                        transactionSuccessful(needDelay = false)
                    }
                }
            )
        val action =
            AddTransactionFragmentDirections.actionCreditEducation(
                getCurrentState().amount ?: 0,
                getCurrentState().txType.toLong()
            )
                .apply {
                    argCustomerMobilePresent = getCurrentState().customer?.mobile.isNotNullOrBlank()
                }
        findNavController(this).navigate(action)
    }

    private fun showBottomSheetLoader() {
        bottomSheetLoader?.let {
            it.load()
            if (it.isAdded.not() && isBottomSheetShown.not()) {
                isBottomSheetShown = true
                it.show(childFragmentManager, BottomSheetLoaderScreen.TAG)
                hideSoftKeyboard()
            }
        }
    }

    private fun showNoteTutorial() {
        customerEventTracker.get().trackInAppNotificationDisplayed("note")
        lifecycleScope.launch {
            localInAppNotificationHandler.get()
                .generateTapTarget(
                    weakScreen = WeakReference(requireActivity()),
                    tapTarget = TapTargetLocal(
                        screenName = label,
                        targetView = WeakReference(binding.textInputNote),
                        title = getString(R.string.note_tutorial),
                        titleTextSize = 16f,
                        listener = { _, state ->
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {

                                customerEventTracker.get().trackInAppNotificationClicked(
                                    "note",
                                    focalArea = true
                                )
                            } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {

                                customerEventTracker.get().trackInAppNotificationClicked(
                                    "note",
                                    focalArea = false
                                )
                            }
                        }
                    )
                )
        }
    }

    private fun showUpdatePinDialog() {
        appLock.get().showUpdatePin(requireActivity().supportFragmentManager, this, sourceScreen = TXN_SCREEN)
    }

    companion object {
        const val CAMERA_REQUEST_CODE = 1002
        const val MULTIPLE_IMAGE_REQUEST_CODE = 1003
        const val ADD_NOTE_WITH_VOICE = 1004
        const val ADD_NEW_TRANSACTION = 191
        const val ADD_NEW_TRANSACTION_UPDATE_PIN = 1201
        const val TXN_SCREEN = "AddTxnScreen"
        const val KEY_TRANSACTION_ID = "transaction_id"

        const val FINAL_SELECTED_IMAGE_LIST = "finalSelectedImagelist"
    }

    override fun showAlertMessage(alertMessage: String) {
        longToast(alertMessage)
    }

    override fun onTextResult(result: String) {
        binding.apply {
            val noteText = noteEditText.text.toString() + " " + result
            noteEditText.setText(noteText)
            noteEditText.setSelection(noteEditText.length())
        }
    }

    override fun startVoiceIconAnimation() {
        binding.apply {
            if (voiceIconAnimator == null) {
                voiceIconAnimator = animateMe(binding.voiceAnimationView)
            }
            voiceIconAnimator?.start()
            voiceIcon.setBackgroundResource(R.drawable.dark_blue_voice_animation_icon)
            voiceIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
            noteEditText.hint = requireContext().resources.getString(R.string.listening)
            voiceAnimationView.visibility = View.VISIBLE
        }
    }

    private fun animateMe(voiceIcon: ImageView): Animator {
        val animator =
            ObjectAnimator.ofFloat(voiceIcon, "translationX", -dpToPixel(14f))
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 300L
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = ValueAnimator.INFINITE

        return animator
    }

    override fun stopVoiceIconAnimation() {
        if (isResumed) {
            binding.apply {
                voiceIconAnimator?.cancel()
                voiceAnimationView.visibility = View.GONE
                voiceIcon.setBackgroundColor(Color.TRANSPARENT)
                noteEditText.hint = requireContext().resources.getString(R.string.add_note_optional)
                voiceIcon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary))
            }
        }
    }

    private fun enabledSubmit() {
        binding.btnSubmit.apply {
            elevation = resources.getDimension(R.dimen.view_4dp)
            backgroundTintList =
                ColorStateList.valueOf(getColorFromAttr(R.attr.colorPrimary))
            isEnabled = true
        }
    }

    private fun disableSubmit() {
        binding.btnSubmit.apply {
            elevation = 0f
            backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.grey400)
        }
    }

    fun showRemoveDetailsDialog() {
        val state = if (isStateInitialized()) getCurrentState() else null
        if (state?.isRoboflowEnabled == true && state.amountAdded()) {
            roboflowEventTracker.get().trackRemoveDetailsViewed()
            RemoveDetailsDialog.showRemoveDetailsDialog(
                requireActivity(),
                object : RemoveWarningDialogListener {
                    override fun onRemoveClicked() {
                        requireActivity().finish()
                    }
                }
            )
        } else {
            requireActivity().finish()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AmountFocusChangeListener) {
            amountFocusChangeListener = context
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showRemoveDetailsDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onDetach() {
        amountFocusChangeListener = null
        super.onDetach()
    }

    private fun getCustomerSyncStatus(customer: Customer?): String {
        return when (customer?.customerSyncStatus) {
            IMMUTABLE.code -> "Immutable"
            DIRTY.code -> "Dirty"
            CLEAN.code -> "Clean"
            else -> "Unknown"
        }
    }

    fun clearAmount() {
        // calculator holds amount in its view model, so we have to clear the amount in calculator
        binding.calculatorLayout.clearAmount()
    }

    fun requestAmountFocus() {
        binding.etAmount.requestFocus()
    }
}
