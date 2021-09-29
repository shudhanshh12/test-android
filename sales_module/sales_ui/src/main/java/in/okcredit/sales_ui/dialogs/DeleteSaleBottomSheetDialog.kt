package `in`.okcredit.sales_ui.dialogs

import `in`.okcredit.sales_ui.databinding.DialogDeleteSaleBinding
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DeleteSaleBottomSheetDialog : BottomSheetDialogFragment() {
    private var listener: DeleteDialogListener? = null

    interface DeleteDialogListener {
        fun onDelete(saleId: String)
        fun onCancel()
    }

    lateinit var binding: DialogDeleteSaleBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogDeleteSaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DeleteDialogListener) {
            listener = context
        }
    }

    fun setListener(listener: DeleteDialogListener) {
        this.listener = listener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val id = it.getString(ID) ?: ""
            binding.yes.setOnClickListener {
                listener?.onDelete(id)
                dismiss()
            }
            binding.cancel.setOnClickListener {
                listener?.onCancel()
                dismiss()
            }
        }
    }

    companion object {
        val ID = "id"
        val TAG = "DeleteSaleBottomSheetDialog"
        fun newInstance(saleId: String): DeleteSaleBottomSheetDialog {
            val fragment = DeleteSaleBottomSheetDialog()
            val args = Bundle()
            args.putString(ID, saleId)
            fragment.arguments = args
            return fragment
        }
    }
}
