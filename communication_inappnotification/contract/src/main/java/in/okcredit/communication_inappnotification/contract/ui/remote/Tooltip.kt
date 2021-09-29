package `in`.okcredit.communication_inappnotification.contract.ui.remote

import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.contract.TargetIdType
import com.skydoves.balloon.ArrowOrientation
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
open class Tooltip(
    id: String,
    screenName: String,
    delay: Int,
    minAppBuildNumber: Int,
    maxAppBuildNumber: Int,
    priority: Int,
    expiryTime: Long,
    displayStatus: DisplayStatus,
    name: String,
    source: String,

    var title: String,
    var targetIdType: TargetIdType,
    var targetId: String,
    var targetIndex: Int, // 0-based
    var arrowPosition: Float,
    var arrowOrientation: ArrowOrientation // TOP, BOTTOM, LEFT or RIGHT
) : InAppNotification(
    id = id,
    screenName = screenName,
    delay = delay,
    minAppBuildNumber = minAppBuildNumber,
    maxAppBuildNumber = maxAppBuildNumber,
    priority = priority,
    expiryTime = expiryTime,
    kind = KIND,
    displayStatus = displayStatus,
    name = name,
    source = source
) {
    companion object {
        const val KIND = "tooltip"

        const val DEFAULT_ARROW_POSITION = 0.5f
        val DEFAULT_ARROW_ORIENTATION = ArrowOrientation.BOTTOM

        fun String.toArrowOrientation(): ArrowOrientation {
            return try {
                ArrowOrientation.valueOf(this.toUpperCase(Locale.ENGLISH))
            } catch (_: IllegalArgumentException) {
                DEFAULT_ARROW_ORIENTATION
            }
        }
    }
}
