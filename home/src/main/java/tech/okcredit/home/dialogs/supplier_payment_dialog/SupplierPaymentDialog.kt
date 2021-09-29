package tech.okcredit.home.dialogs.supplier_payment_dialog

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Screen
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.base.BaseDialogFragmentScreen
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.home.R
import tech.okcredit.home.databinding.SupplierPaymentDialogBinding
import javax.inject.Inject

class SupplierPaymentDialog : BaseDialogFragmentScreen<SupplierPaymentContract.State>() {

    private var binding: SupplierPaymentDialogBinding? = null
    private var listener: SupplierPaymentListener? = null

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var tracker: Lazy<Tracker>

    interface SupplierPaymentListener {
        fun onCallIconClicked(supplier: Supplier)
    }

    companion object {
        const val TAG = "SupplierPaymentDialog"

        fun newInstance(supplierId: String): SupplierPaymentDialog {
            val bundle = Bundle().apply {
                putString(SupplierPaymentContract.ARG_SUPPLIER_ID, supplierId)
            }
            return SupplierPaymentDialog().apply {
                arguments = bundle
            }
        }
    }

    fun initialise(listener: SupplierPaymentListener) {
        this.listener = listener
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding?.callSupplier?.setOnClickListener {
            dismiss()
            val state = getCurrentState()
            if (state.supplier != null) {
                listener?.onCallIconClicked(state.supplier)
            }
        }

        binding?.payOnline?.setOnClickListener {
            dismiss()
            val state = getCurrentState()
            val messageLink = state.collectionCustomerProfile?.message_link

            if (!messageLink.isNullOrBlank()) {
                goToWebActivity(messageLink)
            }

            tracker.get().trackEvents(
                Event.ONLINE_PAYMENT_CLICK,
                screen = Screen.SUPPLIER_PAYMENT_DIALOG_SCREEN,
                relation = PropertyValue.SUPPLIER,
                propertiesMap = PropertiesMap.create()
                    .add(PropertyKey.ACCOUNT_ID, state.supplier?.id ?: "")
                    .add(PropertyKey.DUE_AMOUNT, state.supplier?.balance ?: "")
            )
        }
    }

    private fun goToWebActivity(messageLink: String) {
        activity?.runOnUiThread {
            legacyNavigator.get().goToWebViewScreen(requireActivity(), messageLink)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SupplierPaymentDialogBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        val deviceWidth = context!!.resources.displayMetrics.widthPixels
        dialog?.window?.setLayout(deviceWidth, RelativeLayout.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun loadIntent(): UserIntent {
        return SupplierPaymentContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: SupplierPaymentContract.State) {

        binding?.supplierName?.text = state.supplier?.name

        if (binding?.supplierImage != null) {
            Glide.with(this)
                .load(state.supplier?.profileImage)
                .circleCrop()
                .placeholder(R.drawable.ic_account_125dp)
                .error(R.drawable.ic_account_125dp)
                .into(binding?.supplierImage!!)
        }
    }
}
