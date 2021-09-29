package tech.okcredit.android.base.extensions

import android.annotation.SuppressLint
import android.text.Html
import android.view.MotionEvent
import android.widget.TextView
import com.google.android.material.textview.MaterialTextView

fun TextView.setHtmlText(string: Int) {
    this.text = Html.fromHtml(context.getString(string))
}

fun TextView.setHtmlText(string: String) {
    this.text = Html.fromHtml(string)
}

@SuppressLint("ClickableViewAccessibility")
fun MaterialTextView.setRightDrawableTouchListener(action: () -> Unit) {
    setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_DOWN && event.rawX >= this.right - this.compoundDrawables[2].bounds.width()) {
            action()
            return@setOnTouchListener true
        }
        false
    }
}
