package `in`.okcredit.frontend.utils

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

@Deprecated("Moved to Base. Import from base")
fun SpannableStringBuilder.withClickableSpan(
    start: Int,
    end: Int,
    onClickListener: () -> Unit
): SpannableStringBuilder {
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) = onClickListener.invoke()
    }
    setSpan(
        clickableSpan,
        start,
        end,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return this
}

fun View.invertVisibility() {
    if (this.visibility == View.VISIBLE) {
        this.visibility = View.GONE
    } else {
        this.visibility = View.VISIBLE
    }
}

fun ConstraintLayout.contstraintBottom(rootView: Int, bottomTextContainer: Int, miniCalculatorView: Int) {
    val constraintLayout: ConstraintLayout = findViewById(rootView)
    val constraintSet = ConstraintSet()
    constraintSet.clone(constraintLayout)
    constraintSet.connect(bottomTextContainer, ConstraintSet.BOTTOM, miniCalculatorView, ConstraintSet.TOP, 0)
    constraintSet.applyTo(constraintLayout)
}
