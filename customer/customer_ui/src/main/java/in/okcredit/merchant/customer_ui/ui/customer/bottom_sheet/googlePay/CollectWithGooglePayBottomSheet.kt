package `in`.okcredit.merchant.customer_ui.ui.customer.bottom_sheet.googlePay

import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.CollectWithGooglePayBottomSheetBinding
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerContract
import `in`.okcredit.merchant.customer_ui.ui.customer.bottom_sheet.googlePay.CollectWithGooglePayContract.*
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputFilter
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.Observable
import kotlinx.coroutines.delay
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.utils.InputFilterDecimal
import timber.log.Timber

class CollectWithGooglePayBottomSheet :
    BaseBottomSheetWithViewEvents<State, ViewEvents, Intent>(
        "PaymentEditAmountBottomSheet"
    ) {

    private var isAmountEdited = false
    private var isAmountPrefilled = false

    private val binding: CollectWithGooglePayBottomSheetBinding by viewLifecycleScoped(
        CollectWithGooglePayBottomSheetBinding::bind
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.BottomSheetMaterialDialogStyleWithKeyboard)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return CollectWithGooglePayBottomSheetBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBottomSheetBehaviuor()
        setClickListener()
        amountTextChangeWatcher()
        handleBackPress()
    }

    private fun handleBackPress() {
        dialog?.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dismissAllowingStateLoss()
                return@setOnKeyListener true
            }

            return@setOnKeyListener false
        }
    }

    private fun showBottomSheetFullyExpanded(): BottomSheetBehavior<*> {
        val dialog = dialog as BottomSheetDialog?
        val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.peekHeight = 0
        return behavior
    }

    private fun disableDraggingInBottomSheet(behavior: BottomSheetBehavior<*>) {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
            }

            override fun onStateChanged(view: View, state: Int) {
                // to stop dragging of bottom sheet
                if (state == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })
    }

    private fun setBottomSheetBehaviuor() {
        view?.viewTreeObserver?.addOnGlobalLayoutListener {
            val behavior: BottomSheetBehavior<*> = showBottomSheetFullyExpanded()
            disableDraggingInBottomSheet(behavior)
        }
    }

    private fun addDecimalFilter() {
        val inputFilters = arrayOfNulls<InputFilter>(binding.etAmount.filters.size + 1)
        for ((counter, inputFilter) in binding.etAmount.filters.withIndex()) {
            inputFilters[counter] = inputFilter
        }
        inputFilters[inputFilters.size - 1] = InputFilterDecimal(9, 2) {}
        binding.etAmount.filters = inputFilters
    }

    private fun amountTextChangeWatcher() {
        addDecimalFilter()
        binding.etAmount.afterTextChange { text ->
            if (text.length == 1 && text.startsWith("0")) {
                binding.etAmount.clear()
                return@afterTextChange
            }
            if (text.isNotEmpty() && text.toDouble() > 0.0) {
                if (isStateInitialized()) {
                    if (!isAmountEdited) {
                        getCurrentState().let {
                            // we have to skip first edit event when we set pre-filled amount (already logging thr) thn
                            // only will get actual edit event
                            if (isAmountPrefilled) {
                                isAmountEdited = true
                            }
                        }
                    }
                }
                pushIntent(Intent.SetAmountEntered(text.toDouble().times(100).toLong()))
            }
        }
    }

    private fun setClickListener() {
        binding.apply {
            mbProceed.setOnClickListener {
                if (isValidAmount()) {
                    val balance = if (isStateInitialized()) (getCurrentState().currentAmountSelected ?: 0L) else 0L
                    val mobile = if (isStateInitialized()) getCurrentState().customerMobile else ""
                    pushIntent(Intent.CollectWithGooglePay(balance, mobile))
                    updateUiOnProceed()
                    hideSoftKeyboard(binding.etAmount)
                } else {
                    if (!binding.etAmount.text.isNullOrEmpty() &&
                        binding.etAmount.text.toString().toDouble() < 1
                    ) {
                        shortToast(R.string.payment_put_a_valid_amount_greater_than_1)
                    } else {
                        shortToast(R.string.payment_put_a_valid_amount)
                    }
                }
            }

            ivCross.setOnClickListener {
                dismissAllowingStateLoss()
            }
        }
    }

    private fun isValidAmount(): Boolean {
        getCurrentState().currentAmountSelected?.let {
            return !binding.etAmount.text.isNullOrEmpty() && binding.etAmount.text.toString()
                .toDouble() >= 1 && getCurrentState().dueBalance >= it
        }

        return false
    }

    private fun updateUiOnProceed() {
        binding.apply {
            etAmount.disable()
            mbProceed.text = ""
            ivLoading.visible()
            ivLoading.startAnimation(
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.payment_rotate
                )
            )
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.just(Intent.Load)
        )
    }

    override fun render(state: State) {
        setProfileUi(state)
        preFillAmountUi(state)
        setLimitReachedUi(state)
    }

    private fun preFillAmountUi(state: State) {
        if (state.dueBalance == 0L) return
        if (!isAmountPrefilled) {
            val amountInDecimal = state.dueBalance.formatDecimalString()
            binding.etAmount.setText(amountInDecimal)
            val amount = binding.etAmount.text.toString()
            if (amount.isNotEmpty()) {
                binding.etAmount.setSelection(amount.length)
            }

            // keeping after setting prefilled amount so that first edit event can be skip in text watcher
            isAmountPrefilled = true
        }
    }

    private fun Long.formatDecimalString(): String {
        val fraction: Long = this % 100
        val fractionString: String = when {
            fraction == 0L -> {
                ""
            }
            fraction < 10 -> {
                ".0$fraction"
            }
            else -> {
                ".$fraction"
            }
        }
        val digit = this / 100
        return String.format("%s%s", digit, fractionString)
    }

    private fun setLimitReachedUi(state: State) {
        state.currentAmountSelected?.let {
            binding.apply {
                if (state.currentAmountSelected <= state.dueBalance) {
                    viewAmountBorder.setBackgroundColor(getColorCompat(R.color.green_primary))
                    TextViewCompat.setCompoundDrawableTintList(
                        etAmount,
                        ColorStateList.valueOf(getColorCompat(R.color.green_primary))
                    )
                    errorAmount.gone()
                    mbProceed.enable()
                } else {
                    mbProceed.disable()
                    if (errorAmount.isVisible()) return
                    errorAmount.visible()
                    viewAmountBorder.setBackgroundColor(getColorCompat(R.color.tx_credit))
                    TextViewCompat.setCompoundDrawableTintList(
                        etAmount,
                        ColorStateList.valueOf(getColorCompat(R.color.tx_credit))
                    )
                    TransitionManager.beginDelayedTransition(textInputAmount)
                    tech.okcredit.android.base.animation.AnimationUtils.shake(textInputAmount)
                    hideErrorAfterDelay()
                }
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

    private fun setProfileUi(state: State) {
        binding.apply {
            state.let {
                Timber.i("Payment_state $state")
                tvAccountName.text = it.name
                val array = it.paymentAddress.split("@").toTypedArray()
                when (it.destinationType) {
                    CollectionDestinationType.BANK.value -> {
                        if (array.isNotEmpty()) {
                            tvAccountId.text = array[0]
                            array.let { arr ->
                                if (arr.size > 1) {
                                    tvAccountIfsc.text = arr[1]
                                    tvAccountIfsc.visible()
                                }
                            }
                        }
                    }
                    else -> {
                        tvAccountId.text = it.paymentAddress
                        tvAccountIfsc.gone()
                    }
                }
            }
        }
    }

    override fun handleViewEvent(event: ViewEvents) {
        when (event) {
            is ViewEvents.ShowError -> {
                hideProgressUi()
                longToast(event.error)
            }
            ViewEvents.CollectWithGPayRequestSent -> showSuccessAndDismiss()
            ViewEvents.DismissBottomSheet -> dismissAllowingStateLoss()
        }
    }

    private fun hideProgressUi() {
        binding.apply {
            etAmount.enable()
            mbProceed.text = getString(R.string.cta_send_collection_request)
            ivLoading.clearAnimation()
            ivLoading.gone()
        }
    }

    private fun showSuccessAndDismiss() {
        lifecycleScope.launchWhenResumed {
            binding.groupAmount.gone()
            binding.textSuccess.visible()
            binding.imageSuccess.visible()
            delay(1_000)
            dismissAllowingStateLoss()
        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    companion object {
        @JvmStatic
        fun newInstance(customerId: String): CollectWithGooglePayBottomSheet {
            val args = Bundle()
            args.putString(CustomerContract.ARG_CUSTOMER_ID, customerId)
            val fragment = CollectWithGooglePayBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }
}
