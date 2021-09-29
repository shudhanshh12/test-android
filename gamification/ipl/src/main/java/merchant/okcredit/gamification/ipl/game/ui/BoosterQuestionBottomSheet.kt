package merchant.okcredit.gamification.ipl.game.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.booster_question_bottom_sheet.*
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.BoosterQuestionBottomSheetBinding
import merchant.okcredit.gamification.ipl.game.data.server.model.response.BoosterQuestion
import merchant.okcredit.gamification.ipl.game.data.server.model.response.QuestionType
import tech.okcredit.android.base.extensions.afterTextChange
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.showSoftKeyboard
import tech.okcredit.android.base.extensions.visible

class BoosterQuestionBottomSheet : BottomSheetDialogFragment() {
    private val question: BoosterQuestion.Question by lazy { arguments?.getParcelable<BoosterQuestion.Question>("booster_question") as BoosterQuestion.Question }

    private lateinit var binding: BoosterQuestionBottomSheetBinding
    private var boosterSubmitListener: BoosterSubmitListener? = null

    private var selectedChoice = ""
    private var selectedChoicePosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetMaterialDialogStyleOverKeyboard)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            if (it is BottomSheetDialog) {
                val bottomSheet: FrameLayout? = it.findViewById(R.id.design_bottom_sheet)
                bottomSheet?.let {
                    val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BoosterQuestionBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvQuestion.text = question.text
        binding.tvSubmit.text = question.cta
        if (question.questionType == QuestionType.MCQ) {
            setMCQQuestionUi()
        } else {
            setOpenQuestionUi()
        }

        binding.tvSubmit.setOnClickListener {
            val choice = if (question.questionType == QuestionType.MCQ) {
                selectedChoice
            } else {
                binding.editTextAnswer.text.toString()
            }
            boosterSubmitListener?.boosterSubmitted(question, choice)
            dismiss()
        }
        binding.tvCancel.setOnClickListener { dismiss() }
    }

    private fun setOpenQuestionUi() {
        binding.apply {
            hideCheckboxes()
            textInputAnswer.visible()
            showSoftKeyboard(editTextAnswer)
            editTextAnswer.afterTextChange {
                tvSubmit.isEnabled = it.isNotEmpty()
            }
        }
    }

    private fun hideCheckboxes() {
        binding.apply {
            tvOption1.gone()
            tvOption2.gone()
            tvOption3.gone()
            tvOption4.gone()
        }
    }

    private fun updatedSelectedPosition(currentPosition: Int) {
        if (selectedChoicePosition != -1) {
            when (selectedChoicePosition) {
                1 -> tv_option_1.isChecked = false
                2 -> tv_option_2.isChecked = false
                3 -> tv_option_3.isChecked = false
                4 -> tv_option_4.isChecked = false
            }
        }

        selectedChoicePosition = currentPosition
        binding.tvSubmit.isEnabled = selectedChoicePosition > 0
    }

    private fun setMCQQuestionUi() {
        binding.apply {
            textInputAnswer.gone()
            tvOption1.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    selectedChoice = buttonView.text.toString()
                    updatedSelectedPosition(1)
                } else {
                    updatedSelectedPosition(-1)
                }
            }
            tvOption2.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    selectedChoice = buttonView.text.toString()
                    updatedSelectedPosition(2)
                } else {
                    updatedSelectedPosition(-1)
                }
            }
            tvOption3.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    selectedChoice = buttonView.text.toString()
                    updatedSelectedPosition(3)
                } else {
                    updatedSelectedPosition(-1)
                }
            }
            tvOption4.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    selectedChoice = buttonView.text.toString()
                    updatedSelectedPosition(4)
                } else {
                    updatedSelectedPosition(-1)
                }
            }
            question.mcqs.forEachIndexed { index, text ->
                when (index) {
                    0 -> tvOption1.text = text
                    1 -> tvOption2.text = text
                    2 -> tvOption3.text = text
                    3 -> tvOption4.text = text
                }
            }
            when (question.mcqs.size) {
                2 -> {
                    tvOption1.visible()
                    tvOption2.visible()
                    tvOption3.gone()
                    tvOption4.gone()
                }
                else -> {
                    tvOption1.visible()
                    tvOption2.visible()
                    tvOption3.visible()
                    tvOption4.visible()
                }
            }
        }
    }

    fun setListener(boosterSubmitListener: BoosterSubmitListener) {
        this.boosterSubmitListener = boosterSubmitListener
    }

    companion object {

        @JvmStatic
        fun getInstance(question: BoosterQuestion.Question) = BoosterQuestionBottomSheet().apply {
            val bundle = Bundle()
            bundle.putParcelable("booster_question", question)
            arguments = bundle
        }
    }

    interface BoosterSubmitListener {
        fun boosterSubmitted(question: BoosterQuestion.Question, choice: String)
    }
}
