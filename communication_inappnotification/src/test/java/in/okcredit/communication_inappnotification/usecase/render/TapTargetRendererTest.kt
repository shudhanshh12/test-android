package `in`.okcredit.communication_inappnotification.usecase.render

import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.contract.ui.remote.Tooltip
import `in`.okcredit.communication_inappnotification.usecase.TargetViewFinder
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.coroutines.DispatcherProvider
import java.lang.ref.WeakReference

class TapTargetRendererTest {

    private val dispatcherProvider: DispatcherProvider = mock()
    private val targetViewFinder: TargetViewFinder = mock()
    private val tracker: InAppNotificationTracker = mock()
    private val tapTargetRenderer = TapTargetRenderer(
        { dispatcherProvider },
        { targetViewFinder },
        { tracker }
    )

    @Before
    fun setUp() {
        whenever(dispatcherProvider.io()).thenReturn(Dispatchers.Unconfined)
    }

    @Test
    fun `given notification type is not TapTarget should call track error`() = runBlocking {
        // Given
        val id = "notification-id"
        val kind = "kind"
        val screenName = "HomeFragment"
        val name = "name"

        val weakScreen = mock<WeakReference<FragmentActivity>>()
        val weakView = mock<WeakReference<View>>()
        val notification = mock<Tooltip>().apply {
            whenever(this.id).thenReturn(id)
            whenever(this.kind).thenReturn(kind)
            whenever(this.getTypeForAnalyticsTracking()).thenReturn(kind)
            whenever(this.screenName).thenReturn(screenName)
            whenever(this.name).thenReturn(name)
        }

        // When
        tapTargetRenderer.renderRemoteNotification(weakScreen = weakScreen, weakView = weakView, notification = notification)

        // Then
        val exceptionCaptor = argumentCaptor<Exception>()
        verify(tracker).trackNotificationDisplayError(
            exception = exceptionCaptor.capture(),
            notificationId = eq(id),
            targetIdType = anyOrNull(),
            targetId = anyOrNull(),
            type = eq(kind),
            screenName = eq(screenName),
            name = eq(name)
        )
        Truth.assertThat(exceptionCaptor.firstValue).isInstanceOf(IllegalArgumentException::class.java)
        Truth.assertThat(exceptionCaptor.firstValue.message).isEqualTo("Expected notification type is TapTarget")
    }
}
