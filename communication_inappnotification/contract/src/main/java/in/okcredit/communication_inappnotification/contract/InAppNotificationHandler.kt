package `in`.okcredit.communication_inappnotification.contract

import android.view.View
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import java.lang.ref.WeakReference

interface InAppNotificationHandler {

    companion object {
        const val NOTIFICATION_COUNT_PER_DAY = "in_app_notification_count_per_day"
    }

    suspend fun execute(
        screenName: String,
        weakScreen: WeakReference<FragmentActivity>,
        weakView: WeakReference<View>,
    )

    fun getExceptionHandler(): CoroutineExceptionHandler
}
