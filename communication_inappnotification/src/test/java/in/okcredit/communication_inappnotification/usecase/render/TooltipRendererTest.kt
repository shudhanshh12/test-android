package `in`.okcredit.communication_inappnotification.usecase.render

import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.contract.ui.remote.TapTarget
import `in`.okcredit.communication_inappnotification.usecase.TargetViewFinder
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.coroutines.DispatcherProvider
import java.lang.ref.WeakReference

class TooltipRendererTest {

    private val dispatcherProvider: DispatcherProvider = mock()
    private val targetViewFinder: TargetViewFinder = mock()
    private val tracker: InAppNotificationTracker = mock()
    private val tooltipRenderer = TooltipRenderer(
        { dispatcherProvider },
        { targetViewFinder },
        { tracker }
    )

    @Before
    fun setUp() {
        whenever(dispatcherProvider.io()).thenReturn(Dispatchers.Unconfined)
    }

    @Test
    fun `given notification type is not Tooltip should call track error`() = runBlocking {
        // Given
        val id = "notification-id"
        val kind = "kind"
        val screenName = "HomeFragment"
        val name = "name"

        val weakScreen = mock<WeakReference<FragmentActivity>>()
        val weakView = mock<WeakReference<View>>()
        val notification = mock<TapTarget>().apply {
            whenever(this.id).thenReturn(id)
            whenever(this.kind).thenReturn(kind)
            whenever(this.getTypeForAnalyticsTracking()).thenReturn(kind)
            whenever(this.screenName).thenReturn(screenName)
            whenever(this.name).thenReturn(name)
        }

        // When
        tooltipRenderer.renderRemoteNotification(weakScreen = weakScreen, weakView = weakView, notification = notification)

        // Then
        val exceptionCaptor = argumentCaptor<Exception>()
        verify(tracker).trackNotificationDisplayError(
            exception = exceptionCaptor.capture(),
            notificationId = eq(id),
            targetIdType = anyOrNull(),
            targetId = anyOrNull(),
            type = eq(kind),
            screenName = eq(screenName),
            name = any()
        )
        assertThat(exceptionCaptor.firstValue).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exceptionCaptor.firstValue.message).isEqualTo("Expected notification type is Tooltip")
    }
}
