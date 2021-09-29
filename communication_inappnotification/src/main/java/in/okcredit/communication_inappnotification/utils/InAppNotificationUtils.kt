package `in`.okcredit.communication_inappnotification.utils

import android.content.Context
import tech.okcredit.android.base.extensions.dpToPixel

object InAppNotificationUtils {

    fun Int.dpToPixel(context: Context) = context.dpToPixel(this.toFloat()).toInt()
}
