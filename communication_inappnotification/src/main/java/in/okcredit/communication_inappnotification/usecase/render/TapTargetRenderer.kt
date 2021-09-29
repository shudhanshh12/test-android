package `in`.okcredit.communication_inappnotification.usecase.render

import `in`.okcredit.communication_inappnotification.R
import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.contract.ui.local.TapTargetLocal
import `in`.okcredit.communication_inappnotification.contract.ui.remote.TapTarget
import `in`.okcredit.communication_inappnotification.usecase.TargetViewFinder
import `in`.okcredit.communication_inappnotification.usecase.builder.TapTargetBuilder
import `in`.okcredit.shared.utils.TimeUtils.toMillis
import android.view.View
import androidx.annotation.UiThread
import androidx.fragment.app.FragmentActivity
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.coroutines.DispatcherProvider
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.*
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

class TapTargetRenderer @Inject constructor(
    private val dispatcherProvider: Lazy<DispatcherProvider>,
    private val targetViewFinder: Lazy<TargetViewFinder>,
    private val tracker: Lazy<InAppNotificationTracker>,
) : RemoteInAppNotificationRenderer, LocalInAppNotificationRenderer {
    companion object {
        const val REMOTE_NAME = TapTarget.KIND
        const val LOCAL_NAME = TapTargetLocal.KIND
    }

    override suspend fun renderRemoteNotification(
        weakScreen: WeakReference<FragmentActivity>,
        weakView: WeakReference<View>,
        notification: InAppNotification,
    ): DisplayStatus = withContext(dispatcherProvider.get().io()) outer@{
        val tapTarget = notification as? TapTarget ?: return@outer notificationNotDisplayed(notification)
        delay(tapTarget.delay.toMillis())
        val targetView = targetViewFinder.get().execute(weakView, tapTarget)
        return@outer withContext(dispatcherProvider.get().main()) inner@{
            targetView.get()?.let {
                try {
                    ensureCanShowNotification(weakScreen, this@outer)

                    renderRemoteTapTargetNotification(weakScreen, targetView, tapTarget)

                    trackNotificationDisplayed(notification)
                    return@inner DisplayStatus.DISPLAYED
                } catch (e: Exception) {
                    return@inner tapTargetNotificationNotDisplayed(tapTarget, e)
                }
            } ?: return@inner DisplayStatus.NOT_DISPLAYED
        }
    }

    override suspend fun renderLocalNotification(
        weakScreen: WeakReference<FragmentActivity>,
        notification: InAppNotification,
    ): TapTargetBuilder? {
        val tapTarget = notification as? TapTargetLocal ?: return null
        val builder = renderLocalTapTargetNotification(weakScreen, tapTarget.targetView, tapTarget)

        return withContext(Dispatchers.Main) {
            builder.show()
        }
    }

    private fun renderLocalTapTargetNotification(
        weakScreen: WeakReference<FragmentActivity>,
        targetView: WeakReference<View?>,
        tapTargetNotification: TapTargetLocal
    ): TapTargetBuilder {

        return TapTargetBuilder(weakScreen, targetView, tapTargetNotification.targetViewId)
            .setPrimaryText(tapTargetNotification.title)
            .setSecondaryText(tapTargetNotification.subtitle)
            .setRadius(tapTargetNotification.radius)
            .setPadding(tapTargetNotification.padding)
            .setPromptChangeListener(tapTargetNotification.listener)
            .setBackgroundColour(tapTargetNotification.backgroundColor)
            .enableCaptureTouchEventOutsidePrompt(tapTargetNotification.enableTouchEventOutsidePrompt)
            .enableBackButtonDismiss(tapTargetNotification.enableBackButtonDismiss)
            .setPrimaryTextTypeFace(tapTargetNotification.titleTypeFace, tapTargetNotification.titleTypeFaceStyle)
            .setSecondaryTextTypeFace(
                tapTargetNotification.subtitleTypeFace,
                tapTargetNotification.subtitleTypeFaceStyle
            )
            .setPrimaryTextSize(tapTargetNotification.titleTextSize)
            .setSecondaryTextSize(tapTargetNotification.subtitleTextSize)
            .setPrimaryTextColor(tapTargetNotification.titleTextColor)
            .setSecondaryTextColor(tapTargetNotification.subtitleTextColor)
            .setPromptBackground(tapTargetNotification.outerRadius)
            .setPrimaryTextGravity(tapTargetNotification.titleGravity)
            .setSecondaryTextGravity(tapTargetNotification.subtitleGravity)
            .setFocalPadding(tapTargetNotification.focalPadding)
            .setFocalRadius(tapTargetNotification.focalRadius)
    }

    @UiThread
    private fun renderRemoteTapTargetNotification(
        weakScreen: WeakReference<FragmentActivity>,
        targetView: WeakReference<View?>,
        tapTargetNotification: TapTarget
    ) {
        TapTargetBuilder(weakScreen, targetView)
            .setPrimaryText(tapTargetNotification.title)
            .setSecondaryText(tapTargetNotification.subtitle)
            .setRadius(tapTargetNotification.radius)
            .setPadding(tapTargetNotification.padding)
            .setPromptChangeListener { _, state ->
                onStateChanged(state, tapTargetNotification)
            }
            .enableCaptureTouchEventOutsidePrompt(true)
            .setBackgroundColour(R.color.primary)
            .show()
    }

    private fun onStateChanged(state: Int, notification: TapTarget) {
        when (state) {
            STATE_FOCAL_PRESSED -> trackNotificationClicked(notification)
            STATE_NON_FOCAL_PRESSED, STATE_BACK_BUTTON_PRESSED -> trackNotificationCleared(notification)
        }
    }

    private suspend fun tapTargetNotificationNotDisplayed(notification: TapTarget, exception: Exception) =
        withContext(dispatcherProvider.get().io()) {
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

    private suspend fun notificationNotDisplayed(notification: InAppNotification) =
        withContext(dispatcherProvider.get().io()) {
            tracker.get().trackNotificationDisplayError(
                exception = IllegalArgumentException("Expected notification type is TapTarget"),
                notificationId = notification.id,
                type = notification.getTypeForAnalyticsTracking(),
                screenName = notification.screenName,
                name = notification.name
            )
            return@withContext DisplayStatus.NOT_DISPLAYED
        }

    private suspend fun trackNotificationDisplayed(notification: TapTarget) =
        withContext(dispatcherProvider.get().io()) {
            tracker.get().trackNotificationDisplayed(
                type = notification.getTypeForAnalyticsTracking(),
                id = notification.id,
                name = notification.name,
                source = notification.source
            )
        }

    private fun trackNotificationCleared(notification: TapTarget) {
        tracker.get().trackNotificationCleared(
            type = notification.getTypeForAnalyticsTracking(),
            id = notification.id,
            name = notification.name,
            source = notification.source
        )
    }

    private fun trackNotificationClicked(notification: TapTarget) {
        tracker.get().trackNotificationClicked(
            type = notification.getTypeForAnalyticsTracking(),
            id = notification.id,
            name = notification.name,
            source = notification.source,
            value = "Focal Area"
        )
    }
}
