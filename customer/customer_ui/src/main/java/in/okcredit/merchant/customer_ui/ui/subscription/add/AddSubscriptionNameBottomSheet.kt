package `in`.okcredit.merchant.customer_ui.ui.subscription.add

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.TxnAddNotesBottomSheetBinding
import `in`.okcredit.merchant.customer_ui.utils.SpecialCharacterInputFilter
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.app.Dialog
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tech.okcredit.android.base.extensions.*

class AddSubscriptionNameBottomSheet : ExpandedBottomSheetDialogFragment() {

    private val binding by viewLifecycleScoped(TxnAddNotesBottomSheetBinding::bind)

    private var listener: NameSubmitListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.BottomSheetMaterialDialogStyleWithKeyboard)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return TxnAddNotesBottomSheetBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUiForAddName()

        binding.buttonSubmit.setOnClickListener {
            hideSoftKeyboard(binding.editTextAnswer)
            it.postDelayed(
                {
                    listener?.onTextSubmitted(binding.editTextAnswer.text.toString())
                    dismiss()
                },
                300
            )
        }

        showSoftKeyboard(binding.editTextAnswer)
    }

    private fun setUiForAddName() {
        val existingNote = arguments?.getString(PARAM_EXISTING_NAME)
        if (existingNote.isNotNullOrBlank()) {
            binding.editTextAnswer.setText(existingNote)
        }
        binding.buttonForgotPwd.gone()
        binding.tvTitle.text = getString(R.string.add_subscription_name)
        binding.textInputAnswer.startIconDrawable = getDrawableCompact(R.drawable.ic_notes)
        binding.voiceIconContainer.gone()
        binding.editTextAnswer.apply {
            inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS or InputType.TYPE_CLASS_TEXT
            maxLines = 1
            hint = getString(R.string.start_typing)
        }
        setInputFilters()
    }

    private fun setInputFilters() {
        val inputFilters = arrayOfNulls<InputFilter>(binding.editTextAnswer.filters.size + 2)
        for ((counter, inputFilter) in binding.editTextAnswer.filters.withIndex()) {
            inputFilters[counter] = inputFilter
        }
        inputFilters[inputFilters.size - 2] = InputFilter.LengthFilter(MAX_NAME_LENGTH)
        inputFilters[inputFilters.size - 1] = SpecialCharacterInputFilter()
        binding.editTextAnswer.filters = inputFilters
    }

    fun setListener(notesSubmitListener: NameSubmitListener) {
        this.listener = notesSubmitListener
    }

    companion object {
        @JvmStatic
        fun getInstance(existingNote: String? = null) = AddSubscriptionNameBottomSheet().apply {
            val bundle = Bundle()
            existingNote?.let {
                bundle.putString(PARAM_EXISTING_NAME, it)
            }
            arguments = bundle
        }

        private const val PARAM_EXISTING_NAME = "existing_name"

        private const val MAX_NAME_LENGTH = 50
    }

    interface NameSubmitListener {
        fun onTextSubmitted(name: String)
    }
}
