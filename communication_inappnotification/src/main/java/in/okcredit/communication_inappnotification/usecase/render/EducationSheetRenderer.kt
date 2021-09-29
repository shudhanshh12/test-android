package `in`.okcredit.communication_inappnotification.usecase.render

import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.model.EducationSheet
import `in`.okcredit.communication_inappnotification.ui.education_sheet.BaseEducationBottomSheet
import `in`.okcredit.communication_inappnotification.ui.education_sheet.StandardEducationBottomSheet
import `in`.okcredit.communication_inappnotification.ui.education_sheet.TertiaryEducationBottomSheet
import `in`.okcredit.shared.utils.TimeUtils.toMillis
import android.view.View
import androidx.fragment.app.FragmentActivity
import dagger.Lazy
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.coroutines.DispatcherProvider
import java.lang.ref.WeakReference
import javax.inject.Inject
import `in`.okcredit.communication_inappnotification.ui.education_sheet.StandardEducationBottomSheet.Companion.TEMPLATE_NAME as STANDARD_TEMPLATE
import `in`.okcredit.communication_inappnotification.ui.education_sheet.TertiaryEducationBottomSheet.Companion.TEMPLATE_NAME as TERTIARY_TEMPLATE

class EducationSheetRenderer @Inject constructor(
    private val dispatcherProvider: Lazy<DispatcherProvider>,
    private val tracker: Lazy<InAppNotificationTracker>
) : RemoteInAppNotificationRenderer {
    companion object {
        const val NAME = EducationSheet.KIND
    }

    override suspend fun renderRemoteNotification(
        weakScreen: WeakReference<FragmentActivity>,
        weakView: WeakReference<View>,
        notification: InAppNotification
    ): DisplayStatus = withContext(dispatcherProvider.get().io()) outer@{
        val educationSheet = notification as? EducationSheet ?: return@outer notificationNotDisplayed(notification)
        delay(educationSheet.delay.toMillis())
        return@outer withContext(dispatcherProvider.get().main()) inner@{
            val bottomSheetDialogFragment = getBottomSheetDialogFragmentForTemplate(educationSheet)
            try {
                ensureCanShowNotification(weakScreen, this@outer)
                bottomSheetDialogFragment.show(
                    weakScreen.get()!!.supportFragmentManager,
                    bottomSheetDialogFragment.TAG
                )
                trackNotificationDisplayed(notification)
                return@inner DisplayStatus.DISPLAYED
            } catch (e: Exception) {
                return@inner tapTargetNotificationNotDisplayed(educationSheet, e)
            }
        }
    }

    private fun getBottomSheetDialogFragmentForTemplate(educationSheet: EducationSheet): BaseEducationBottomSheet {
        return when (educationSheet.template) {
            STANDARD_TEMPLATE -> StandardEducationBottomSheet(educationSheet)
            TERTIARY_TEMPLATE -> TertiaryEducationBottomSheet(educationSheet)
            else -> throw IllegalArgumentException("Unsupported EducationSheet template : ${educationSheet.template}")
        }
    }

    private suspend fun notificationNotDisplayed(notification: InAppNotification) =
        withContext(dispatcherProvider.get().io()) {
            val message = "Expected notification type is EducationSheet"
            tracker.get().trackNotificationDisplayError(
                exception = IllegalArgumentException(message),
                notificationId = notification.id,
                type = notification.getTypeForAnalyticsTracking(),
                screenName = notification.screenName,
                name = notification.name
            )
            return@withContext DisplayStatus.NOT_DISPLAYED
        }

    private suspend fun trackNotificationDisplayed(notification: EducationSheet) =
        withContext(dispatcherProvider.get().io()) {
            tracker.get().trackNotificationDisplayed(
                type = notification.getTypeForAnalyticsTracking(),
                id = notification.id,
                name = notification.name,
                source = notification.source
            )
        }

    private suspend fun tapTargetNotificationNotDisplayed(notification: EducationSheet, exception: Exception) =
        withContext(dispatcherProvider.get().io()) {
            tracker.get().trackNotificationDisplayError(
                exception = exception,
                notificationId = notification.id,
                type = notification.getTypeForAnalyticsTracking(),
                screenName = notification.screenName,
                name = notification.name
            )
            return@withContext DisplayStatus.NOT_DISPLAYED
        }
}
