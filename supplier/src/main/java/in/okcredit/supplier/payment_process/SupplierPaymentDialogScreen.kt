package `in`.okcredit.supplier.payment_process

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.supplier.R
import `in`.okcredit.supplier.databinding.DialogSupplierPaymentBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import merchant.okcredit.supplier.contract.SupplierPaymentListener
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SupplierPaymentDialogScreen :
    BaseBottomSheetWithViewEvents<SupplierPaymentDialogContract.State, SupplierPaymentDialogContract.ViewEvent, SupplierPaymentDialogContract.Intent>(
        "SupplierPaymentDialogScreen"
    ) {

    private lateinit var binding: DialogSupplierPaymentBinding

    @Inject
    internal lateinit var tracker: Tracker

    private var supplierPaymentListener: SupplierPaymentListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogSupplierPaymentBinding.inflate(inflater)
        return binding.root
    }

    fun setListener(supplierPaymentListener: SupplierPaymentListener) {
        this.supplierPaymentListener = supplierPaymentListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<View>(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun loadIntent(): UserIntent {
        return SupplierPaymentDialogContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.confirm.clicks()
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    SupplierPaymentDialogContract.Intent.OnConfirmClicked
                },
            binding.changeDetails.clicks()
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    SupplierPaymentDialogContract.Intent.OnChangeDetails
                }
        )
    }

    override fun render(state: SupplierPaymentDialogContract.State) {
        binding.apply {
            state.let { state ->
                name.text = state.name
                val array = state.paymentAddress.split("@").toTypedArray()
                when (state.destinationType) {
                    CollectionDestinationType.BANK.value -> {
                        profileImage.setImageResource(R.drawable.ic_account_balance_bank)
                        if (array.isNotEmpty()) {
                            paymentAddress.text = array[0]
                            array.let {
                                if (it.size > 1) {
                                    ifsc.text = it[1]
                                    ifsc.visible()
                                }
                            }
                        }
                    }
                    else -> {
                        paymentAddress.text = state.paymentAddress
                        ifsc.gone()
                    }
                }
            }
        }
    }

    private fun onConfirmClicked() {
        val state = getCurrentState()
        val type = if (state.destinationType == CollectionDestinationType.BANK.value)
            PropertyValue.BANK
        else
            PropertyValue.UPI

        tracker.trackEvents(
            Event.CLICK_PROCEED_PAYMENT,
            type = type,
            screen = getCurrentState().getScreenFrmAccountType(),
            relation = getCurrentState().getRelationFrmAccountType(),
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.ACCOUNT_ID, state.accountId)
        )

        state.messageLink?.let {
            supplierPaymentListener?.onConfirm(it, this)
        }
    }

    private fun onChangeDetails() {
        val state = getCurrentState()
        val type = if (state.destinationType == CollectionDestinationType.BANK.value)
            PropertyValue.BANK
        else
            PropertyValue.UPI
        tracker.trackEvents(
            Event.CHANGE_PAYMENT_DETAILS,
            type = type,
            screen = getCurrentState().getScreenFrmAccountType(),
            relation = getCurrentState().getRelationFrmAccountType(),
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.ACCOUNT_ID, state.accountId)
                .add(PropertyKey.DUE_AMOUNT, state.balance)
        )
        supplierPaymentListener?.onChangeDetails(this)
    }

    override fun handleViewEvent(event: SupplierPaymentDialogContract.ViewEvent) {
        when (event) {
            SupplierPaymentDialogContract.ViewEvent.OnConfirmClicked -> onConfirmClicked()
            SupplierPaymentDialogContract.ViewEvent.OnChangeDetails -> onChangeDetails()
        }
    }

    companion object {
        const val ARG_SUPPLIER_ACCOUNT_ID = "account_id"
        const val ARG_ACCOUNT_BALANCE = "account_balance"
        const val ARG_SUPPLIER_MESSAGE_LINK = "message_link"
        const val ARG_SUPPLIER_MOBILE = "mobile"
        const val ARG_SUPPLIER_PAYMENT_ADDRESS = "payment_address"
        const val ARG_SUPPLIER_DESTINATION_TYPE = "destination_type"
        const val ARG_SUPPLIER_NAME = "name"
        const val ARG_ACCOUNT_TYPE = "account_type"
        const val TAG = "SupplierPaymentDialogScreen"
        fun newInstance(
            accountId: String = "",
            mobile: String = "",
            balance: Long = 0L,
            destinationType: String = "",
            messageLink: String? = "",
            paymentAddress: String = "",
            name: String = "",
            accountType: String = ""
        ): SupplierPaymentDialogScreen {
            val args = Bundle()
            args.putString(ARG_SUPPLIER_ACCOUNT_ID, accountId)
            args.putString(ARG_SUPPLIER_MOBILE, mobile)
            args.putLong(ARG_ACCOUNT_BALANCE, balance)
            args.putString(ARG_SUPPLIER_DESTINATION_TYPE, destinationType)
            args.putString(ARG_SUPPLIER_MESSAGE_LINK, messageLink)
            args.putString(ARG_SUPPLIER_PAYMENT_ADDRESS, paymentAddress)
            args.putString(ARG_SUPPLIER_NAME, name)
            args.putString(ARG_ACCOUNT_TYPE, accountType)
            val fragment = SupplierPaymentDialogScreen()
            fragment.arguments = args
            return fragment
        }
    }
}
