package tech.okcredit.home.utils

import android.os.Build
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyControllerAdapter
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import tech.okcredit.android.base.language.LocaleManager

object TextDrawableUtils {
    fun getRoundTextDrawable(text: String): TextDrawable {
        val firstLetter = if (text.isNotEmpty()) text.first().toString() else "-"
        return TextDrawable.builder().buildRound(
            firstLetter.toUpperCase(LocaleManager.englishLocale),
            ColorGenerator.MATERIAL.getColor(text)
        )
    }
}

fun EpoxyControllerAdapter.scrollToTopOnItemInsertItem(recyclerView: RecyclerView) {
    this.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            recyclerView.scrollToPosition(0)
        }
    })
}

fun EditText.onChange(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            cb(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun TextView.htmlText(text: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY))
    } else {
        setText(Html.fromHtml(text))
    }
}
