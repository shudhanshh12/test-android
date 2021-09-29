package `in`.okcredit.communication_inappnotification.usecase.render

import `in`.okcredit.communication_inappnotification.R
import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.contract.ui.local.TooltipLocal
import `in`.okcredit.communication_inappnotification.contract.ui.remote.Tooltip
import `in`.okcredit.communication_inappnotification.usecase.TargetViewFinder
import `in`.okcredit.communication_inappnotification.usecase.builder.TooltipBuilder
import `in`.okcredit.shared.utils.TimeUtils.toMillis
import android.view.View
import androidx.annotation.UiThread
import androidx.fragment.app.FragmentActivity
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.coroutines.DispatcherProvider
import java.lang.ref.WeakReference
import javax.inject.Inject

class TooltipRenderer @Inject constructor(
    private val dispatcherProvider: Lazy<DispatcherProvider>,
    private val targetViewFinder: Lazy<TargetViewFinder>,
    private val tracker: Lazy<InAppNotificationTracker>,
) : RemoteInAppNotificationRenderer, LocalInAppNotificationRenderer {
    companion object {
        const val REMOTE_NAME = Tooltip.KIND
        const val LOCAL_NAME = TooltipLocal.KIND
    }

    override suspend fun renderRemoteNotification(
        weakScreen: WeakReference<FragmentActivity>,
        weakView: WeakReference<View>,
        notification: InAppNotification,
    ): DisplayStatus = withContext(dispatcherProvider.get().io()) outer@{
        val tooltip = notification as? Tooltip ?: return@outer notificationNotDisplayed(notification)
        delay(tooltip.delay.toMillis())
        val targetView = targetViewFinder.get().execute(weakView, tooltip)
        return@outer withContext(dispatcherProvider.get().main()) inner@{
            targetView.get()?.let {
                try {
                    ensureCanShowNotification(weakScreen, this@outer)
                    renderRemoteTooltipNotification(weakScreen, WeakReference(it), tooltip)
                    trackNotificationDisplayed(notification)
                    return@inner DisplayStatus.DISPLAYED
                } catch (ex: Exception) {
                    return@inner tooltipNotificationNotDisplayed(tooltip, ex)
                }
            } ?: return@inner DisplayStatus.NOT_DISPLAYED
        }
    }

    override suspend fun renderLocalNotification(
        weakScreen: WeakReference<FragmentActivity>,
        notification: InAppNotification,
    ): TooltipBuilder? {
        val tooltip = notification as? TooltipLocal ?: return null
        val builder = renderLocalTooltipNotification(weakScreen, tooltip.targetView, tooltip)
        return withContext(Dispatchers.Main) { builder.show() }
    }

    private fun renderLocalTooltipNotification(
        weakScreen: WeakReference<FragmentActivity>,
        targetView: WeakReference<View>,
        tooltipNotification: TooltipLocal,
    ): TooltipBuilder {
        val builder = TooltipBuilder(weakScreen, targetView)
            .setPrimaryText(tooltipNotification.title)
            .setPrimaryTextSize(tooltipNotification.textSize)
            .setBackgroundColour(tooltipNotification.backgroundColor)
            .setRadius(tooltipNotification.cornerRadius)
            .setPadding(tooltipNotification.padding.toFloat())
            .setPrimaryTextGravity(tooltipNotification.textGravity)
            .setArrowOrientation(tooltipNotification.arrowOrientation)
            .setArrowPosition(tooltipNotification.arrowPosition)
            .setArrowSize(tooltipNotification.arrowSize)
            .setWidthRatio(tooltipNotification.widthRatio)
            .setMarginRight(tooltipNotification.marginRight)
            .setMarginLeft(tooltipNotification.marginLeft)
            .setAlpha(tooltipNotification.alpha)
            .setTextColor(tooltipNotification.textColor)
            .enableDismissOnClicked(tooltipNotification.dismissOnClicked)
            .enableDismissWhenClickedOutside(tooltipNotification.dismissWhenClickedOutside)
            .setAnimation(tooltipNotification.animation)
            .setOnClickListener(tooltipNotification.clickListener)
            .setOnDismissListener(tooltipNotification.dismissListener)
            .setOnTouchOutsideListener(tooltipNotification.outsideTouchListener)
            .setAutoDismissTime(tooltipNotification.autoDismissTime)
            .setAlignTop(tooltipNotification.alignTop)

        tooltipNotification.textTypeFace?.let {
            builder.setPrimaryTextTypeFace(it)
        }
        return builder
    }

    @UiThread
    private fun renderRemoteTooltipNotification(
        weakScreen: WeakReference<FragmentActivity>,
        targetView: WeakReference<View>,
        tooltipNotification: Tooltip,
    ) {

        TooltipBuilder(weakScreen, targetView)
            .setPrimaryText(tooltipNotification.title)
            .setBackgroundColour(R.color.indigo_1)
            .setArrowOrientation(tooltipNotification.arrowOrientation)
            .setArrowPosition(tooltipNotification.arrowPosition)
            .setTextColor(R.color.white)
            .enableDismissOnClicked(true)
            .setOnDismissListener {
                trackNotificationCleared(tooltipNotification)
            }
            .enableFreeAlignment(true)
            .show()
    }

    private suspend fun tooltipNotificationNotDisplayed(notification: Tooltip, exception: Exception): DisplayStatus {
        return withContext(dispatcherProvider.get().io()) {
            tracker.get().trackNotificationDisplayError(
                exception = exception,
                notificationId = notification.id,
                targetIdType = notification.targetIdType.name,
                targetId = notification.targetId,
                type = notification.getTypeForAnalyticsTracking(),
                screenName = notification.screenName,
                name = notification.name
            )
            return@withContext DisplayStatus.NOT_DISPLAYED
        }
    }

    private suspend fun notificationNotDisplayed(notification: InAppNotification) =
        withContext(dispatcherProvider.get().io()) {
            tracker.get().trackNotificationDisplayError(
                exception = IllegalArgumentException("Expected notification type is Tooltip"),
                notificationId = notification.id,
                type = notification.getTypeForAnalyticsTracking(),
                screenName = notification.screenName,
                name = notification.name
            )
            return@withContext DisplayStatus.NOT_DISPLAYED
        }

    private suspend fun trackNotificationDisplayed(notification: Tooltip) =
        withContext(dispatcherProvider.get().io()) {
            tracker.get().trackNotificationDisplayed(
                type = notification.getTypeForAnalyticsTracking(),
                id = notification.id,
                name = notification.name,
                source = notification.source
            )
        }

    private fun trackNotificationCleared(notification: Tooltip) {
        tracker.get().trackNotificationCleared(
            type = notification.getTypeForAnalyticsTracking(),
            id = notification.id,
            name = notification.name,
            source = notification.source
        )
    }
}
