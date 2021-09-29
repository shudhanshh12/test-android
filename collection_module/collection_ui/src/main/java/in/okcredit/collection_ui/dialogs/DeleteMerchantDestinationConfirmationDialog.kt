package `in`.okcredit.collection_ui.dialogs

import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.DeleteMerchantDestinationConfirmationDialogBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class DeleteMerchantDestinationConfirmationDialog : ExpandedBottomSheetDialogFragment() {

    private var listener: DeleteMerchantDestinationListener? = null

    private val binding: DeleteMerchantDestinationConfirmationDialogBinding by viewLifecycleScoped(
        DeleteMerchantDestinationConfirmationDialogBinding::bind
    )

    companion object {
        const val TAG = "DeleteMerchantDestinationConfirmationDialog"
        private const val IS_KYC_COMPLETED = "is_kyc_completed"
        fun newInstance(isKycCompleted: Boolean = false): DeleteMerchantDestinationConfirmationDialog {
            val bundle = Bundle().apply {
                putBoolean(IS_KYC_COMPLETED, isKycCompleted)
            }
            return DeleteMerchantDestinationConfirmationDialog().apply {
                arguments = bundle
            }
        }
    }

    interface DeleteMerchantDestinationListener {
        fun onDelete()

        fun onCancel()
    }

    fun initialise(listener: DeleteMerchantDestinationListener) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DeleteMerchantDestinationConfirmationDialogBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBundleArguments()
        render()
    }

    private fun getBundleArguments() {
        arguments?.getBoolean(IS_KYC_COMPLETED)?.let {
            if (it) setTextForKycRisk()
        }
    }

    private fun setTextForKycRisk() {
        binding.tvDeleteInfo.text = context?.getString(R.string.if_you_delete_account_kyc)
    }

    private fun render() {
        binding.apply {
            cancel.setOnClickListener {
                dismiss()
                listener?.onCancel()
            }

            delete.setOnClickListener {
                dismiss()
                listener?.onDelete()
            }
        }
    }
}
