package `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.UpdateTransactionAmountFragmentBinding
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerContract
import `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount.UpdateTransactionAmountContract.*
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.annotation.UiThread
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.edit_text.DecimalDigitsInputFilter
import tech.okcredit.android.base.extensions.afterTextChange
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToLong

class UpdateTransactionAmountScreen :
    BaseBottomSheetWithViewEvents<State, ViewEvent, Intent>("UpdateTransactionAmountScreen") {

    private var isAmountEnteredEventSent: Boolean = false
    private var alert: Snackbar? = null
    private var listener: OnUpDateAmountDismissListener? = null

    private val binding: UpdateTransactionAmountFragmentBinding by viewLifecycleScoped(
        UpdateTransactionAmountFragmentBinding::bind
    )

    private var updateAmountPublishSubject = PublishSubject.create<Unit>()

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    interface OnUpDateAmountDismissListener {
        fun onDismiss()
    }

    companion object {
        const val TAG = "UpdateTransactionAmountScreen"

        fun newInstance(transactionId: String): UpdateTransactionAmountScreen {
            return UpdateTransactionAmountScreen().apply {
                arguments = Bundle().apply {
                    putString(CustomerContract.ARG_TXN_ID, transactionId)
                }
            }
        }
    }

    fun initialise(listener: OnUpDateAmountDismissListener) {
        this.listener = listener
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            updateAmountPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    val amountEntered = (binding.etAmount.text.toString().toDouble() * 100).roundToLong()
                    if (amountEntered > 0) {
                        Intent.UpdateTransactionAmount(amountEntered)
                    } else {
                        requireContext().shortToast(R.string.please_enter_amount_greater_than_zero)
                        Intent.NoChange
                    }
                }
        )
    }

    @SuppressLint("RestrictedApi")
    override fun render(state: State) {
        if (state.isLoading) {
            binding.submitAmount.gone()
            binding.submitLoader.visible()
        } else {
            binding.submitLoader.gone()
            binding.submitAmount.visible()
        }
        showAlertErrors(state)
    }

    private fun showAlertErrors(state: State) {
        if (state.networkError or state.error or state.isAlertVisible) {
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
    }

    private fun handleOutsideClick() {
        val outsideView = dialog?.window?.decorView?.findViewById<View>(com.google.android.material.R.id.touch_outside)
        outsideView?.setOnClickListener {
            if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                KeyboardVisibilityEvent.hideKeyboard(context, view)
            } else if (isStateInitialized() && getCurrentState().isLoading.not()) {
                dismiss()
            }
        }
    }

    internal fun showBottomSheetFullyExpanded(): BottomSheetBehavior<*> {
        val dialog = dialog as BottomSheetDialog?
        val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return behavior
    }

    internal fun disableDraggingInBottomSheet(behavior: BottomSheetBehavior<*>) {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {}

            override fun onStateChanged(view: View, state: Int) {
                if (state == BottomSheetBehavior.STATE_DRAGGING && KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                    KeyboardVisibilityEvent.hideKeyboard(context, view)
                }
            }
        })
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogThemeWithKeyboard)
    }

    override fun onStart() {
        super.onStart()
        handleOutsideClick()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.update_transaction_amount_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.viewTreeObserver.addOnGlobalLayoutListener {
            val behavior: BottomSheetBehavior<*> = showBottomSheetFullyExpanded()

            disableDraggingInBottomSheet(behavior)
        }

        binding.apply {

            submitAmount.setOnClickListener {
                if (binding.etAmount.text.isNullOrBlank()) {
                    shortToast(R.string.amount_should_not_be_empty)
                    return@setOnClickListener
                }

                updateAmountPublishSubject.onNext(Unit)

                val state = getCurrentState()
                val type =
                    if (state.transaction?.type == merchant.okcredit.accounting.model.Transaction.PAYMENT ||
                        state.transaction?.type == merchant.okcredit.accounting.model.Transaction.RETURN
                    ) {
                        PropertyValue.PAYMENT
                    } else if (state.transaction?.type == merchant.okcredit.accounting.model.Transaction.CREDIT) {
                        PropertyValue.CREDIT
                    } else {
                        "na"
                    }
                tracker.trackEvents(
                    Event.ACCOUNT_EDIT_AMOUNT_CONFIRMED,
                    type = type,
                    screen = "transaction_detail",
                    relation = PropertyValue.CUSTOMER,
                    propertiesMap = PropertiesMap.create()
                        .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                        .add(PropertyKey.OLD_AMOUNT, state.transaction?.amountV2 ?: 0)
                        .add(PropertyKey.AMOUNT, etAmount.text.toString())
                )
            }

            etAmount.filters = arrayOf(DecimalDigitsInputFilter(7, 2))

            etAmount.afterTextChange {

                if (isAmountEnteredEventSent.not() && !etAmount.text.isNullOrBlank()) {
                    val state = getCurrentState()
                    val type =
                        if (state.transaction?.type == merchant.okcredit.accounting.model.Transaction.PAYMENT || state.transaction?.type == merchant.okcredit.accounting.model.Transaction.RETURN) {
                            PropertyValue.PAYMENT
                        } else if (state.transaction?.type == merchant.okcredit.accounting.model.Transaction.CREDIT) {
                            PropertyValue.CREDIT
                        } else {
                            "na"
                        }
                    tracker.trackEvents(
                        Event.ACCOUNT_EDIT_AMOUNT_ENTERED,
                        type = type,
                        screen = "transaction_detail",
                        relation = PropertyValue.CUSTOMER,
                        propertiesMap = PropertiesMap.create()
                            .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                    )
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDismiss()
    }

    /****************************************************************
     * Navigation
     ****************************************************************/
    @UiThread
    private fun goToLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireContext())
        }
    }

    @UiThread
    private fun onAmountUpdatedSuccessfully() {
        dismiss()
    }

    override fun handleViewEvent(effect: ViewEvent) {
        when (effect) {
            is ViewEvent.GoToLogin -> goToLogin()

            is ViewEvent.AmountUpdatedSuccessfully -> onAmountUpdatedSuccessfully()
        }
    }
}
