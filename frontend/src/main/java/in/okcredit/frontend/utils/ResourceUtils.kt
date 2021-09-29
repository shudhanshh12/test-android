package `in`.okcredit.frontend.utils

import android.content.Context
import androidx.annotation.AttrRes
import tech.okcredit.android.base.extensions.getColorFromAttr

object ResourceUtils {
    fun getColorFromAttr(context: Context, @AttrRes attrColor: Int): Int = context.getColorFromAttr(attrColor)
}
