package `in`.okcredit.merchant.customer_ui.ui.discount_info

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.customer_ui.databinding.CustomerAddTxnDiscountInfoDialogScreenBinding
import `in`.okcredit.merchant.customer_ui.ui.discount_info.CustomerAddTxnDiscountInfoDialogContract.*
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.utils.KeyboardUtil.hideKeyboard
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent.isKeyboardVisible

class CustomerAddTxnDiscountInfoDialogScreen :
    BaseBottomSheetWithViewEvents<State, ViewEvents, Intent>("CustomerAddTxnDiscountInfoDialogScreen") {

    private val binding: CustomerAddTxnDiscountInfoDialogScreenBinding by viewLifecycleScoped(
        CustomerAddTxnDiscountInfoDialogScreenBinding::bind
    )

    override fun onStart() {
        super.onStart()
        handleOutsideClick()
    }

    private fun handleOutsideClick() {
        val outsideView = dialog?.window?.decorView?.findViewById<View>(com.google.android.material.R.id.touch_outside)
        outsideView?.setOnClickListener {
            if (isKeyboardVisible(activity)) {
                hideKeyboard(context, view)
            } else {
                dismiss()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return CustomerAddTxnDiscountInfoDialogScreenBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val behavior: BottomSheetBehavior<*> = showBottomSheetFullyExpanded()
            disableDraggingInBottomSheet(behavior)
        }
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

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: State) {
        if (state.creditAmount.isNotBlank() && state.discountAmount.isNotBlank() && state.netAmount.isNotBlank()) {
            CurrencyUtil.renderCustomerCredit(state.creditAmount.toLong(), binding.creditAmounnt)
            CurrencyUtil.renderAmount(state.discountAmount.toLong(), binding.discountAmount)
            CurrencyUtil.renderAmount(state.netAmount.toLong(), binding.netAmount)
        }
    }

    override fun handleViewEvent(event: ViewEvents) {}
}
