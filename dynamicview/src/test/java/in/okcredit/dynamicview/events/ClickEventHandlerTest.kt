package `in`.okcredit.dynamicview.events

import `in`.okcredit.dynamicview.component.menu.MenuComponentModel
import `in`.okcredit.dynamicview.data.model.Action
import `in`.okcredit.shared.deeplink.InternalDeeplinkNavigationDelegator
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test

class ClickEventHandlerTest {

    private val tracker: DynamicViewEventTracker = mock()
    private val deeplinkNavigator: InternalDeeplinkNavigationDelegator = mock()
    private val clickEventHandler = ClickEventHandler(tracker, deeplinkNavigator)

    @Test
    fun `should call track with with given action for track action`() {
        // Given
        val trackAction1 = Action.Track("Side Menu Interacted")
        val trackAction2 = Action.Track("Referral v2 Clicked")
        val component = MenuComponentModel(
            "v1-alpha",
            "menu_item",
            null,
            mapOf(
                "click" to setOf<Action>(
                    trackAction1, trackAction2
                )
            ),
            "Referral V2",
            "https://pbs.twimg.com/profile_images/724435763287326720/7Ntlvkey_400x400.jpg"
        )

        // When
        clickEventHandler.onClick(component)

        // Then
        verify(tracker, times(1)).track(trackAction1)
        verify(tracker, times(1)).track(trackAction2)
        verifyNoMoreInteractions(tracker)
    }

    @Test
    fun `should call execute deeplink with url for navigate action`() {
        // Given
        val component = MenuComponentModel(
            "v1-alpha",
            "menu_item",
            null,
            mapOf(
                "click" to setOf<Action>(
                    Action.Navigate("https://google.co.in"),
                    Action.Navigate("https://okcredit.in")
                )
            ),
            "WhatsApp us",
            "https://pbs.twimg.com/profile_images/724435763287326720/7Ntlvkey_400x400.jpg"
        )

        // When
        clickEventHandler.onClick(component)

        // Then
        verify(deeplinkNavigator, times(1)).executeDeeplink("https://google.co.in")
        verify(deeplinkNavigator, times(1)).executeDeeplink("https://okcredit.in")
        verifyNoMoreInteractions(deeplinkNavigator)
    }
}
