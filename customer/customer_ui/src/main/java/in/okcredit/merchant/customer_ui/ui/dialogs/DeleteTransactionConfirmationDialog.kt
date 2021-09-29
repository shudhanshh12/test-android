package `in`.okcredit.merchant.customer_ui.ui.dialogs

import `in`.okcredit.merchant.customer_ui.databinding.DeleteTransactionConfirmationDialogBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class DeleteTransactionConfirmationDialog : ExpandedBottomSheetDialogFragment() {

    private var listener: DeleteTxnConfirmationListener? = null

    private val binding: DeleteTransactionConfirmationDialogBinding by viewLifecycleScoped(
        DeleteTransactionConfirmationDialogBinding::bind
    )

    interface DeleteTxnConfirmationListener {
        fun onDeleteTxn()

        fun onEditAmount()
    }

    companion object {
        const val TAG = "DeleteTransactionConfirmationDialog"

        fun newInstance(): DeleteTransactionConfirmationDialog {
            return DeleteTransactionConfirmationDialog()
        }
    }

    fun initialise(listener: DeleteTxnConfirmationListener) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DeleteTransactionConfirmationDialogBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            tvDeleteTxn.setOnClickListener {
                listener?.onDeleteTxn()
                dismiss()
            }

            tvEditAmount.setOnClickListener {
                listener?.onEditAmount()
                dismiss()
            }
        }
    }
}
