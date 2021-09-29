package `in`.okcredit.communication_inappnotification.remote

import `in`.okcredit.communication_inappnotification.BuildConfig
import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.contract.InAppNotification.Companion.DEFAULT_DELAY
import `in`.okcredit.communication_inappnotification.contract.InAppNotification.Companion.DEFAULT_PRIORITY
import `in`.okcredit.communication_inappnotification.contract.InAppNotification.Companion.DEFAULT_TARGET_INDEX
import `in`.okcredit.communication_inappnotification.contract.TargetIdType.Companion.toTargetIdType
import `in`.okcredit.communication_inappnotification.contract.ui.remote.TapTarget
import `in`.okcredit.communication_inappnotification.contract.ui.remote.TapTarget.Companion.DEFAULT_PADDING
import `in`.okcredit.communication_inappnotification.contract.ui.remote.TapTarget.Companion.DEFAULT_RADIUS
import `in`.okcredit.communication_inappnotification.contract.ui.remote.Tooltip
import `in`.okcredit.communication_inappnotification.contract.ui.remote.Tooltip.Companion.DEFAULT_ARROW_ORIENTATION
import `in`.okcredit.communication_inappnotification.contract.ui.remote.Tooltip.Companion.DEFAULT_ARROW_POSITION
import `in`.okcredit.communication_inappnotification.contract.ui.remote.Tooltip.Companion.toArrowOrientation
import `in`.okcredit.communication_inappnotification.model.EducationSheet
import `in`.okcredit.communication_inappnotification.model.EducationSheet.Companion.DEFAULT_IMAGE_SIZE
import `in`.okcredit.communication_inappnotification.model.EducationSheet.Companion.IMAGE_WIDTH_MATCH_PARENT
import `in`.okcredit.merchant.device.DeviceRepository
import dagger.Lazy
import kotlinx.coroutines.rx2.awaitFirst
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.language.LocaleManager
import javax.inject.Inject
import `in`.okcredit.communication_inappnotification.model.Action as DomainAction
import `in`.okcredit.communication_inappnotification.model.ActionButton as DomainActionButton

class InAppNotificationRemoteSourceImpl @Inject constructor(
    private val apiClient: Lazy<InAppNotificationApiClient>,
    private val localeManager: Lazy<LocaleManager>,
    private val deviceRepository: Lazy<DeviceRepository>,
) : InAppNotificationRemoteSource {

    override suspend fun getNotifications(businessId: String): List<InAppNotification> {
        val deviceId = deviceRepository.get().getDevice().awaitFirst().id
        val lang = localeManager.get().getLanguage()
        val appBuildNumber = BuildConfig.VERSION_CODE
        val request = GetInAppNotificationsRequest(deviceId, lang, appBuildNumber)
        val response = apiClient.get().getNotifications(request, businessId)
        return response.inAppNotifications.mapNotNull { it.toNotification() }
    }

    override suspend fun acknowledgeNotifications(ids: List<String>, businessId: String): Boolean {
        val deviceId = deviceRepository.get().getDevice().awaitFirst().id
        val request = AckNotificationsRequest(ids, deviceId)
        val response = apiClient.get().acknowledgeNotifications(request, businessId)
        return response.acknowledged
    }

    private fun Notification.toNotification(): InAppNotification? {
        return try {
            when (notificationData[0].kind) {
                Tooltip.KIND -> buildTooltipNotification(this)
                TapTarget.KIND -> buildTapTargetNotification(this)
                EducationSheet.KIND -> buildEducationSheetNotification(this)
                else -> null
            }
        } catch (e: Exception) { // catch non-nullable field is received null from API
            RecordException.recordException(e)
            null
        }
    }

    private fun buildTooltipNotification(notification: Notification): InAppNotification {
        val notificationData = notification.notificationData[0]
        val arrowPosition = with(notificationData.arrowPosition) {
            return@with if (this != null && this <= 1f && this >= 0f) this else DEFAULT_ARROW_POSITION
        }
        return Tooltip(
            id = notification.id,
            screenName = notificationData.screenName,
            delay = notificationData.delay ?: DEFAULT_DELAY,
            minAppBuildNumber = notification.minAppVersion,
            maxAppBuildNumber = notification.maxAppVersion,
            priority = notificationData.priority ?: DEFAULT_PRIORITY,
            expiryTime = notification.expiry_time,
            displayStatus = DisplayStatus.TO_BE_DISPLAYED,
            name = notification.name,
            source = notification.source,
            title = notificationData.title!!,
            targetIdType = notificationData.targetIdType!!.toTargetIdType(),
            targetId = notificationData.targetId!!,
            targetIndex = notificationData.targetIndex ?: DEFAULT_TARGET_INDEX,
            arrowPosition = arrowPosition,
            arrowOrientation = notificationData.arrowOrientation?.toArrowOrientation() ?: DEFAULT_ARROW_ORIENTATION
        )
    }

    private fun buildTapTargetNotification(notification: Notification): InAppNotification {
        val notificationData = notification.notificationData[0]
        val radius = with(notificationData.radius?.toFloat()) {
            return@with if (this != null && this >= 0f && this <= 500f) this else DEFAULT_RADIUS
        }
        val padding = with(notificationData.padding?.toFloat()) {
            return@with if (this != null && this >= 0f && this <= 500f) this else DEFAULT_PADDING
        }
        return TapTarget(
            id = notification.id,
            screenName = notificationData.screenName,
            delay = notificationData.delay ?: DEFAULT_DELAY,
            minAppBuildNumber = notification.minAppVersion,
            maxAppBuildNumber = notification.maxAppVersion,
            priority = notificationData.priority ?: DEFAULT_PRIORITY,
            expiryTime = notification.expiry_time,
            displayStatus = DisplayStatus.TO_BE_DISPLAYED,
            name = notification.name,
            source = notification.source,
            title = notificationData.title!!,
            subtitle = notificationData.subtitle!!,
            targetIdType = notificationData.targetIdType!!.toTargetIdType(),
            targetId = notificationData.targetId!!,
            targetIndex = notificationData.targetIndex ?: DEFAULT_TARGET_INDEX,
            radius = radius,
            padding = padding
        )
    }

    private fun buildEducationSheetNotification(notification: Notification): InAppNotification {
        val notificationData = notification.notificationData[0]
        val width = with(notificationData.imageWidth) {
            return@with if (this != null && this >= IMAGE_WIDTH_MATCH_PARENT && this <= 500f) this else DEFAULT_IMAGE_SIZE
        }
        val height = with(notificationData.imageHeight) {
            return@with if (this != null && this >= 0f && this <= 500f) this else DEFAULT_IMAGE_SIZE
        }
        return EducationSheet(
            id = notification.id,
            screenName = notificationData.screenName,
            delay = notificationData.delay ?: DEFAULT_DELAY,
            minAppBuildNumber = notification.minAppVersion,
            maxAppBuildNumber = notification.maxAppVersion,
            priority = notificationData.priority ?: DEFAULT_PRIORITY,
            expiryTime = notification.expiry_time,
            displayStatus = DisplayStatus.TO_BE_DISPLAYED,
            name = notification.name,
            source = notification.source,
            template = notificationData.template!!,
            imageWidth = width,
            imageHeight = height,
            imageUrl = notificationData.imageUrl,
            title = notificationData.title,
            subtitle = notificationData.subtitle,
            primaryBtn = notificationData.primaryButton?.toActionButton(),
            secondaryBtn = notificationData.secondaryButton?.toActionButton(),
            tertiaryBtn = notificationData.tertiaryButton?.toActionButton()
        )
    }

    private fun ActionButton.toActionButton(): DomainActionButton {
        return DomainActionButton(
            text = this.text,
            iconUrl = this.iconUrl,
            clickHandlers = this.eventHandlers?.clickHandlers?.mapNotNull { it.toActionButton() }?.toSet()
        )
    }

    private fun Action.toActionButton(): DomainAction? {
        return when (action) {
            DomainAction.Track.ACTION -> DomainAction.Track(event = this.event!!, properties = this.properties!!)
            DomainAction.Navigate.ACTION -> DomainAction.Navigate(url = this.url!!)
            DomainAction.Dismiss.ACTION -> DomainAction.Dismiss()
            else -> null
        }
    }
}
