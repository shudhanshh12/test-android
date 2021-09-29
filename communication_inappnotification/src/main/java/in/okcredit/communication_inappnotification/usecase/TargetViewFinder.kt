package `in`.okcredit.communication_inappnotification.usecase

import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.contract.TargetIdType
import `in`.okcredit.communication_inappnotification.contract.TargetIdType.*
import `in`.okcredit.communication_inappnotification.contract.ui.remote.TapTarget
import `in`.okcredit.communication_inappnotification.contract.ui.remote.Tooltip
import `in`.okcredit.communication_inappnotification.exception.TargetViewNotFoundException
import android.view.View
import android.view.View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION
import android.view.View.FIND_VIEWS_WITH_TEXT
import dagger.Lazy
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.extensions.isVisible
import java.lang.ref.WeakReference
import javax.inject.Inject

class TargetViewFinder @Inject constructor(
    private val dispatcherProvider: Lazy<DispatcherProvider>,
    private val tracker: Lazy<InAppNotificationTracker>,
) {
    companion object {
        const val ID_DEF_TYPE = "id"
    }

    private lateinit var targetIdType: TargetIdType
    private lateinit var targetId: String
    private var targetIndex: Int = 0

    suspend fun execute(
        weakView: WeakReference<View>,
        notification: InAppNotification,
    ): WeakReference<View?> {
        when (notification) {
            is Tooltip -> {
                targetIdType = notification.targetIdType
                targetId = notification.targetId
                targetIndex = notification.targetIndex
            }
            is TapTarget -> {
                targetIdType = notification.targetIdType
                targetId = notification.targetId
                targetIndex = notification.targetIndex
            }
            else -> {
                trackIllegalArgument(notification)
                return WeakReference(null)
            }
        }

        val targetView = when (targetIdType) {
            CONTENT_DESCRIPTION -> findByContentDescription(targetId, weakView, targetIndex)
            TEXT -> findByText(targetId, weakView, targetIndex)
            ID -> findById(targetId, weakView)
            TAG -> findByTag(targetId, weakView)
        }
        return if (targetView != null && targetView.isVisible()) {
            WeakReference(targetView)
        } else {
            if (targetView == null) {
                trackNotificationNotDisplayed(notification, targetIdType, targetId, TargetViewNotFoundException)
            }
            WeakReference(null)
        }
    }

    private suspend fun findByContentDescription(
        targetId: String,
        weakView: WeakReference<View>,
        targetIndex: Int,
    ) = findByContentDescriptionOrText(targetId, weakView, targetIndex, FIND_VIEWS_WITH_CONTENT_DESCRIPTION)

    private suspend fun findByText(
        targetId: String,
        weakView: WeakReference<View>,
        targetIndex: Int,
    ) = findByContentDescriptionOrText(targetId, weakView, targetIndex, FIND_VIEWS_WITH_TEXT)

    private suspend fun findByContentDescriptionOrText(
        targetId: String,
        weakView: WeakReference<View>,
        targetIndex: Int,
        findBy: Int,
    ): View? {
        val viewList = arrayListOf<View>()
        withContext(dispatcherProvider.get().main()) {
            weakView.get()?.findViewsWithText(viewList, targetId, findBy)
        }
        return if (targetIndex in viewList.indices) {
            viewList.getOrNull(targetIndex)
        } else {
            if (targetIndex > viewList.size) viewList.lastOrNull()
            else viewList.firstOrNull()
        }
    }

    private suspend fun findById(
        targetId: String,
        weakView: WeakReference<View>
    ): View? {
        return withContext(dispatcherProvider.get().main()) {
            val id =
                weakView.get()?.resources?.getIdentifier(targetId, ID_DEF_TYPE, weakView.get()?.context?.packageName)
            return@withContext id?.let { weakView.get()?.findViewById<View>(id) }
        }
    }

    private suspend fun findByTag(targetId: String, weakView: WeakReference<View>): View? {
        return withContext(dispatcherProvider.get().main()) {
            weakView.get()?.findViewWithTag(targetId)
        }
    }

    private fun trackIllegalArgument(notification: InAppNotification) {
        val message = "Unsupported notification type: ${notification.getTypeForAnalyticsTracking()}"
        val exception = IllegalArgumentException(message)
        tracker.get().trackException(exception)
    }

    private suspend fun trackNotificationNotDisplayed(
        notification: InAppNotification,
        targetIdType: TargetIdType,
        targetId: String,
        exception: Exception,
    ) = withContext(dispatcherProvider.get().io()) {
        tracker.get().trackNotificationDisplayError(
            exception = exception,
            notificationId = notification.id,
            targetIdType = targetIdType.name,
            targetId = targetId,
            type = notification.getTypeForAnalyticsTracking(),
            screenName = notification.screenName,
            name = notification.name
        )
    }
}
