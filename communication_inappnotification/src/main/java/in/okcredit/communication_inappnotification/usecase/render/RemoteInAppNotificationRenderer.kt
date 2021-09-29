package `in`.okcredit.communication_inappnotification.usecase.render

import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.exception.ScreenNotResumedException
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import java.lang.ref.WeakReference

interface RemoteInAppNotificationRenderer {
    suspend fun renderRemoteNotification(
        weakScreen: WeakReference<FragmentActivity>,
        weakView: WeakReference<View>,
        notification: InAppNotification,
    ): DisplayStatus

    private fun ensureScreenIsResumed(weakScreen: WeakReference<FragmentActivity>) =
        weakScreen.get()!!.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

    fun ensureCanShowNotification(weakScreen: WeakReference<FragmentActivity>, scope: CoroutineScope) {
        scope.ensureActive()
        if (ensureScreenIsResumed(weakScreen).not()) throw ScreenNotResumedException
    }
}
