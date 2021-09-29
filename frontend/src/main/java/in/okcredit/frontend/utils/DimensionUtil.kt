package `in`.okcredit.frontend.utils

import android.content.Context
import android.util.TypedValue

@Deprecated("Use DimensionUtil inside base module")
object DimensionUtil {
    fun dp2px(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }
}
