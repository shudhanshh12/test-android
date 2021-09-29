package `in`.okcredit.frontend.ui._dialogs

import `in`.okcredit.frontend.databinding.DialogFeedbackBinding
import `in`.okcredit.shared.R
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.doOnTextChanged
import tech.okcredit.android.base.extensions.disable
import tech.okcredit.android.base.extensions.enable
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.showSoftKeyboard

class FeedBackBottomSheetDialog : ExpandedBottomSheetDialogFragment() {

    companion object {
        const val TAG = "FeedBackBottomSheetDialog"
    }

    lateinit var binding: DialogFeedbackBinding
    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding.inputFeedback.requestFocus()
        showSoftKeyboard(binding.inputFeedback)
        binding.inputFeedback.doOnTextChanged { text, start, count, after ->
            if (text.isNullOrEmpty().not()) {
                binding.submitFeedback.enable()
                binding.submitFeedback.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.green_primary))
            } else {
                binding.submitFeedback.disable()
                binding.submitFeedback.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.grey300))
            }
        }
        binding.submitFeedback.setOnClickListener {
            if (binding.inputFeedback.text.trim().isNotEmpty()) {
                listener?.onSubmitFeedBack(binding.inputFeedback.text.trim().toString())
                context?.shortToast(getString(R.string.thanks_for_the_feedback))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.inputFeedback.setText("")
    }

    interface Listener {
        fun onSubmitFeedBack(msg: String)
    }
}
