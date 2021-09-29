package `in`.okcredit.frontend.utils

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import tech.okcredit.android.base.extensions.getColorFromAttr

object DrawableUtil {

    fun getDrawableWithColor(context: Context, @DrawableRes drawableRes: Int, @ColorRes colorRes: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(context, drawableRes)
        if (drawable != null) {
            drawable.mutate()
            drawable.setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_ATOP)
        }
        return drawable
    }

    fun getDrawableWithAttributeColor(context: Context, @DrawableRes drawableRes: Int, @AttrRes colorRes: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(context, drawableRes)
        if (drawable != null) {
            drawable.mutate()
            drawable.setColorFilter(context.getColorFromAttr(colorRes), PorterDuff.Mode.SRC_ATOP)
        }
        return drawable
    }
}
