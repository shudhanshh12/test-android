package merchant.okcredit.accounting.ui.transaction_sort_options_dialog

import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.android.support.AndroidSupportInjection
import merchant.okcredit.accounting.R
import merchant.okcredit.accounting.databinding.TransactionsSortCriteriaSelectionBottomSheetBinding

class TransactionsSortCriteriaSelectionBottomSheet : ExpandedBottomSheetDialogFragment() {

    internal var listener: TransactionsSortCriteriaSelectionListener? = null

    private lateinit var binding: TransactionsSortCriteriaSelectionBottomSheetBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            val dialog = dialog as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<View>(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = TransactionsSortCriteriaSelectionBottomSheetBinding.inflate(inflater, container, false)
        setSortSelection(arguments?.getString(INITIAL_SORT_SELECTION))
        setOnCheckedChangeListener()
        return binding.root
    }

    private fun setOnCheckedChangeListener() {
        binding.radioGroupSortBy.setOnCheckedChangeListener { _, checkedId ->
            handleSelection(checkedId)
        }
    }

    private fun handleSelection(checkedId: Int) {
        val sortBy = when (checkedId) {
            R.id.radio_button_created_date -> CREATE_DATE_STRING
            R.id.radio_button_billed_date -> BILL_DATE_STRING
            else -> throw IllegalArgumentException("Unknown sort option selected")
        }
        listener?.onSortOptionSelected(sortBy)
        dismiss()
    }

    private fun setSortSelection(sortSelection: String?) {
        val id = when (sortSelection) {
            CREATE_DATE_STRING -> R.id.radio_button_created_date
            BILL_DATE_STRING -> R.id.radio_button_billed_date
            else -> throw IllegalArgumentException("Unknown sort selection: $sortSelection")
        }
        binding.radioGroupSortBy.check(id)
    }

    companion object {
        @JvmStatic
        fun newInstance(
            initialSortSelection: String?,
            listener: TransactionsSortCriteriaSelectionListener,
        ): TransactionsSortCriteriaSelectionBottomSheet {
            val bottomSheet = TransactionsSortCriteriaSelectionBottomSheet()
            bottomSheet.listener = listener
            bottomSheet.arguments = Bundle().apply { putString(INITIAL_SORT_SELECTION, initialSortSelection) }
            return bottomSheet
        }

        const val TAG: String = "SortTransactionsByOptionsBottomSheet"
        private const val INITIAL_SORT_SELECTION: String = "initial_sort_selection"
        const val CREATE_DATE_STRING: String = "create_date"
        const val BILL_DATE_STRING: String = "bill_date"
    }

    interface TransactionsSortCriteriaSelectionListener {
        fun onSortOptionSelected(sortSelection: String)
    }
}
