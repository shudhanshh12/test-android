package `in`.okcredit.communication_inappnotification.usecase.render

import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.usecase.builder.InAppNotificationUiBuilder
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

interface LocalInAppNotificationRenderer {
    suspend fun renderLocalNotification(
        weakScreen: WeakReference<FragmentActivity>,
        notification: InAppNotification,
    ): InAppNotificationUiBuilder?
}
