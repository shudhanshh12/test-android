package `in`.okcredit.payment.ui.payment_destination

import `in`.okcredit.payment.R
import `in`.okcredit.payment.databinding.PaymentDestinationDialogBinding
import `in`.okcredit.payment.server.internal.PaymentDestinationType
import `in`.okcredit.payment.ui.payment_destination.PaymentDestinationContract.*
import `in`.okcredit.payment.ui.payment_destination.PaymentDestinationDialog.Companion.newInstance
import `in`.okcredit.payment.ui.payment_destination.analytics.PaymentDestinationAdoptionTraces
import `in`.okcredit.payment.ui.payment_destination.analytics.PaymentDestinationAdoptionTracker
import `in`.okcredit.payment.utils.PaymentUtils.isInvalidBankDetails
import `in`.okcredit.payment.utils.PaymentUtils.isValidIfsc
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.android.synthetic.main.payment_destination_dialog.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.okcredit.android.base.extensions.*
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 *  To set a merchant destination this dialog can be used.Currently their are two ways to do it bank details and upi id.
 *  With the help of [newInstance] dialog can be configured acc to requirement
 *  @param showUpiOption Boolean to set should upi option be shown or not
 *  @param source String to set from which source destination is getting set
 *  @param descriptionText Boolean to set desc text need to be shown ot not
 *
 *  function [setDestinationSuccessListener] is provided to set a listener [DestinationSetSuccessListener] to
 *  get a call back in case destination is successfully set.
 */

class PaymentDestinationDialog : BaseBottomSheetWithViewEvents<State, ViewEvents, Intent>("PaymentDestinationDialog") {

    private val binding: PaymentDestinationDialogBinding by viewLifecycleScoped(PaymentDestinationDialogBinding::bind)
    private var currentAdoptionMode = ""
    private var successListener: DestinationSetSuccessListener? = null

    @Inject
    internal lateinit var tracker: Lazy<PaymentDestinationAdoptionTracker>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    companion object {
        const val TAG = "PaymentDestinationDialog"
        const val ARG_SHOW_UPI = "show_upi_option"
        const val ARG_DESC_TEXT = "show_description_text"
        const val ARG_SOURCE = "source"
        const val QR_SCANNER_REQUEST_CODE = 40002
        const val UPI_ID = "upi_id"

        fun newInstance(
            showUpiOption: Boolean = false,
            source: String,
            descriptionText: Boolean = true
        ): PaymentDestinationDialog {
            val bundle = Bundle().apply {
                putBoolean(ARG_SHOW_UPI, showUpiOption)
                putString(ARG_SOURCE, source)
                putBoolean(ARG_DESC_TEXT, descriptionText)
            }
            return PaymentDestinationDialog().apply {
                arguments = bundle
            }
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogThemeWithKeyboard)
    }

    @AddTrace(name = PaymentDestinationAdoptionTraces.ON_CREATE_VIEW_PAYMENT_ADOPTION_POPUP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return PaymentDestinationDialogBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tracker.get().trackEventPaymentDestinationDialogLoaded()

        binding.setClickListeners()

        ifscTextChangeWatcher()

        accountTextChangeWatcher()

        upiTextChangeWatcher()

        setGlobalLayoutListener(view)

        tietAccountNumber.requestFocus()

        handleOutsideClick()
    }

    private fun setGlobalLayoutListener(view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val behavior: BottomSheetBehavior<*> = showBottomSheetFullyExpanded()
            disableDraggingInBottomSheet(behavior)
        }
    }

    private fun PaymentDestinationDialogBinding.setClickListeners() {

        tvSwitchPaymentMode.setOnClickListener {
            if (currentAdoptionMode == PaymentDestinationType.BANK.value) {
                tracker.get().trackEventSwitchOptionBtnClicked(PaymentDestinationType.UPI.value)
                pushIntent(Intent.SetAdoptionMode(PaymentDestinationType.UPI.value))
            } else {
                tracker.get().trackEventSwitchOptionBtnClicked(PaymentDestinationType.BANK.value)
                pushIntent(Intent.SetAdoptionMode(PaymentDestinationType.BANK.value))
            }
        }

        tilUpi.setEndIconOnClickListener {
            if (getCurrentState().showUi == UiScreenType.UPI) {
                tracker.get().trackEventScanBtnClicked()
                getCameraPermission()
            }
        }

        tilAccountNumber.setEndIconOnClickListener {
            if (getCurrentState().showUi == UiScreenType.BANK) {
                tietAccountNumber.clear()
            }
        }

        tilIfsc.setEndIconOnClickListener {
            if (getCurrentState().showUi == UiScreenType.BANK) {
                tietIfsc.clear()
            }
        }
    }

    private fun showBottomSheetFullyExpanded(): BottomSheetBehavior<*> {
        val dialog = dialog as BottomSheetDialog?
        val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        return BottomSheetBehavior.from(bottomSheet!!).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            peekHeight = 0
        }
    }

    private fun disableDraggingInBottomSheet(behavior: BottomSheetBehavior<*>) {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {}
            override fun onStateChanged(view: View, state: Int) {
                if (state == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })
    }

    private fun ifscTextChangeWatcher() {
        binding.apply {
            tietIfsc.afterTextChange { text ->
                if (text.isEmpty() || text.isValidIfsc()) {
                    tilIfsc.error = null
                    tilIfsc.isErrorEnabled = false
                } else tilIfsc.error =
                    getString(R.string.payment_wrong_ifsc)
                pushIntent(Intent.EnteredIfsc(text))
            }
        }
    }

    private fun accountTextChangeWatcher() {
        binding.tietAccountNumber.afterTextChange { text ->
            pushIntent(Intent.EnteredAccountNumber(text))
        }
    }

    private fun upiTextChangeWatcher() {
        binding.tietUpi.afterTextChange { text ->
            pushIntent(Intent.EnteredUPI(text))
        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            binding.ivValidateDetails.clicks()
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    onConfirm()
                }
        )
    }

    private fun onConfirm(): Intent {
        return if (getCurrentState().adoptionMode == PaymentDestinationType.BANK.value) {
            val paymentAddress = "${binding.tietAccountNumber.text}@${binding.tietIfsc.text}"
            tracker.get()
                .trackEventValidateBtnClicked()
            Intent.SetDestinationToServer(paymentAddress, PaymentDestinationType.BANK.value)
        } else {
            val paymentAddress = tietUpi.text.toString()
            tracker.get()
                .trackEventValidateBtnClicked()
            Intent.SetDestinationToServer(paymentAddress, PaymentDestinationType.UPI.value)
        }
    }

    @AddTrace(name = PaymentDestinationAdoptionTraces.RENDER_PAYMENT_ADOPTION_POPUP)
    override fun render(state: State) {

        when (state.showUi) {
            UiScreenType.BANK -> {
                showBankUi(state)
            }
            UiScreenType.VALIDATE -> {
                showValidateUi()
            }
            UiScreenType.SUCCESS -> {
                showSuccessUi(state)
            }
            UiScreenType.UPI -> {
                showUpiUiScreen(state)
            }
        }
    }

    private fun showValidateUi() {
        binding.apply {
            verifyProgressBar.visible()
            ivValidateDetails.gone()
            tietAccountNumber.disable()
            tietIfsc.disable()
            tietUpi.disable()
            tvSwitchPaymentMode.disable()
        }
    }

    private fun showSuccessUi(state: State) {
        binding.apply {

            hideSoftKeyboard()

            groupSuccess.visible()
            addMethodGroup.gone()
            verifyProgressBar.gone()
            tvSwitchPaymentMode.gone()
            tvAddPaymentDescription.gone()
            tilAccountNumber.gone()
            tilIfsc.gone()
            tilUpi.gone()

            if (getCurrentState().adoptionMode == PaymentDestinationType.BANK.value) {
                showBankSuccessUi()
            } else {
                showUpiSuccessUi()
            }
            tvMerchantNameSuccess.text = state.accountHolderName
        }
    }

    private fun PaymentDestinationDialogBinding.showBankSuccessUi() {
        ivPaymentModeSuccess.setImageResource(R.drawable.ic_account_balance_bank)
        tvBankAccountSuccess.visible()
        tvBankAccountSuccess.text = tietAccountNumber.text
        tvIfscOrUpiSuccess.text = tietIfsc.text
    }

    private fun PaymentDestinationDialogBinding.showUpiSuccessUi() {
        ivPaymentModeSuccess.setImageResource(R.drawable.ic_upi_icon)
        tvBankAccountSuccess.gone()
        tvIfscOrUpiSuccess.text = getCurrentState().enteredUpi
    }

    private fun showBankUi(state: State) {

        binding.apply {

            tietIfsc.enable()
            tietAccountNumber.enable()

            // need to add explicitly to set focus listener
            tilAccountNumber.visible()
            tilIfsc.visible()

            if (currentAdoptionMode != getCurrentState().adoptionMode) {
                currentAdoptionMode = getCurrentState().adoptionMode
                tietAccountNumber.requestFocus()
            }

            addMethodGroup.visible()
            groupSuccess.gone()
            tilUpi.gone()
            verifyProgressBar.gone()

            tvAddPaymentTitle.text = getString(R.string.payment_add_bank_details)

            if (!state.showDescText) {
                binding.tvAddPaymentDescription.gone()
            } else {
                binding.tvAddPaymentDescription.visible()
                tvAddPaymentDescription.text =
                    getString(R.string.payment_to_accept_online_payment, getString(R.string.payment_add_bank_details))
            }

            if (!state.showUpiOption) {
                binding.tvSwitchPaymentMode.gone()
            } else {
                tvSwitchPaymentMode.enable()
                binding.tvSwitchPaymentMode.visible()
                tvSwitchPaymentMode.text = getString(R.string.payment_add_upi_id)
            }

            if (isInvalidBankDetails(state.enteredAccountNumber, state.enteredIfsc)) {
                ivValidateDetails.disable()
                ivValidateDetails.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey300))
            } else {
                ivValidateDetails.enable()
                ivValidateDetails.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green_primary))
            }
        }
    }

    private fun showUpiUiScreen(state: State) {
        binding.apply {

            addMethodGroup.visible()

            groupSuccess.gone()
            tilAccountNumber.gone()
            tilIfsc.gone()

            verifyProgressBar.gone()

            if (!state.showUpiOption) {
                tvSwitchPaymentMode.gone()
            } else {
                tvSwitchPaymentMode.enable()
                tvSwitchPaymentMode.text = getString(R.string.payment_add_bank_account)
                tvSwitchPaymentMode.visible()
            }

            tvAddPaymentTitle.text = getString(R.string.payment_add_upi_id)

            if (!state.showDescText) {
                binding.tvAddPaymentDescription.gone()
            } else {
                binding.tvAddPaymentDescription.visible()
                tvAddPaymentDescription.text =
                    getString(R.string.payment_inapp_upi_description)
            }

            // need to add explicitly to set focus listener

            tilUpi.visible()
            tietUpi.enable()

            if (currentAdoptionMode != getCurrentState().adoptionMode) {
                currentAdoptionMode = getCurrentState().adoptionMode
                tietUpi.requestFocus()
            }

            if (tietUpi.text.isNullOrEmpty()) {
                ivValidateDetails.disable()
                ivValidateDetails.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey300))
            } else {
                ivValidateDetails.enable()
                ivValidateDetails.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green_primary))
            }
        }
    }

    private fun handleOutsideClick() {
        val outsideView =
            dialog?.window?.decorView?.findViewById<View>(com.google.android.material.R.id.touch_outside)
        outsideView?.setOnClickListener {
            if (requireActivity().isKeyboardOpen()) {
                hideSoftKeyboard(tietAccountNumber)
            } else {
                dismiss()
            }
        }
    }

    private fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.get().goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    private fun onAccountAddedSuccessfully() {
        lifecycleScope.launch {
            delay(3000)
            if (successListener == null)
                dismiss()
            else {
                successListener!!.onDestinationSet()
            }
        }
    }

    override fun handleViewEvent(event: ViewEvents) {
        when (event) {
            is ViewEvents.GoToLogin -> gotoLogin()
            is ViewEvents.OnAccountAddedSuccessfully -> onAccountAddedSuccessfully()
            is ViewEvents.ShowError -> {
                view?.snackbar(event.errMsg, Snackbar.LENGTH_SHORT)?.show()
                tracker.get().trackEventErrorShown(event.errMsg)
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == QR_SCANNER_REQUEST_CODE) {
            data?.let {
                val upiVpa = it.getStringExtra(UPI_ID)
                tracker.get().trackEventUpiProvidedByScan()
                tietUpi.setText(upiVpa)
            }
        }
    }

    fun setDestinationSuccessListener(listener: DestinationSetSuccessListener) {
        successListener = listener
    }
}
