package `in`.okcredit.sales_ui.ui.add_sales.views

import `in`.okcredit.sales_ui.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.longClicks
import kotlinx.android.synthetic.main.add_sale_calculator_view.view.*

class CalculatorView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    interface CalcListener {
        fun onDigitClicked(d: Int)

        fun onOperatorClicked(d: String)

        fun onDotClicked()

        fun onEqualsClicked()

        fun onBackspaceClicked()

        fun onBackspaceLongPress()
    }

    init {
        LayoutInflater.from(ctx).inflate(R.layout.add_sale_calculator_view, this, true)
    }

    fun setListener(listener: CalcListener?) {
        btn_zero.clicks()
            .doOnNext {
                listener?.onDigitClicked(0)
            }
            .subscribe()

        btn_one.clicks()
            .doOnNext {
                listener?.onDigitClicked(1)
            }
            .subscribe()

        btn_two.clicks()
            .doOnNext {
                listener?.onDigitClicked(2)
            }
            .subscribe()

        btn_three.clicks()
            .doOnNext {
                listener?.onDigitClicked(3)
            }
            .subscribe()

        btn_four.clicks()
            .doOnNext {
                listener?.onDigitClicked(4)
            }
            .subscribe()

        btn_five.clicks()
            .doOnNext {
                listener?.onDigitClicked(5)
            }
            .subscribe()

        btn_six.clicks()
            .doOnNext {
                listener?.onDigitClicked(6)
            }
            .subscribe()

        btn_seven.clicks()
            .doOnNext {
                listener?.onDigitClicked(7)
            }
            .subscribe()

        btn_eight.clicks()
            .doOnNext {
                listener?.onDigitClicked(8)
            }
            .subscribe()

        btn_nine.clicks()
            .doOnNext {
                listener?.onDigitClicked(9)
            }
            .subscribe()

        btn_plus.clicks()
            .doOnNext {
                listener?.onOperatorClicked("+")
            }
            .subscribe()

        btn_minus.clicks()
            .doOnNext {
                listener?.onOperatorClicked("-")
            }
            .subscribe()

        btn_multiply.clicks()
            .doOnNext {
                listener?.onOperatorClicked("*")
            }
            .subscribe()

        btn_dot.clicks()
            .doOnNext {
                listener?.onDotClicked()
            }
            .subscribe()

        btn_backspace.clicks()
            .doOnNext {
                listener?.onBackspaceClicked()
            }
            .subscribe()

        btn_backspace.longClicks()
            .doOnNext {
                listener?.onBackspaceLongPress()
            }
            .subscribe()

        btn_equal.clicks()
            .doOnNext {
                listener?.onEqualsClicked()
            }
            .subscribe()
    }
}
