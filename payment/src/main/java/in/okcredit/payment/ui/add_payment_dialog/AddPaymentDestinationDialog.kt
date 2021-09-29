package `in`.okcredit.payment.ui.add_payment_dialog

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.collection.contract.CollectionConstants
import `in`.okcredit.collection.contract.CollectionConstants.QR_SCANNER_REQUEST_CODE
import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_ACCOUNT_BALANCE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_ACCOUNT_TYPE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_BLIND_PAY_ENABLED
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_ACCOUNT_ID
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_MOBILE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_NAME
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_PROFILE_IMAGE
import `in`.okcredit.payment.R
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.BANK
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.BANK_ACCOUNT_NUMBER
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.IFSC
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.INTERNAL_SUPPLIER_COLLECTION
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.NOT_AWARE
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_ADDRESS_DETAILS
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.UPI
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.UPI_ID
import `in`.okcredit.payment.contract.AddPaymentDestinationListener
import `in`.okcredit.payment.databinding.AddPaymentMethodDialogBinding
import `in`.okcredit.payment.utils.PaymentUtils.isValidIfsc
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.contract.model.SupportType
import tech.okcredit.android.base.extensions.*
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddPaymentDestinationDialog :
    BaseBottomSheetWithViewEvents<AddPaymentDestinationContract.State, AddPaymentDestinationContract.ViewEvents, AddPaymentDestinationContract.Intent>(
        "AddPaymentDestinationDialog"
    ) {

    private val binding: AddPaymentMethodDialogBinding by viewLifecycleScoped(AddPaymentMethodDialogBinding::bind)
    private var paymentMethod = CollectionDestinationType.UPI
    internal var accountNumEventSent = false
    internal var ifscEventSent = false
    internal var upiIdEventSent = false
    private var listener: AddPaymentDestinationListener? = null

    @Inject
    internal lateinit var tracker: Tracker

    @Inject
    internal lateinit var paymentAnalyticsEvents: Lazy<PaymentAnalyticsEvents>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    private var isBlindPayEnabled: Boolean? = null

    companion object {
        const val WHATSAPP_PACKAGE_NAME = "com.whatsapp"
        const val TAG = "AddPaymentDestinationDialog"
        fun newInstance(
            accountId: String,
            accountType: String,
            mobile: String,
            name: String,
            dueBalance: Long,
            profileImage: String,
            isBlindPayEnabled: Boolean,
        ): AddPaymentDestinationDialog {
            val bundle = Bundle().apply {
                putString(ARG_PAYMENT_ACCOUNT_ID, accountId)
                putString(ARG_ACCOUNT_TYPE, accountType)
                putString(ARG_PAYMENT_MOBILE, mobile)
                putString(ARG_PAYMENT_NAME, name)
                putLong(ARG_ACCOUNT_BALANCE, dueBalance)
                putString(ARG_PAYMENT_PROFILE_IMAGE, profileImage)
                putBoolean(ARG_BLIND_PAY_ENABLED, isBlindPayEnabled)
            }
            return AddPaymentDestinationDialog().apply {
                arguments = bundle
            }
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        requireActivity().window.disableScreanCapture()
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogThemeWithKeyboard)
        isBlindPayEnabled = arguments?.getBoolean(ARG_BLIND_PAY_ENABLED)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AddPaymentMethodDialogBinding.inflate(inflater, container, false).root
    }

    fun setListener(listener: AddPaymentDestinationListener) {
        this.listener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountNumberFocusChangeListener()

        ifscFocusChangeListener()

        ifscTextChangeWatcher()

        accountTextChangeWatcher()

        upiTextChangeWatcher()

        radioPayment()

        enableIDontKnowOption()

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<View>(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }

        setClickListener()
    }

    private fun setClickListener() {
        binding.apply {
            accountClear.setOnClickListener {
                binding.accountNumber.text?.clear()
            }

            ifscClear.setOnClickListener {
                binding.ifsc.text?.clear()
            }

            upiScan.setOnClickListener {
                getCameraPermission()
            }

            buttonSupport.setOnClickListener {
                pushIntent(
                    AddPaymentDestinationContract.Intent.SupportClicked(
                        getString(R.string.t_002_i_need_help_generic),
                        getCurrentState().supportNumber
                    )
                )
            }
        }
    }

    private fun enableIDontKnowOption() {
        isBlindPayEnabled?.let {
            if (it) binding.iDontKnowRadio.gone()
        }
    }

    private fun radioPayment() {
        binding.paymentRadioGrp.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.upiRadio.id -> {
                    pushIntent(AddPaymentDestinationContract.Intent.SetAdoptionMode(CollectionDestinationType.UPI))
                }
                binding.bankRadio.id -> {
                    pushIntent(AddPaymentDestinationContract.Intent.SetAdoptionMode(CollectionDestinationType.BANK))
                }
                binding.iDontKnowRadio.id -> {
                    pushIntent(AddPaymentDestinationContract.Intent.SetAdoptionMode(CollectionDestinationType.I_DONT_KNOW))
                }
            }
        }
    }

    private fun accountNumberFocusChangeListener() {
        binding.accountNumber.setOnFocusChangeListener { _, hasFocus ->
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
        binding.ifsc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                binding.ifscError.visibility = if (editable.toString().isValidIfsc()) View.GONE else View.VISIBLE
                binding.ifscClear.visibility = if (editable.isEmpty()) View.GONE else View.VISIBLE
                if (editable.isNotEmpty() && ifscEventSent.not()) {
                    if (isStateInitialized()) {
                        getCurrentState().let {
                            paymentAnalyticsEvents.get().trackEnteredPaymentDetails(
                                accountId = it.accountId,
                                dueAmount = it.dueBalance.toString(),
                                screen = PAYMENT_ADDRESS_DETAILS,
                                relation = it.getRelationFrmAccountType(),
                                flow = INTERNAL_SUPPLIER_COLLECTION,
                                type = IFSC
                            )
                        }
                    }
                }
                setSubmitEnabled()
            }
        })
    }

    private fun accountTextChangeWatcher() {
        binding.accountNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                binding.accountClear.visibility = if (editable.isEmpty()) View.GONE else View.VISIBLE
                if (editable.isNotEmpty() && accountNumEventSent.not()) {
                    if (isStateInitialized()) {
                        getCurrentState().let {
                            paymentAnalyticsEvents.get().trackEnteredPaymentDetails(
                                accountId = it.accountId,
                                dueAmount = it.dueBalance.toString(),
                                screen = PAYMENT_ADDRESS_DETAILS,
                                relation = it.getRelationFrmAccountType(),
                                flow = INTERNAL_SUPPLIER_COLLECTION,
                                type = BANK_ACCOUNT_NUMBER
                            )
                        }
                    }
                }
                setSubmitEnabled()
            }
        })
    }

    private fun upiTextChangeWatcher() {
        binding.upiId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                if (editable.isNotEmpty() && upiIdEventSent.not()) {
                    if (isStateInitialized()) {
                        getCurrentState().let {
                            paymentAnalyticsEvents.get().trackEnteredPaymentDetails(
                                accountId = it.accountId,
                                dueAmount = it.dueBalance.toString(),
                                screen = PAYMENT_ADDRESS_DETAILS,
                                relation = it.getRelationFrmAccountType(),
                                flow = INTERNAL_SUPPLIER_COLLECTION,
                                type = UPI_ID
                            )
                        }
                    }
                }
                if (editable.isEmpty()) {
                    pushIntent(AddPaymentDestinationContract.Intent.ClearUpiError)
                }
                setSubmitEnabled()
            }
        })
    }

    internal fun setSubmitEnabled() {
        if (isStateInitialized()) {
            val isValid = validate()
            binding.submitUpi.isEnabled = isValid
            if (isValid) {
                binding.submitCard.setCardBackgroundColor(getColorCompat(R.color.green_primary))
            } else {
                binding.submitCard.setCardBackgroundColor(getColorCompat(R.color.grey400))
            }
        }
    }

    private fun validate(): Boolean {
        val state = getCurrentState()
        return when (state.adoptionMode) {
            CollectionDestinationType.UPI -> {
                isUpiValid()
            }
            CollectionDestinationType.BANK -> {
                isBankDetailsValid()
            }
            else -> {
                false
            }
        }
    }

    private fun isUpiValid(): Boolean {
        val upi = binding.upiId.text ?: ""
        return upi.isNotEmpty()
    }

    private fun isBankDetailsValid(): Boolean {
        val bankAccount = binding.accountNumber.text.toString()
        val ifsc = binding.ifsc.text.toString()
        return bankAccount.length >= 9 && ifsc.isNotEmpty() && ifsc.length == 11 && ifsc.isValidIfsc()
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.just(AddPaymentDestinationContract.Intent.Load),
            binding.submitUpi.clicks()
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    onSubmit()
                },
            binding.mbRequest.clicks()
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    getCurrentState().let {
                        paymentAnalyticsEvents.get().trackClickPaymentRequestDetails(
                            accountId = getCurrentState().accountId,
                            dueAmount = getCurrentState().dueBalance.toString(),
                            screen = PAYMENT_ADDRESS_DETAILS,
                            relation = it.getRelationFrmAccountType(),
                            flow = INTERNAL_SUPPLIER_COLLECTION,
                            type = NOT_AWARE
                        )
                    }

                    AddPaymentDestinationContract.Intent.ShareRequestToWhatsApp(getString(R.string.add_destination_request_text_whatsapp))
                }
        )
    }

    private fun onSubmit(): AddPaymentDestinationContract.Intent {
        val state = getCurrentState()
        val upi = binding.upiId.text.toString()
        return if (state.adoptionMode == CollectionDestinationType.UPI) {

            paymentAnalyticsEvents.get().trackConfirmPaymentDetails(
                accountId = state.accountId,
                screen = PAYMENT_ADDRESS_DETAILS,
                relation = state.getRelationFrmAccountType(),
                flow = INTERNAL_SUPPLIER_COLLECTION,
                dueAmount = getCurrentState().dueBalance.toString(),
                type = UPI
            )

            AddPaymentDestinationContract.Intent.SetPaymentVpa(upi, CollectionDestinationType.UPI.value)
        } else {
            paymentAnalyticsEvents.get().trackConfirmPaymentDetails(
                accountId = getCurrentState().accountId,
                screen = PAYMENT_ADDRESS_DETAILS,
                relation = state.getRelationFrmAccountType(),
                flow = INTERNAL_SUPPLIER_COLLECTION,
                dueAmount = getCurrentState().dueBalance.toString(),
                type = BANK
            )
            val paymentAddress = "${binding.accountNumber.text}@${binding.ifsc.text}"
            AddPaymentDestinationContract.Intent.SetPaymentVpa(paymentAddress, CollectionDestinationType.BANK.value)
        }
    }

    override fun render(state: AddPaymentDestinationContract.State) {
        val name = state.name?.split(" ")?.get(0) ?: ""
        showPaymentInputUI(name)

        if (state.upiLoaderStatus) {
            binding.upiLoader.visibility = View.VISIBLE
            binding.accountNumber.isEnabled = false
            binding.ifsc.isEnabled = false
            binding.upiId.isEnabled = false
            binding.accountClear.visibility = View.GONE
            binding.ifscClear.visibility = View.GONE
            binding.upiScan.gone()
        } else {
            binding.upiLoader.visibility = View.GONE
            binding.accountNumber.isEnabled = true
            binding.ifsc.isEnabled = true
            binding.upiId.isEnabled = true

            if (binding.accountNumber.hasFocus() && binding.accountNumber.text.isNullOrBlank().not()) {
                binding.accountClear.visibility = View.VISIBLE
            }
            if (binding.ifsc.hasFocus() && binding.ifsc.text.isNullOrBlank().not()) {
                binding.ifscClear.visibility = View.VISIBLE
            }
            if (binding.upiId.hasFocus()) {
                binding.upiScan.visible()
            }
        }

        when (state.adoptionMode) {
            CollectionDestinationType.UPI -> {
                showUpiUI(name)
                setSubmitButtonUi(state)
            }
            CollectionDestinationType.BANK -> {
                showBankUI(name)
                setSubmitButtonUi(state)
            }
            CollectionDestinationType.I_DONT_KNOW -> {
                showDontKnowUi(name)
            }
            else -> {
            }
        }

        showErrorStates(state)

        showUI(state)

        setTopBannerUi(state.supportType)
    }

    private fun setTopBannerUi(supportType: SupportType) {
        when (supportType) {
            SupportType.CALL -> setSupportBannerUi(supportType)
            SupportType.CHAT -> setSupportBannerUi(supportType)
            SupportType.NONE -> {
                binding.apply {
                    buttonSupport.gone()
                }
            }
        }
    }

    private fun setSupportBannerUi(supportType: SupportType) {
        binding.apply {
            buttonSupport.visible()

            buttonSupport.text =
                if (supportType == SupportType.CALL) {
                    getString(
                        R.string.t_002_24X7help_banner_call_label,
                        getCurrentState().support24x7String,
                        getCurrentState().supportNumber
                    ).trim()
                } else
                    getString(
                        R.string.t_002_24X7help_banner_whatsapp_label,
                        getCurrentState().support24x7String,
                        getCurrentState().supportNumber
                    ).trim()

            buttonSupport.icon =
                getDrawableCompact(
                    if (supportType == SupportType.CALL)
                        R.drawable.ic_call_support_indigo
                    else R.drawable.ic_whatsapp_indigo
                )
        }
    }

    private fun setSubmitButtonUi(state: AddPaymentDestinationContract.State) {
        if (state.upiLoaderStatus) {
            binding.upiLoader.visibility = View.VISIBLE
            binding.submitUpi.visibility = View.GONE
        } else {
            binding.upiLoader.visibility = View.GONE
            binding.submitUpi.visibility = View.VISIBLE
        }
    }

    private fun showUI(state: AddPaymentDestinationContract.State) {
        binding.paymentRadioGrp.visible()
        binding.contactView.visible()
        showProfile(state)
    }

    private fun showProfile(state: AddPaymentDestinationContract.State) {
        Glide.with(this).load(state.profileImage).placeholder(R.drawable.ic_contacts_placeholder).into(binding.pic)
        binding.name.text = state.name
        binding.mobile.text = state.mobile
    }

    private fun showPaymentInputUI(name: String) {
        when (paymentMethod) {
            CollectionDestinationType.UPI -> {
                showUpiUI(name)
            }
            CollectionDestinationType.BANK -> {
                showBankUI(name)
            }
            CollectionDestinationType.I_DONT_KNOW -> {
                showDontKnowUi(name)
            }
            else -> {
                paymentMethod = CollectionDestinationType.NONE
            }
        }
    }

    private fun showUpiUI(name: String) {

        val upi = getString(R.string.upi)
        binding.apply {
            ifscContainer.visibility = View.GONE
            bankAccountContainer.visibility = View.GONE
            tvAccountNumber.visibility = View.GONE
            tvIfsc.visibility = View.GONE
            upiIdContainer.visibility = View.VISIBLE
            tvUpiId.visibility = View.VISIBLE
            tvAddPaymentTitle.text = getString(R.string.add_user_upi_id, name)
            ifscError.visibility = View.GONE
            tvError.gone()
            tvAddPaymentDescription.text = getString(R.string.add_supplier_details_to_pay, upi)
            upiId.requestFocus()
            clDontKnow.gone()
            submitCard.visible()
        }
        setSubmitEnabled()
    }

    private fun showDontKnowUi(name: String) {
        binding.apply {
            ifscContainer.gone()
            bankAccountContainer.gone()
            tvAccountNumber.gone()
            tvIfsc.gone()
            upiIdContainer.gone()
            tvUpiId.gone()
            tvAddPaymentTitle.text = getString(R.string.request_for_details)
            ifscError.gone()
            tvError.gone()
            clDontKnow.visible()
            submitCard.gone()
            tvRequest.text = getString(R.string.request_or_their_bank_upi_details_to_make_online_payment, name)
        }
    }

    private fun showBankUI(name: String) {

        val bankAccount = getString(R.string.bank_account)
        binding.apply {
            ifscContainer.visibility = View.VISIBLE
            bankAccountContainer.visibility = View.VISIBLE
            tvAccountNumber.visibility = View.VISIBLE
            tvIfsc.visibility = View.VISIBLE
            upiIdContainer.visibility = View.GONE
            tvUpiId.visibility = View.GONE
            tvAddPaymentTitle.text = getString(R.string.add_user_bank_account, name)
            tvAddPaymentDescription.text =
                getString(R.string.add_supplier_details_to_pay, bankAccount.toLowerCase())
            tvError.isVisible = getCurrentState().errorMessage.isNotEmpty()
            accountNumber.requestFocus()
            clDontKnow.gone()
            submitCard.visible()
        }
        setSubmitEnabled()
    }

    private fun showErrorStates(state: AddPaymentDestinationContract.State) {
        binding.tvError.text = state.errorMessage
        binding.tvError.isVisible =
            state.adoptionMode == CollectionDestinationType.BANK && state.errorMessage.isNotEmpty()
        if (state.upiErrorServer) {
            binding.tvUpiId.setTextColor(getColorCompat(R.color.red_primary))
            binding.upiIdContainer.background = getDrawableCompact(R.drawable.circular_corners_selected_red_stroke)
        } else {
            binding.tvUpiId.setTextColor(getColorCompat(R.color.grey900))
            binding.upiIdContainer.background = getDrawableCompact(R.drawable.circular_corners_selected_background)
        }
        binding.tvUpiIdError.isVisible = state.upiErrorServer && state.adoptionMode == CollectionDestinationType.UPI
        binding.ifscError.isVisible =
            state.errorMessage.isEmpty() && state.invalidBankAccountError && state.adoptionMode == CollectionDestinationType.BANK
        when (state.invalidBankAccountCode) {
            AddPaymentDestinationContract.INVALID_ACCOUNT_NUMBER -> {
                binding.bankAccountContainer.background =
                    getDrawableCompact(R.drawable.circular_corners_selected_red_stroke)
            }
            AddPaymentDestinationContract.INVALID_IFSC_CODE -> {
                binding.ifscContainer.background =
                    getDrawableCompact(R.drawable.circular_corners_selected_red_stroke)
                binding.ifscError.visible()
            }
            AddPaymentDestinationContract.INVALID_ACCOUNT_NUMBER_AND_IFSC_CODE -> {
                binding.bankAccountContainer.background =
                    getDrawableCompact(R.drawable.circular_corners_selected_red_stroke)
                binding.ifscContainer.background =
                    getDrawableCompact(R.drawable.circular_corners_selected_red_stroke)
                binding.ifscError.visible()
            }
            else -> {
                binding.ifscError.gone()
            }
        }
    }

    private fun onAccountAddedSuccessfully() {
        hideSoftKeyboard()
        listener?.onDestinationAddedSuccessfully()
        dismissAllowingStateLoss()
    }

    private fun openWhatsAppToShareScreenshot(sharingText: String) {
        try {
            val whatsappIntent = Intent(Intent.ACTION_SEND)
            whatsappIntent.type = "text/plain"
            whatsappIntent.setPackage(WHATSAPP_PACKAGE_NAME)
            getCurrentState().let {
                if (it.mobile.isNotNullOrBlank())
                    whatsappIntent.putExtra("jid", "91${it.mobile}@s.whatsapp.net")
            }

            whatsappIntent.putExtra(
                Intent.EXTRA_TEXT,
                sharingText
            )
            startActivity(whatsappIntent)
            hideSoftKeyboard()
            dismiss()
        } catch (ex: ActivityNotFoundException) {
            shortToast(R.string.add_destination_whats_app_not_installed)
        }
    }

    private fun getCameraPermission() {
        Permission.requestCameraPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {}

                override fun onPermissionGranted() {
                    goToQRScannerScreen()
                }

                override fun onPermissionDenied() {
                    longToast(R.string.payment_camera_permission_denied_qr)
                }

                override fun onPermissionPermanentlyDenied() {}
            }
        )
    }

    internal fun goToQRScannerScreen() {
        legacyNavigator.get().goToQRScannerScreen(this, QR_SCANNER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == QR_SCANNER_REQUEST_CODE) {
            data?.let {
                val upiVpa = it.getStringExtra(CollectionConstants.UPI_ID)
                binding.apply {
                    upiVpa?.let {
                        upiId.setText(upiVpa)
                        upiId.setSelection(upiVpa.length)
                    }
                }
            }
        }
    }

    override fun handleViewEvent(event: AddPaymentDestinationContract.ViewEvents) {
        when (event) {
            is AddPaymentDestinationContract.ViewEvents.OnAccountAddedSuccessfully -> onAccountAddedSuccessfully()
            is AddPaymentDestinationContract.ViewEvents.ShareRequestToWhatsapp -> openWhatsAppToShareScreenshot(event.sharingText)
            is AddPaymentDestinationContract.ViewEvents.ShowErrorMessage -> {
                view?.snackbar(event.errorMessage, Snackbar.LENGTH_SHORT)?.show()
            }
            AddPaymentDestinationContract.ViewEvents.CallCustomerCare -> callSupport()
            is AddPaymentDestinationContract.ViewEvents.SendWhatsAppMessage -> startActivity(event.intent)
            AddPaymentDestinationContract.ViewEvents.ShowDefaultError -> shortToast(getString(R.string.payment_something_went_wrong))
            AddPaymentDestinationContract.ViewEvents.ShowWhatsAppError -> shortToast(getString(R.string.whatsapp_not_installed))
        }
    }

    private fun callSupport() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse(getString(R.string.call_template, getCurrentState().supportNumber))
        startActivity(intent)
    }

    override fun loadIntent(): UserIntent {
        return AddPaymentDestinationContract.Intent.LoadFirst
    }
}
