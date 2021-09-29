package `in`.okcredit.communication_inappnotification.contract.ui.remote

import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.contract.TargetIdType
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class TapTarget(
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
    var subtitle: String?,
    var targetIdType: TargetIdType,
    var targetId: String,
    var targetIndex: Int, // 0-based
    var radius: Float,
    var padding: Float,
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
        const val KIND = "tap_target"

        const val DEFAULT_RADIUS = 48f
        const val DEFAULT_PADDING = 10f
        const val DEFAULT_FOCAL_PADDING = 80f
    }
}
