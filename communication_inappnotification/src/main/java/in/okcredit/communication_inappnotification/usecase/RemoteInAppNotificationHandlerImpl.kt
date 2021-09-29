package `in`.okcredit.communication_inappnotification.usecase

import `in`.okcredit.communication_inappnotification.BuildConfig
import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationHandler.Companion.NOTIFICATION_COUNT_PER_DAY
import `in`.okcredit.communication_inappnotification.exception.RendererNotFoundException
import `in`.okcredit.communication_inappnotification.local.InAppNotificationLocalSource
import `in`.okcredit.communication_inappnotification.local.InAppNotificationPreferences
import `in`.okcredit.communication_inappnotification.usecase.render.RemoteInAppNotificationRenderer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.utils.CommonUtils
import `in`.okcredit.shared.utils.TimeUtils.toSeconds
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.datetime.epoch
import tech.okcredit.android.base.preferences.Scope
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

class RemoteInAppNotificationHandlerImpl @Inject constructor(
    private val dispatcherProvider: Lazy<DispatcherProvider>,
    private val localSource: Lazy<InAppNotificationLocalSource>,
    private val preferences: Lazy<InAppNotificationPreferences>,
    private val renderers: Lazy<Map<String, @JvmSuppressWildcards RemoteInAppNotificationRenderer>>,
    private val tracker: Lazy<InAppNotificationTracker>,
    private val displayStatusUpdater: Lazy<DisplayStatusUpdater>,
    firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : InAppNotificationHandler {

    private val notificationCountAllowedPerDay = firebaseRemoteConfig.get().getLong(NOTIFICATION_COUNT_PER_DAY)

    override suspend fun execute(
        screenName: String,
        weakScreen: WeakReference<FragmentActivity>,
        weakView: WeakReference<View>,
    ) {
        withContext(dispatcherProvider.get().io()) {
            val businessId = getActiveBusinessId.get().execute().await()
            val notificationList = localSource.get().getNotificationsNotDisplayedForScreen(screenName, businessId)
            if (notificationList.isNotEmpty() && canShowNotification(businessId)) {
                val filteredList = filterByConstraints(notificationList, businessId)
                val notification = getNotificationWithHighestPriority(filteredList)
                notification?.let {
                    val renderer = renderers.get()[notification.kind]
                    if (renderer != null) {
                        val displayStatus = renderer.renderRemoteNotification(weakScreen, weakView, notification)
                        displayStatusUpdater.get().execute(notification.id, displayStatus, businessId)
                    } else {
                        trackRendererNotFound(notification)
                    }
                }
            }
        }
    }

    private suspend fun canShowNotification(businessId: String): Boolean {
        val date = preferences.get().getLong(InAppNotificationPreferences.Keys.PREF_BUSINESS_DATE, Scope.Business(businessId)).first()
        val currentDayStartTime = CommonUtils.currentDateTime().withTimeAtStartOfDay().epoch

        return if (date == currentDayStartTime) {
            val notificationShownForDateCount =
                preferences.get().getInt(InAppNotificationPreferences.Keys.PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT, Scope.Business(businessId))
                    .first()
            val canShowNotification = notificationShownForDateCount < notificationCountAllowedPerDay
            if (canShowNotification.not()) {
                Timber.d("InAppNotificationHandlerImpl: Daily notification limit (= $notificationCountAllowedPerDay) exhausted.")
            }
            return canShowNotification
        } else {
            true
        }
    }

    private suspend fun filterByConstraints(
        notificationList: List<InAppNotification>,
        businessId: String,
    ): List<InAppNotification> {
        return notificationList.filter { notification ->
            val appVersion = getAppBuildNumber()
            if (appVersion != null &&
                (notification.minAppBuildNumber > appVersion || notification.maxAppBuildNumber < appVersion)
            ) {
                markNotificationNotDisplayed(
                    notification = notification,
                    reason = "AppVersion=$appVersion,(${notification.minAppBuildNumber},${notification.maxAppBuildNumber})",
                    businessId = businessId
                )
                return@filter false
            }
            if (notification.expiryTime < currentTime()) {
                markNotificationNotDisplayed(
                    notification = notification,
                    reason = "ExpiryTime=${notification.expiryTime}",
                    businessId = businessId
                )
                return@filter false
            }
            return@filter true
        }
    }

    private suspend fun markNotificationNotDisplayed(
        notification: InAppNotification,
        reason: String,
        businessId: String,
    ) {
        tracker.get().trackNotificationNotDisplayed(
            notificationId = notification.id,
            type = notification.getTypeForAnalyticsTracking(),
            screenName = notification.screenName,
            reason = reason,
            name = notification.name
        )
        displayStatusUpdater.get().execute(notification.id, DisplayStatus.NOT_DISPLAYED, businessId)
    }

    private fun getNotificationWithHighestPriority(notificationList: List<InAppNotification>): InAppNotification? {
        return notificationList.maxByOrNull { it.priority }
    }

    private fun currentTime() = System.currentTimeMillis().toSeconds()

    private fun getAppBuildNumber() = BuildConfig.VERSION_CODE.toIntOrNull()

    private fun trackRendererNotFound(notification: InAppNotification) {
        tracker.get().trackNotificationDisplayError(
            exception = RendererNotFoundException(notification.getTypeForAnalyticsTracking()),
            notificationId = notification.id,
            type = notification.getTypeForAnalyticsTracking(),
            screenName = notification.screenName,
            name = notification.name
        )
    }

    override fun getExceptionHandler() = CoroutineExceptionHandler { _, t -> tracker.get().trackException(t) }
}
