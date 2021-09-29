package `in`.okcredit.ui.delete_customer

import `in`.okcredit.databinding.UnableToDeleteCustomerBottomSheetBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class UnableToDeleteCustomerBottomSheet(
    private val listener: UnableToDeleteCustomerBottomSheetListener? = null
) : ExpandedBottomSheetDialogFragment() {

    companion object {
        const val TAG = "UnableToDeleteCustomerBottomSheet"
    }

    private val binding: UnableToDeleteCustomerBottomSheetBinding by viewLifecycleScoped(
        UnableToDeleteCustomerBottomSheetBinding::bind
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = UnableToDeleteCustomerBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancel.setOnClickListener {
            listener?.cancel()
            dismiss()
        }
        binding.retry.setOnClickListener {
            listener?.retry()
            dismiss()
        }
    }
}

interface UnableToDeleteCustomerBottomSheetListener {
    fun cancel()
    fun retry()
}
