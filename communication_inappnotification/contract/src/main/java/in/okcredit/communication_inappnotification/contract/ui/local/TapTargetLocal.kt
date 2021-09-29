package `in`.okcredit.communication_inappnotification.contract.ui.local

import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.TargetIdType
import `in`.okcredit.communication_inappnotification.contract.ui.remote.TapTarget
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.lang.ref.WeakReference

class TapTargetLocal(
    screenName: String,
    title: String,
    subtitle: String? = null,
    radius: Float = DEFAULT_RADIUS,
    padding: Float = DEFAULT_PADDING,
    targetId: String = "",

    /**
     * Either pass @param targetView or @param targetViewId
     */
    var targetView: WeakReference<View?>,
    var targetViewId: Int? = null,
    /**
     * Extra parameters for building UI locally
     */
    var titleTextSize: Float = 20f,
    var titleTypeFace: Typeface = Typeface.DEFAULT_BOLD,
    var titleTypeFaceStyle: Int? = null,
    var titleGravity: Int = Gravity.START,
    var titleTextColor: Int? = null,

    var subtitleTextSize: Float = 14f,
    var subtitleTypeFace: Typeface = Typeface.DEFAULT,
    var subtitleTypeFaceStyle: Int? = null,
    var subtitleGravity: Int = Gravity.START,
    var subtitleTextColor: Int? = null,

    var outerRadius: Float? = null,
    var focalRadius: Float? = null,
    var enableTouchEventOutsidePrompt: Boolean = true,
    var enableBackButtonDismiss: Boolean = true,
    var backgroundColor: Int? = null,
    var focalPadding: Float = DEFAULT_FOCAL_PADDING,

    /**
     * Listener to manage state change of prompt
     */
    var listener: MaterialTapTargetPrompt.PromptStateChangeListener? = null,
) : TapTarget(
    id = "-1",
    screenName = screenName,
    source = screenName,
    delay = DEFAULT_DELAY,
    minAppBuildNumber = Int.MIN_VALUE,
    maxAppBuildNumber = Int.MAX_VALUE,
    priority = DEFAULT_PRIORITY,
    expiryTime = 0,
    displayStatus = DisplayStatus.TO_BE_DISPLAYED,
    name = "Local Notification",
    targetId = targetId,
    targetIdType = TargetIdType.ID,
    targetIndex = DEFAULT_TARGET_INDEX,
    title = title,
    subtitle = subtitle,
    radius = radius,
    padding = padding
) {
    companion object {
        const val KIND = "tap_target_local"
    }
}
