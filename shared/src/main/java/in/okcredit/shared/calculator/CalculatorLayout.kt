package `in`.okcredit.shared.calculator

import `in`.okcredit.shared.R
import `in`.okcredit.shared.base.BaseLayout
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.databinding.AddTransactionFragmentCalculatorViewBinding
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.isGone
import io.reactivex.Observable
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.visible

class CalculatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BaseLayout<CalculatorContract.State>(context, attrs, defStyleAttr),
    CalculatorContract.Interactor {

    private val binding = AddTransactionFragmentCalculatorViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        viewModel.setNavigation(this)
        initClickListeners()
    }

    private fun initClickListeners() {
        binding.btnZero.setOnClickListener { onDigitClicked(0) }
        binding.btnOne.setOnClickListener { onDigitClicked(1) }
        binding.btnTwo.setOnClickListener { onDigitClicked(2) }
        binding.btnThree.setOnClickListener { onDigitClicked(3) }
        binding.btnFour.setOnClickListener { onDigitClicked(4) }
        binding.btnFive.setOnClickListener { onDigitClicked(5) }
        binding.btnSix.setOnClickListener { onDigitClicked(6) }
        binding.btnSeven.setOnClickListener { onDigitClicked(7) }
        binding.btnEight.setOnClickListener { onDigitClicked(8) }
        binding.btnNine.setOnClickListener { onDigitClicked(9) }
        binding.btnPlus.setOnClickListener { onOperatorClicked("+") }
        binding.btnMinus.setOnClickListener { onOperatorClicked("-") }
        binding.btnMultiply.setOnClickListener { onOperatorClicked("*") }
        binding.btnDot.setOnClickListener { onDotClicked() }
        binding.btnBackspace.setOnClickListener { onBackspaceClicked() }
        binding.btnBackspace.setOnLongClickListener {
            onBackspaceLongPress()
            return@setOnLongClickListener true
        }
        binding.btnEqual.setOnClickListener { onEqualsClicked() }
    }

    private var callback: CalculatorContract.Callback? = null
    private var highLightOperators = false

    override fun loadIntent(): UserIntent {
        return CalculatorContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: CalculatorContract.State) {
        if (state.isLoading) return
        if (state.amount > 0 && state.calculatorOperatorsUsed.isNotNullOrBlank()) {
            highLightOperators = false
            clearHighLightAndAnimation()
        } else {
            if (highLightOperators && state.amountCalculation?.length == 1 && binding.imagePlusHighlight.isGone) {
                binding.imagePlusHighlight.visible()
                binding.btnPlus.setImageResource(R.drawable.ic_add_circle_filled_orange)
                binding.btnPlus.imageTintList = null

                AnimationUtils.cursorAnimation(binding.imagePlusHighlight)
            }
        }
        callback?.callbackData(state.amountCalculation?.replace("*", "x"), state.amount, state.calculatorOperatorsUsed)
        if (state.amountError) {
            callback?.isInvalidAmount()
        }
    }

    private fun clearHighLightAndAnimation() {
        binding.imagePlusHighlight.clearAnimation()
        binding.imagePlusHighlight.gone()
        binding.btnMinus.imageTintList = ColorStateList.valueOf(context.getColorCompat(R.color.grey800))
        binding.btnMultiply.imageTintList = ColorStateList.valueOf(context.getColorCompat(R.color.grey800))
        binding.btnPlus.setImageResource(R.drawable.ic_add)
        binding.btnPlus.imageTintList = ColorStateList.valueOf(context.getColorCompat(R.color.grey800))
        binding.btnEqual.setTextColor(context.getColorCompat(R.color.grey800))
        binding.btnBackspace.imageTintList = ColorStateList.valueOf(context.getColorCompat(R.color.grey800))
    }

    fun highlightOperators() {
        highLightOperators = true
        binding.btnMinus.imageTintList = ColorStateList.valueOf(context.getColorCompat(R.color.indigo_ada))
        binding.btnMultiply.imageTintList = ColorStateList.valueOf(context.getColorCompat(R.color.indigo_ada))
        binding.btnPlus.setImageResource(R.drawable.ic_add)
        binding.btnPlus.imageTintList = ColorStateList.valueOf(context.getColorCompat(R.color.indigo_ada))
        binding.btnEqual.setTextColor(context.getColorCompat(R.color.indigo_ada))
        binding.btnBackspace.imageTintList = ColorStateList.valueOf(context.getColorCompat(R.color.red_ada))
    }

    fun setData(
        callback: CalculatorContract.Callback,
        initialAmount: Long,
        amountCalculation: String,
    ) {
        this.callback = callback
        pushIntent(
            CalculatorContract.Intent.LoadInitialData(
                CalculatorContract.InitialData(
                    initialAmount,
                    amountCalculation
                )
            )
        )
    }

    fun clearAmount() {
        pushIntent(CalculatorContract.Intent.ClearAmount)
    }

    private fun onDigitClicked(digit: Int) {
        pushIntent(CalculatorContract.Intent.OnDigitClicked(digit))
    }

    private fun onOperatorClicked(operator: String) {
        pushIntent(CalculatorContract.Intent.OnOperatorClicked(operator))
    }

    private fun onEqualsClicked() {
        pushIntent(CalculatorContract.Intent.OnEqualClicked)
    }

    private fun onDotClicked() {
        pushIntent(CalculatorContract.Intent.OnDotClicked)
    }

    private fun onBackspaceLongPress() {
        pushIntent(CalculatorContract.Intent.OnLongPressBackSpace)
    }

    private fun onBackspaceClicked() {
        pushIntent(CalculatorContract.Intent.OnBackSpaceClicked)
    }
}
