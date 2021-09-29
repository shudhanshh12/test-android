package tech.okcredit.home.ui.home.dialog

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import tech.okcredit.android.base.extensions.toArrayList
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.home.R
import tech.okcredit.home.databinding.AddBankDetailsBottomSheetBinding
import javax.inject.Inject

class AddBankDetailBottomSheet : ExpandedBottomSheetDialogFragment() {

    @Inject
    lateinit var collectionNavigator: Lazy<CollectionNavigator>

    @Inject
    lateinit var tracker: Lazy<Tracker>

    private val binding: AddBankDetailsBottomSheetBinding by viewLifecycleScoped(
        AddBankDetailsBottomSheetBinding::bind
    )

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return AddBankDetailsBottomSheetBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val customerNames = arguments?.getStringArrayList("customer_names")

        binding.textMessage.text = getFormattedMessage(customerNames)

        binding.buttonAddBankDetails.setOnClickListener {
            collectionNavigator.get().showAddMerchantDestinationDialog(requireActivity().supportFragmentManager, "customer_payment_intent_home")
            tracker.get().trackInAppClickedV1(type = "customer_payment_intent", "")
            dismiss()
        }

        tracker.get().trackInAppDisplayed(type = "customer_payment_intent")
    }

    private fun getFormattedMessage(customerNames: List<String>?): String {
        if (customerNames.isNullOrEmpty()) {
            return getString(R.string.customer_add_bank_request_single, getString(R.string.customer))
        }

        return when (customerNames.size) {
            1 -> getString(R.string.customer_add_bank_request_single, customerNames[0])
            2 -> getString(R.string.customer_add_bank_request_two, customerNames[0], customerNames[1])
            else -> getString(R.string.customer_add_bank_request_multiple, customerNames[0], customerNames.size - 1)
        }
    }

    companion object {

        fun getInstance(customerNames: List<String>? = null) = AddBankDetailBottomSheet().apply {
            val args = Bundle()
            if (customerNames != null) {
                args.putStringArrayList("customer_names", customerNames.toArrayList())
            }
            arguments = args
        }
    }
}
