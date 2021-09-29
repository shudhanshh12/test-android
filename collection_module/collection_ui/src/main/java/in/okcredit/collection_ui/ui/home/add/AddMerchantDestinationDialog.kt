package `in`.okcredit.collection_ui.ui.home.add

import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.collection.contract.CollectionConstants
import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.collection.contract.MerchantDestinationListener
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.analytics.CollectionTracker.*
import `in`.okcredit.collection_ui.databinding.AddMerchantDestinationDialogBinding
import `in`.okcredit.collection_ui.ui.home.add.AddMerchantDestinationContract.*
import `in`.okcredit.collection_ui.ui.home.adoption.CollectionAdoptionV2Fragment.Companion.ARG_REFERRAL_MERCHANT_ID
import `in`.okcredit.collection_ui.utils.CollectionUtils
import `in`.okcredit.merchant.collection.analytics.CollectionTraces
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.performance.PerformanceTracker
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.utils.KeyboardUtil
import tech.okcredit.android.base.utils.debug
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.AppConstants
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddMerchantDestinationDialog :
    BaseBottomSheetWithViewEvents<State, ViewEvents, Intent>("AddMerchantDestinationDialog") {

    private var setAdoptionModePublishSubject = PublishSubject.create<String>()
    private var showConfirmPublishSubject = PublishSubject.create<Boolean>()

    private val binding: AddMerchantDestinationDialogBinding by viewLifecycleScoped(AddMerchantDestinationDialogBinding::bind)
    private var alert: Snackbar? = null
    internal var accountNumEventSent = false
    internal var ifscEventSent = false
    internal var upiIdEventSent = false
    internal var isSelectedCollectionTypeEventSent = false
    internal var currentAdoptionMode = ""
    private val compositeDisposable = CompositeDisposable()

    @Inject
    internal lateinit var tracker: Lazy<CollectionTracker>

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var performanceTracker: Lazy<PerformanceTracker>

    private var listener: MerchantDestinationListener? = null

    private var source = DEFAULT_SOURCE

    companion object {
        const val TAG = "AddMerchantDestinationDialog"
        const val ARG_ASYNC_REQUEST = "async_request"
        const val ARG_SOURCE = "source"
        const val DEFAULT_SOURCE = CollectionScreen.COLLECTION_ADOPTION_POPUP_SCREEN

        fun newInstance(
            isUpdateCollection: Boolean,
            paymentMethodType: String?,
            asyncRequest: Boolean = false,
            source: String? = null,
            referredByMerchantId: String = "",
        ): AddMerchantDestinationDialog {
            val bundle = Bundle().apply {
                putString(AppConstants.ARG_PAYMENT_METHOD_TYPE, paymentMethodType)
                putBoolean(AppConstants.ARG_IS_UPDATE_COLLECTION, isUpdateCollection)
                putBoolean(ARG_ASYNC_REQUEST, asyncRequest)
                if (source.isNullOrEmpty()) {
                    putString(ARG_SOURCE, DEFAULT_SOURCE)
                } else {
                    putString(ARG_SOURCE, source)
                }
                putString(ARG_REFERRAL_MERCHANT_ID, referredByMerchantId)
            }
            return AddMerchantDestinationDialog().apply {
                arguments = bundle
            }
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.disableScreanCapture()
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogThemeWithKeyboard)
        source = arguments?.getString(ARG_SOURCE) ?: DEFAULT_SOURCE
    }

    @AddTrace(name = CollectionTraces.RENDER_COLLECTION_ADOPTION_POPUP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AddMerchantDestinationDialogBinding.inflate(inflater, container, false).root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MerchantDestinationListener)
            listener = context
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accountClear.setOnClickListener {
            binding.accountNumber.text?.clear()
        }

        binding.ifscClear.setOnClickListener {
            binding.ifsc.text?.clear()
        }

        binding.ivScanQr.setOnClickListener {
            tracker.get().trackEvents(
                CollectionEvent.CLICKED_CAMERA,
                type = CollectionPropertyValue.UPI,
                screen = source
            )
            getCameraPermission()
        }

        binding.validateDetails.setOnClickListener {
            onValidateDetails()
        }

        binding.change.setOnClickListener {
            changeEnteredDetails()
        }

        accountNumberFocusChangeListener()

        ifscFocusChangeListener()

        ifscTextChangeWatcher()

        accountTextChangeWatcher()

        upiTextChangeWatcher()

        autoFillInDebugBuild()

        binding.addDestinationContainer.setTracker(performanceTracker)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun changeEnteredDetails() {
        val state = getCurrentState()

        tracker.get().trackEvents(
            CollectionEvent.CHANGE_COLLECTION_DETAILS,
            type = state.adoptionMode,
            source = source,
            propertiesMap = PropertiesMap.create()
                .add(CollectionPropertyKey.FLOW, isAdoptOrUpdateFlow())
        )
        showConfirmPublishSubject.onNext(false)
    }

    private fun onValidateDetails() {
        val state = getCurrentState()

        if (isInvalidInputs(state)) return

        tracker.get().trackCollectionDetailsCompleted(
            method = CollectionPropertyValue.Typing,
            isUpdateCollection = isUpdateCollection(),
            adoptionMode = getAdoptionType(state.adoptionMode),
            source = source,
            campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
            campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
        )
        showConfirmPublishSubject.onNext(true)
    }

    internal fun showBottomSheetFullyExpanded(): BottomSheetBehavior<*> {
        val dialog = dialog as BottomSheetDialog?
        val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.peekHeight = 0
        return behavior
    }

    internal fun disableDraggingInBottomSheet(behavior: BottomSheetBehavior<*>) {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {}

            override fun onStateChanged(view: View, state: Int) {
                if (state == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })
    }

    fun setUpiVpaFromScanner(upiVpa: String?, method: String) {

        if (upiVpa.isNullOrBlank().not()) {
            tracker.get().trackEnterCollectionDetails(
                method = method,
                type = CollectionPropertyValue.UPI,
                isUpdateCollection = isUpdateCollection(),
                source = source,
                campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
                campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
            )
        }
        tracker.get().trackCollectionDetailsCompleted(
            method = method,
            isUpdateCollection = isUpdateCollection(),
            adoptionMode = getAdoptionType(getCurrentState().adoptionMode),
            source = source,
            campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
            campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
        )
        binding.upiId.setText(upiVpa)
        lifecycleScope.launchWhenResumed {
            pushIntent(verifyPaymentAddress(true))
        }
    }

    private fun isInvalidInputs(state: State): Boolean {
        binding.apply {
            if (state.adoptionMode == CollectionDestinationType.UPI.value) {

                if (showErrorToastIfUpiEmpty(state)) return true
                return false
            } else if (state.adoptionMode == CollectionDestinationType.BANK.value) {

                if (showErrorToastIfBankDetailsEmpty()) return true
                if (showErrorToastIfBankDetailsInvalid(state)) return true

                return false
            }
        }

        return false
    }

    private fun showErrorToastIfBankDetailsInvalid(state: State): Boolean {
        if (CollectionUtils.isInvalidBankDetails(
                state.enteredAccountNumber,
                state.enteredIfsc,
                state.isValidIfsc
            )
        ) {
            longToast(R.string.invalid_bank_input_err_msg)
            return true
        }
        return false
    }

    private fun AddMerchantDestinationDialogBinding.showErrorToastIfBankDetailsEmpty(): Boolean {
        if (accountNumber.text.isNullOrBlank() && ifsc.text.isNullOrBlank()) {
            longToast(R.string.no_bank_input_err_msg)
            return true
        }
        return false
    }

    private fun showErrorToastIfUpiEmpty(state: State): Boolean {
        if (CollectionUtils.isUpiEmpty(state.enteredUPI)) {
            longToast(R.string.no_upi_input_err_msg)
            return true
        }
        return false
    }

    private fun autoFillInDebugBuild() {
        debug {
            binding.apply {
                upiIcon.setOnClickListener {
                    upiId.setText("8882946897@ybl") // NON-NLS
                }

                bankIcon.setOnClickListener {
                    accountNumber.setText("054101507917") // NON-NLS
                }

                ifscIcon.setOnClickListener {
                    ifsc.setText("ICIC0000541") // NON-NLS
                }
            }
        }
    }

    private fun accountNumberFocusChangeListener() {
        binding.accountNumber.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.tvAccountNumber.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
                binding.bankAccountContainer.setBackgroundResource(R.drawable.circular_corners_selected_background)
            } else {
                binding.tvAccountNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey900))
                binding.bankAccountContainer.setBackgroundResource(R.drawable.circular_corners_unselected_background)
            }

            binding.accountClear.visibility =
                if (hasFocus && binding.accountNumber.text.isNullOrBlank().not()) View.VISIBLE else View.GONE
        }
    }

    private fun ifscFocusChangeListener() {
        binding.ifsc.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tvIfsc.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
                binding.ifscContainer.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.circular_corners_selected_background)
            } else {
                binding.tvIfsc.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey900))
                binding.ifscContainer.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.circular_corners_unselected_background)
            }

            binding.ifscClear.visibility =
                if (hasFocus && binding.ifsc.text.isNullOrBlank().not()) View.VISIBLE else View.GONE
        }
    }

    private fun ifscTextChangeWatcher() {
        binding.ifsc.afterTextChange { text ->
            binding.ifscError.visibility =
                if (CollectionUtils.isValidIFSC(text)) View.GONE else View.VISIBLE
            binding.ifscClear.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
            pushIntent(Intent.EnteredIfsc(text))

            if (text.isNotEmpty() && ifscEventSent.not()) {
                if (isStateInitialized()) {
                    ifscEventSent = true

                    tracker.get().trackEnterCollectionDetails(
                        method = CollectionPropertyValue.Typing,
                        type = CollectionPropertyValue.IFSC,
                        isUpdateCollection = isUpdateCollection(),
                        source = source,
                        campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
                        campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
                    )

                    if (isSelectedCollectionTypeEventSent.not()) {
                        isSelectedCollectionTypeEventSent = true
                        trackSelectedCollectionAdoptionEvent()
                    }
                }
            }
        }
    }

    private fun accountTextChangeWatcher() {
        binding.accountNumber.afterTextChange { text ->
            binding.accountClear.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
            pushIntent(Intent.EnteredAccountNumber(text))

            if (text.isNotEmpty() && accountNumEventSent.not()) {
                if (isStateInitialized()) {
                    accountNumEventSent = true
                    tracker.get().trackEnterCollectionDetails(
                        method = CollectionPropertyValue.Typing,
                        type = CollectionPropertyValue.ACCOUNT_NUMBER,
                        isUpdateCollection = isUpdateCollection(),
                        source = source,
                        campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
                        campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
                    )

                    if (isSelectedCollectionTypeEventSent.not()) {
                        isSelectedCollectionTypeEventSent = true
                        trackSelectedCollectionAdoptionEvent()
                    }
                }
            }
        }
    }

    private fun upiTextChangeWatcher() {
        binding.upiId.afterTextChange { text ->
            pushIntent(Intent.EnteredUPI(text))

            if (text.isNotEmpty() && upiIdEventSent.not()) {
                if (isStateInitialized()) {
                    upiIdEventSent = true
                    tracker.get().trackEnterCollectionDetails(
                        method = CollectionPropertyValue.Typing,
                        type = CollectionPropertyValue.UPI,
                        isUpdateCollection = isUpdateCollection(),
                        source = source,
                        campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
                        campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
                    )

                    if (isSelectedCollectionTypeEventSent.not()) {
                        isSelectedCollectionTypeEventSent = true
                        trackSelectedCollectionAdoptionEvent()
                    }
                }
            }
        }
    }

    private fun trackSelectedCollectionAdoptionEvent() {
        tracker.get().trackSelectedCollectionAdoptionType(
            type = getAdoptionType(getCurrentState().adoptionMode),
            source = source,
            method = CollectionPropertyValue.Typing,
            flow = isAdoptOrUpdateFlow(),
            campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
            campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
        )
    }

    private fun getAdoptionType(type: String): String {
        return if (type == CollectionDestinationType.UPI.value) {
            CollectionPropertyValue.UPI
        } else {
            CollectionPropertyValue.BANK
        }
    }

    private fun isUpdateCollection() =
        getCurrentState().collectionMerchantProfile?.payment_address.isNullOrBlank().not()

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            binding.confirm.clicks()
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    onConfirm()
                },

            setAdoptionModePublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Intent.SetAdoptionMode(it)
                },

            showConfirmPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    verifyPaymentAddress(it)
                },

            binding.tvSwitchPaymentMode.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    val newAdoptionMode = switchPaymentMode()
                    Intent.SetAdoptionMode(newAdoptionMode)
                },
        )
    }

    private fun switchPaymentMode(): String {
        val state = getCurrentState()
        val newAdoptionMode = when (state.adoptionMode) {
            CollectionDestinationType.UPI.value -> {
                CollectionDestinationType.BANK.value
            }
            CollectionDestinationType.BANK.value -> {
                CollectionDestinationType.UPI.value
            }
            else -> {
                ""
            }
        }

        tracker.get().trackChangeCollectionMethod(
            screen = source,
            type = getAdoptionType(newAdoptionMode),
            flow = isAdoptOrUpdateFlow(),
            campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
            campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
        )
        return newAdoptionMode
    }

    private fun isAdoptOrUpdateFlow(): String {
        return if (isUpdateCollection()) {
            CollectionPropertyValue.UPDATE
        } else {
            CollectionPropertyValue.ADOPT
        }
    }

    private fun verifyPaymentAddress(it: Boolean): Intent {
        val state = getCurrentState()
        return if (state.adoptionMode == CollectionDestinationType.UPI.value) {
            val upi = binding.upiId.text.toString()
            Intent.ShowConfirmUI(it, upi, CollectionDestinationType.UPI.value, isUpdateCollection())
        } else {
            val paymentAddress = "${binding.accountNumber.text}@${binding.ifsc.text}"
            Intent.ShowConfirmUI(it, paymentAddress, CollectionDestinationType.BANK.value, isUpdateCollection())
        }
    }

    private fun onConfirm(): Intent {
        val state = getCurrentState()
        val upi = binding.upiId.text.toString()
        return if (state.adoptionMode == CollectionDestinationType.UPI.value) {
            tracker.get().trackConfirmCollectionDetails(
                type = CollectionPropertyValue.UPI,
                isUpdateCollection = isUpdateCollection(),
                campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
                campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
            )
            Intent.SetUpiVpa(upi)
        } else {
            tracker.get().trackConfirmCollectionDetails(
                type = CollectionPropertyValue.BANK,
                isUpdateCollection = isUpdateCollection(),
                campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
                campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
            )
            val paymentAddress = "${binding.accountNumber.text}@${binding.ifsc.text}"
            Intent.ConfirmBankAccount(paymentAddress, state.business?.id)
        }
    }

    @AddTrace(name = CollectionTraces.RENDER_COLLECTION_ADOPTION_POPUP)
    override fun render(state: State) {

        if (state.confirmLoaderStatus) {
            binding.apply {
                progressBar.visibility = View.VISIBLE
                accountNumber.isEnabled = false
                ifsc.isEnabled = false
                upiId.isEnabled = false
                accountClear.visibility = View.GONE
                ifscClear.visibility = View.GONE
                tvSwitchPaymentMode.visibility = View.INVISIBLE
                validateDetails.visibility = View.GONE
                confirm.text = ""
                change.isClickable = false
                confirm.isClickable = false
            }
        } else {
            binding.apply {
                progressBar.visibility = View.GONE
                tvSwitchPaymentMode.visibility = View.VISIBLE
                accountNumber.isEnabled = true
                ifsc.isEnabled = true
                upiId.isEnabled = true
                validateDetails.visibility = View.VISIBLE
                confirm.text = getString(R.string.confirm)
                change.isClickable = true
                confirm.isClickable = true

                if (accountNumber.hasFocus() && binding.accountNumber.text.isNullOrBlank().not()) {
                    accountClear.visibility = View.VISIBLE
                }
                if (ifsc.hasFocus() && ifsc.text.isNullOrBlank().not()) {
                    ifscClear.visibility = View.VISIBLE
                }
            }
        }

        if (state.showVerifyLoader) {
            binding.apply {
                tvSwitchPaymentMode.visibility = View.INVISIBLE
                verifyProgressBar.visible()
                validateDetails.gone()
            }
        } else {
            binding.apply {
                tvSwitchPaymentMode.visibility = View.VISIBLE
                verifyProgressBar.gone()
                validateDetails.visible()
            }
        }

        if (state.showConfirmUI) {
            showConfirmUI(state)
        } else {
            showPaymentModeUI(state)
        }

        showAlertForErrors(state)
    }

    private fun showPaymentModeUI(state: State) {
        changeGroupVisibility(binding.successGroup, View.GONE)
        changeGroupVisibility(binding.confirmGroup, View.GONE)
        binding.bankAccount.gone()

        if (state.adoptionMode == CollectionDestinationType.UPI.value) {
            showUpiUI()
            validateUpiUI(state)
        } else if (state.adoptionMode == CollectionDestinationType.BANK.value) {
            showBankUI()
            validateBankUI(state)
        }
        binding.tvSwitchPaymentMode.isVisible = state.isMerchantComingFromRewardScreen.not()
    }

    private fun validateBankUI(state: State) {
        if (CollectionUtils.isInvalidBankDetails(state.enteredAccountNumber, state.enteredIfsc, state.isValidIfsc)) {
            binding.validateDetails.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.circle_grey_background)
        } else {
            binding.validateDetails.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.circle_background_dark_green_no_theme)
        }
    }

    private fun validateUpiUI(state: State) {
        if (CollectionUtils.isUpiEmpty(state.enteredUPI)) {
            binding.validateDetails.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.circle_grey_background)
        } else {
            binding.validateDetails.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.circle_background_dark_green_no_theme)
        }
    }

    private fun showConfirmUI(state: State) {
        binding.apply {
            KeyboardUtil.hideKeyboard(this@AddMerchantDestinationDialog)
            changeGroupVisibility(bankAccountGroup, View.GONE)
            changeGroupVisibility(upiGroup, View.GONE)
            changeGroupVisibility(addMethodGroup, View.GONE)
            changeGroupVisibility(successGroup, View.GONE)
            changeGroupVisibility(confirmGroup, View.VISIBLE)
            verifyProgressBar.gone()

            if (state.adoptionMode == CollectionDestinationType.UPI.value) {
                showUpiConfirmUI()
            } else if (state.adoptionMode == CollectionDestinationType.BANK.value) {
                showBankConfirmUI()
            }
            merchantName.text =
                if (state.paymentAccountName.isNullOrBlank().not()) state.paymentAccountName else state.business?.name
        }
    }

    private fun AddMerchantDestinationDialogBinding.showBankConfirmUI() {
        confirmDetails.text = getString(R.string.confirm_bank_details)
        paymentModeIcon.setImageResource(R.drawable.ic_account_balance_bank)
        bankAccount.visible()
        bankAccount.text = accountNumber.text
        ifscOrUpi.text = ifsc.text
    }

    private fun AddMerchantDestinationDialogBinding.showUpiConfirmUI() {
        confirmDetails.text = getString(R.string.confirm_upi_details)
        paymentModeIcon.setImageResource(R.drawable.ic_upi_icon)
        ifscOrUpi.text = upiId.text
        bankAccount.gone()
    }

    private fun showUpiUI() {

        val upi = getString(R.string.upi_id)
        binding.apply {
            changeGroupVisibility(binding.bankAccountGroup, View.GONE)
            changeGroupVisibility(binding.upiGroup, View.VISIBLE)
            changeGroupVisibility(addMethodGroup, View.VISIBLE)
            tvSwitchPaymentMode.text = getString(R.string.add_bank_account)
            tvAddPaymentTitle.text = getString(R.string.add_upi_id)
            ifscError.visibility = View.GONE
            tvAddPaymentDescription.text = getString(R.string.to_accept_online_collections, upi)

            if (currentAdoptionMode != getCurrentState().adoptionMode) {
                currentAdoptionMode = getCurrentState().adoptionMode
                upiId.requestFocus()
            }
        }
    }

    private fun showBankUI() {

        val bankDetails = getString(R.string.bank_details)
        binding.apply {
            changeGroupVisibility(bankAccountGroup, View.VISIBLE)
            changeGroupVisibility(addMethodGroup, View.VISIBLE)
            changeGroupVisibility(upiGroup, View.GONE)
            tvSwitchPaymentMode.text = getString(R.string.add_upi_id)
            tvAddPaymentTitle.text = getString(R.string.add_bank_details)
            tvAddPaymentDescription.text =
                getString(R.string.to_accept_online_collections, bankDetails)

            if (currentAdoptionMode != getCurrentState().adoptionMode) {
                currentAdoptionMode = getCurrentState().adoptionMode
                accountNumber.requestFocus()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        handleOutsideClick()
    }

    private fun handleOutsideClick() {
        val outsideView = dialog?.window?.decorView?.findViewById<View>(com.google.android.material.R.id.touch_outside)
        outsideView?.setOnClickListener {
            if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                KeyboardVisibilityEvent.hideKeyboard(context, view)
            } else {
                dismissAllowingStateLoss()
            }
        }
    }

    private fun showAlertForErrors(state: State) {
        if (state.isNetworkError or state.error or state.isAlertVisible or state.invalidPaymentAddressError
            or state.serverAPIError
        ) {
            alert = when {
                state.isNetworkError -> binding.addDestinationContainer.snackbar(
                    getString(R.string.home_no_internet_msg),
                    Snackbar.LENGTH_INDEFINITE
                )
                state.isAlertVisible -> binding.addDestinationContainer.snackbar(
                    state.alertMessage,
                    Snackbar.LENGTH_INDEFINITE
                )
                state.invalidPaymentAddressError -> binding.addDestinationContainer.snackbar(
                    getString(R.string.invalid_address_please_check),
                    Snackbar.LENGTH_INDEFINITE
                )
                state.serverAPIError -> {
                    binding.addDestinationContainer.snackbar(
                        getString(R.string.err_default),
                        Snackbar.LENGTH_LONG
                    )
                }
                else -> binding.addDestinationContainer.snackbar(
                    getString(R.string.err_default),
                    Snackbar.LENGTH_INDEFINITE
                )
            }
            alert?.show()
        } else {
            alert?.dismiss()
        }
    }

    private fun getCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            tracker.get().trackEvents(
                CollectionEvent.VIEW_CAMERA_PERMISSION,
                screen = source
            )
        }
        Permission.requestCameraPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                    tracker.get().trackEvents(
                        CollectionEvent.PERMISSION_ACCEPT,
                        propertiesMap = PropertiesMap.create()
                            .add(CollectionPropertyKey.SCREEN, CollectionScreen.COLLECTION_ADOPTION_POPUP_SCREEN)
                            .add(CollectionPropertyKey.TYPE, CollectionPropertyValue.CAMERA)
                    )
                }

                override fun onPermissionGranted() {
                    goToQRScannerScreen()
                }

                override fun onPermissionDenied() {
                    tracker.get().trackEvents(
                        CollectionEvent.PERMISSION_DENIED,
                        propertiesMap = PropertiesMap.create()
                            .add(CollectionPropertyKey.SCREEN, CollectionScreen.COLLECTION_ADOPTION_POPUP_SCREEN)
                            .add(CollectionPropertyKey.TYPE, CollectionPropertyValue.CAMERA)
                            .add(CollectionPropertyKey.ALWAYS, false)
                    )
                    longToast(R.string.camera_permission_denied_qr)
                }

                override fun onPermissionPermanentlyDenied() {
                    tracker.get().trackEvents(
                        CollectionEvent.PERMISSION_DENIED,
                        propertiesMap = PropertiesMap.create()
                            .add(CollectionPropertyKey.SCREEN, CollectionScreen.COLLECTION_ADOPTION_POPUP_SCREEN)
                            .add(CollectionPropertyKey.TYPE, true)
                    )
                }
            }
        )
    }

    fun gotoLogin() {
        legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
    }

    internal fun goToQRScannerScreen() {
        activity?.runOnUiThread {
            legacyNavigator.goToQRScannerScreen(this, CollectionConstants.QR_SCANNER_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CollectionConstants.QR_SCANNER_REQUEST_CODE) {
            data?.let {
                val upiVpa = it.getStringExtra(CollectionConstants.UPI_ID)
                val scanMethod = it.getStringExtra(CollectionConstants.METHOD) ?: CollectionPropertyValue.CAMERA
                setUpiVpaFromScanner(upiVpa, scanMethod)
            }
        }
    }

    private fun changeGroupVisibility(group: Group, visibility: Int) {
        group.visibility = visibility
        group.requestLayout()
    }

    private fun onAccountAddedSuccessfully() {
        hideSoftKeyboard()

        changeGroupVisibility(binding.successGroup, View.VISIBLE)
        changeGroupVisibility(binding.confirmGroup, View.GONE)
        changeGroupVisibility(binding.bankAccountGroup, View.GONE)
        changeGroupVisibility(binding.upiGroup, View.GONE)
        changeGroupVisibility(binding.addMethodGroup, View.GONE)
        binding.bankAccount.gone()

        compositeDisposable.add(
            Completable
                .timer(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (!isStateSaved)
                        dismiss()
                }
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (isStateInitialized() && getCurrentState().success) {
            listener?.onAccountAddedSuccessfully(getCurrentState().updateEta)
        } else {
            listener?.onCancelled()
        }
        super.onDismiss(dialog)
    }

    override fun handleViewEvent(event: ViewEvents) {
        when (event) {
            is ViewEvents.GoToLogin -> gotoLogin()
            is ViewEvents.OnAccountAddedSuccessfully -> onAccountAddedSuccessfully()
        }
    }
}
