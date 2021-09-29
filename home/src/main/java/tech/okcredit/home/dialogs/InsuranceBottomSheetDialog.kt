package tech.okcredit.home.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.okcredit.home.databinding.BottomSheetInappInsuranceDialogBinding

class InsuranceBottomSheetDialog : BottomSheetDialogFragment() {

    private var listener: InsuranceActionListener? = null
    private lateinit var binding: BottomSheetInappInsuranceDialogBinding

    interface InsuranceActionListener {

        fun onFirstActionClick()

        fun onSecondActionClick()
    }

    override fun onStart() {
        super.onStart()
        val deviceWidth = context!!.resources.displayMetrics.widthPixels
        dialog?.window?.setLayout(deviceWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetInappInsuranceDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvYes.setOnClickListener {
            listener?.onFirstActionClick()
            dialog?.dismiss()
        }

        binding.tvNotNow.setOnClickListener {
            listener?.onSecondActionClick()
            dialog?.dismiss()
        }
    }

    companion object {
        const val TAG = "InsuranceBottomSheetDialog"

        fun newInstance(): InsuranceBottomSheetDialog {
            return InsuranceBottomSheetDialog()
        }
    }

    fun setListener(listener: InsuranceActionListener) {
        this.listener = listener
    }
}
