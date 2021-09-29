package tech.okcredit.home.ui.settings.dialogs

import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tech.okcredit.android.base.extensions.itOrBlank
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.home.databinding.AppPinConfirmBinding

class AppPinSetScreen : ExpandedBottomSheetDialogFragment() {

    private val binding: AppPinConfirmBinding by viewLifecycleScoped(AppPinConfirmBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = AppPinConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val message = arguments?.getString(MESSAGE).itOrBlank()
        binding.tvSetpinMessage.text = message
    }

    companion object {
        fun newInstance(message: String): AppPinSetScreen {
            val instance = AppPinSetScreen()
            val bundle = Bundle().apply { putString(MESSAGE, message) }
            instance.arguments = bundle
            return instance
        }

        private const val MESSAGE = "message"
    }
}
