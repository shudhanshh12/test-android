package `in`.okcredit.frontend.ui._dialogs

import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.databinding.DeleteExpenseDialogBinding
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.delete_expense_dialog.*

class DeleteExpenseBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DeleteExpenseDialogBinding

    private var listner: DeleteDialogListener? = null

    private var expenseId: String = ""

    interface DeleteDialogListener {
        fun onDelete(expenseId: String)
        fun onCancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DeleteExpenseDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DeleteDialogListener) {
            listner = context
        }
    }

    fun setListener(listener: DeleteDialogListener) {
        this.listner = listener
    }

    fun setExpenseId(id: String) {
        expenseId = id
    }

    override fun onDetach() {
        listner = null
        super.onDetach()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        yes.setOnClickListener {
            listner?.onDelete(expenseId)
        }
        cancel.setOnClickListener {
            listner?.onCancel()
        }
    }

    companion object {
        val ID = "id"
        val TAG = "DeleteExpenseBottomSheetDialog"
    }
}
