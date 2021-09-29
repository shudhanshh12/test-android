package `in`.okcredit.merchant.customer_ui.ui.dialogs

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.DialogAutoDueDateBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class AutoDueDateDialog : ExpandedBottomSheetDialogFragment() {

    private val binding: DialogAutoDueDateBinding by viewLifecycleScoped(DialogAutoDueDateBinding::bind)

    private var mListener: Listener? = null

    interface Listener {
        fun onEditDueDate(action: String)
        fun onOkay()
        fun onDismissAutoDueDialog()
        fun onDisplayed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DialogAutoDueDateBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val date = it.getString(DATE, "")
            binding.date.text = date
        }
        binding.edit.setOnClickListener {
            edit("edit_button")
        }
        binding.date.setOnClickListener {
            edit("date")
        }
        binding.ok.setOnClickListener {
            mListener?.onOkay()
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        mListener?.onDisplayed()
    }

    private fun edit(action: String) {
        mListener?.onEditDueDate(action)
        dismiss()
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        mListener?.onDismissAutoDueDialog()
        super.onDismiss(dialog)
    }

    companion object {
        const val TAG = "AutoDueDateDialog"
        const val DATE = "date"

        fun newInstance(date: String?): AutoDueDateDialog {
            val fragment = AutoDueDateDialog()
            val bundle = Bundle()
            bundle.putString(DATE, date)
            fragment.arguments = bundle
            return fragment
        }
    }
}
