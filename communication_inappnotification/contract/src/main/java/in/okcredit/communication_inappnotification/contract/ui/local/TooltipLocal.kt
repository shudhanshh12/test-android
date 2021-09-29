package `in`.okcredit.communication_inappnotification.contract.ui.local

import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.TargetIdType
import `in`.okcredit.communication_inappnotification.contract.ui.remote.Tooltip
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.OnBalloonClickListener
import com.skydoves.balloon.OnBalloonDismissListener
import com.skydoves.balloon.OnBalloonOutsideTouchListener
import java.lang.ref.WeakReference

class TooltipLocal(
    screenName: String,
    title: String,
    targetId: String = "",
    arrowOrientation: ArrowOrientation = ArrowOrientation.BOTTOM,
    arrowPosition: Float = 0.5f,

    var targetView: WeakReference<View>,

    /**
     * Extra parameters for building UI locally
     */
    var arrowSize: Int = 8,
    var widthRatio: Float = 0.7f,
    var cornerRadius: Float = 8f,
    var marginRight: Int = 12,
    var marginLeft: Int = 12,
    var padding: Int = 8,
    var alpha: Float = 1f,
    var textColor: Int = android.R.color.white,
    var textSize: Float = 12f,
    var textTypeFace: Typeface? = null,
    var textGravity: Int = Gravity.CENTER,

    var backgroundColor: Int,
    var dismissOnClicked: Boolean = false,
    var dismissWhenClickedOutside: Boolean = true,
    var animation: BalloonAnimation = BalloonAnimation.FADE,
    var autoDismissTime: Long? = null,
    /**
     * Listeners to manage state change of prompt
     */
    var clickListener: OnBalloonClickListener? = null,
    var dismissListener: OnBalloonDismissListener? = null,
    var outsideTouchListener: OnBalloonOutsideTouchListener? = null,

    /**
     * if set to false, Tooltip will be automatically aligned to bottom
     */
    var alignTop: Boolean = true,
) : Tooltip(
    id = "-1",
    screenName = screenName,
    source = screenName,
    title = title,
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
    arrowOrientation = arrowOrientation,
    arrowPosition = arrowPosition
) {
    companion object {
        const val KIND = "tooltip_local"
    }
}
