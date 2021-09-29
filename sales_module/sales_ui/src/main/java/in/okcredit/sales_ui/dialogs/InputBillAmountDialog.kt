package `in`.okcredit.sales_ui.dialogs

import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.databinding.DialogBillTotalBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.showSoftKeyboard
import tech.okcredit.android.base.extensions.visible

class InputBillAmountDialog : ExpandedBottomSheetDialogFragment() {

    lateinit var binding: DialogBillTotalBinding

    companion object {
        const val TAG = "InputBillAmountDialog"
    }

    interface Listener {
        fun onAddTotal(total: Double)
    }

    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogBillTotalBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDialog()
        initListeners()
        binding.billAmountEditText.requestFocus()
        showSoftKeyboard(binding.billAmountEditText)
    }

    private fun initDialog() {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog?.setOnShowListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<View>(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun initListeners() {
        binding.cleatBill.setOnClickListener {
            binding.billAmountEditText.setText("")
        }
        binding.billAmountEditText.doOnTextChanged { text, start, count, after ->
            if (text.isNullOrEmpty().not() && text!!.isNotEmpty()) {
                binding.cleatBill.visible()
            } else {
                binding.cleatBill.gone()
            }
        }
        binding.submitBillTotal.setOnClickListener {
            val total: Double =
                if (binding.billAmountEditText.text.isNullOrEmpty().not()) binding.billAmountEditText.text.toString()
                    .toDouble() else 0.0
            listener?.onAddTotal(total)
            dismiss()
        }
    }
}
