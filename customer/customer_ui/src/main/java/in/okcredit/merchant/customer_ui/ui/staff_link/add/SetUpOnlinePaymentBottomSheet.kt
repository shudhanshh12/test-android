package `in`.okcredit.merchant.customer_ui.ui.staff_link.add

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.BottomSheetSetupOnlinePayementsBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class SetUpOnlinePaymentBottomSheet : ExpandedBottomSheetDialogFragment() {

    private var listener: SetupOnlinePaymentListener? = null

    private val binding: BottomSheetSetupOnlinePayementsBinding by viewLifecycleScoped(
        BottomSheetSetupOnlinePayementsBinding::bind
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BottomSheetSetupOnlinePayementsBinding.inflate(inflater, container, false).root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.BottomSheetMaterialDialogStyle)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonSubmit.setOnClickListener {
            listener?.onSetUpConfirmed()
            dismiss()
        }

        binding.imageClose.setOnClickListener {
            listener?.onSetUpCancelled()
            dismiss()
        }
    }

    fun setListener(setupOnlinePaymentListener: SetupOnlinePaymentListener) {
        this.listener = setupOnlinePaymentListener
    }

    companion object {
        @JvmStatic
        fun getInstance() = SetUpOnlinePaymentBottomSheet()
    }

    interface SetupOnlinePaymentListener {
        fun onSetUpCancelled()
        fun onSetUpConfirmed()
    }
}
