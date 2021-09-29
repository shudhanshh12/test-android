package merchant.okcredit.gamification.ipl.game.ui

import `in`.okcredit.shared.R
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import merchant.okcredit.gamification.ipl.databinding.BoosterSuccessBottomSheetBinding

class BoosterSuccessBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BoosterSuccessBottomSheetBinding
    private var dismissListener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.BottomSheetMaterialDialogStyle)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BoosterSuccessBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val message = arguments?.getString(SUCCESS_MESSAGE)
        message.let { binding.tvMessage.text = it }

        lifecycleScope.launch {
            delay(2_000)
            dismissListener?.invoke()
            dismiss()
        }
    }

    fun setListener(dismissListener: (() -> Unit)) {
        this.dismissListener = dismissListener
    }

    companion object {
        const val SUCCESS_MESSAGE = "success_message"

        @JvmStatic
        fun newInstance(message: String) = BoosterSuccessBottomSheet().apply {
            val bundle = Bundle()
            bundle.putString(SUCCESS_MESSAGE, message)
            arguments = bundle
        }
    }
}
