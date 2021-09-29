package `in`.okcredit.shared.utils

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

object SharedDrawableUtils {

    fun getDrawableWithColor(context: Context, @DrawableRes drawableRes: Int, @ColorRes colorRes: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(context, drawableRes)
        if (drawable != null) {
            drawable.mutate()
            drawable.setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_ATOP)
        }
        return drawable
    }
}
