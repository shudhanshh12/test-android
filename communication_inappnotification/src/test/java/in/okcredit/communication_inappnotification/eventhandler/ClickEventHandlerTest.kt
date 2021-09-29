package `in`.okcredit.communication_inappnotification.eventhandler

import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.model.Action
import `in`.okcredit.communication_inappnotification.model.ActionButton
import `in`.okcredit.shared.deeplink.InternalDeeplinkNavigator
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import java.lang.ref.WeakReference

class ClickEventHandlerTest {

    private val tracker: InAppNotificationTracker = mock()
    private val internalDeeplinkNavigator: InternalDeeplinkNavigator = mock()
    private val clickEventHandler = ClickEventHandler({ tracker }, { internalDeeplinkNavigator })

    @Test
    fun `when actions contains track should track on tracker`() {
        // given
        val event = "event-name"
        val properties = mapOf("key-1" to "val-1", "key-2" to "val-2")
        val clickHandlers = setOf(Action.Track(event, properties))
        val actionButton = mock<ActionButton>().apply {
            whenever(this.clickHandlers).thenReturn(clickHandlers)
        }

        // when
        clickEventHandler.onClick(actionButton, mock())

        // then
        verify(tracker).track(event, properties)
    }

    @Test
    fun `when actions contains navigate should executeDeeplink on internalDeeplinkNavigator`() {
        // given
        val url = "https://url.com"
        val clickHandlers = setOf(Action.Navigate(url))
        val actionButton = mock<ActionButton>().apply {
            whenever(this.clickHandlers).thenReturn(clickHandlers)
        }
        val activity = mock<FragmentActivity>()
        val bottomSheetDialogFragment = mock<BottomSheetDialogFragment>().apply {
            whenever(this.requireActivity()).thenReturn(activity)
        }

        // when
        clickEventHandler.onClick(actionButton, WeakReference(bottomSheetDialogFragment))

        // then
        verify(internalDeeplinkNavigator).executeDeeplink(url, activity)
    }

    @Test
    fun `when actions contains dismiss should dismiss on bottomSheetDialogFragment`() {
        // given
        val clickHandlers = setOf(Action.Dismiss())
        val actionButton = mock<ActionButton>().apply {
            whenever(this.clickHandlers).thenReturn(clickHandlers)
        }
        val bottomSheetDialogFragment = mock<BottomSheetDialogFragment>()

        // when
        clickEventHandler.onClick(actionButton, WeakReference(bottomSheetDialogFragment))

        // then
        verify(bottomSheetDialogFragment).dismiss()
    }

    @Test
    fun `when actions contains all actions should perform all actions`() {
        // given
        val event = "event-name"
        val properties = mapOf("key-1" to "val-1", "key-2" to "val-2")
        val url = "https://url.com"
        val activity = mock<FragmentActivity>()
        val bottomSheetDialogFragment = mock<BottomSheetDialogFragment>().apply {
            whenever(this.requireActivity()).thenReturn(activity)
        }
        val clickHandlers = setOf(
            Action.Track(event, properties),
            Action.Dismiss(),
            Action.Navigate(url)
        )
        val actionButton = mock<ActionButton>().apply {
            whenever(this.clickHandlers).thenReturn(clickHandlers)
        }

        // when
        clickEventHandler.onClick(actionButton, WeakReference(bottomSheetDialogFragment))

        // then
        verify(tracker).track(event, properties)
        verify(internalDeeplinkNavigator).executeDeeplink(url, activity)
        verify(bottomSheetDialogFragment).dismiss()
    }
}
