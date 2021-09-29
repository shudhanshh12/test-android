package `in`.okcredit.communication_inappnotification.contract

import `in`.okcredit.communication_inappnotification.contract.ui.local.TapTargetLocal
import `in`.okcredit.communication_inappnotification.contract.ui.local.TooltipLocal
import androidx.fragment.app.FragmentActivity
import com.skydoves.balloon.Balloon
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.lang.ref.WeakReference

/**
 * Responsible to handle the rendering of in-app notification
 * @see TapTargetLocal
 * @see TooltipLocal
 */
abstract class LocalInAppNotificationHandler {

    suspend fun generateTapTarget(
        weakScreen: WeakReference<FragmentActivity>,
        tapTarget: TapTargetLocal,
    ): MaterialTapTargetPrompt? {
        ensureLifecycleAware(weakScreen)
        return generateTapTargetDelegate(weakScreen, tapTarget)
    }

    protected abstract suspend fun generateTapTargetDelegate(
        weakScreen: WeakReference<FragmentActivity>,
        tapTarget: TapTargetLocal,
    ): MaterialTapTargetPrompt?

    protected abstract fun ensureLifecycleAware(
        weakScreen: WeakReference<FragmentActivity>,
    )

    suspend fun generateTooltip(
        weakScreen: WeakReference<FragmentActivity>,
        tooltip: TooltipLocal,
    ): Balloon? {
        ensureLifecycleAware(weakScreen)
        return generateTooltipDelegate(weakScreen, tooltip)
    }

    protected abstract suspend fun generateTooltipDelegate(
        weakScreen: WeakReference<FragmentActivity>,
        tooltip: TooltipLocal,
    ): Balloon?
}
