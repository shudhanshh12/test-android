package `in`.okcredit.dynamicview.events

import `in`.okcredit.dynamicview.Environment
import `in`.okcredit.dynamicview.TargetSpec
import `in`.okcredit.dynamicview.component.menu.MenuComponentModel
import `in`.okcredit.dynamicview.data.model.Action
import `in`.okcredit.dynamicview.data.model.ComponentModel
import `in`.okcredit.dynamicview.data.model.withDefaultProperties
import com.airbnb.epoxy.VisibilityState
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test

class ViewEventHandlerTest {

    private val tracker: DynamicViewEventTracker = mock()
    private val viewEventHandler = ViewEventHandler(tracker)
    private val trackAction1 = Action.Track("Customization Viewed", mapOf("default key1" to "default value1"))
    private val trackAction2 = Action.Track("Side Menu Viewed", mapOf("default key2" to "default value2"))

    @Test
    fun `should call track for given track actions with default properties when visibility state is focused visible`() {
        // Given
        val component = getComponent()
        val targetSpec = TargetSpec(
            "target_name", setOf()
        )
        val environment = Environment(targetSpec, mock())

        // When
        viewEventHandler.onVisibilityStateChanged(VisibilityState.FOCUSED_VISIBLE, environment, component)

        // Then
        verify(tracker).track(trackAction1.withDefaultProperties("target_name", component))
        verify(tracker).track(trackAction2.withDefaultProperties("target_name", component))
    }

    @Test
    fun `should not call track for given track actions when visibility state is not focused visible`() {
        // Given
        val component = getComponent()
        val targetSpec = TargetSpec(
            "target_name", setOf()
        )
        val environment = Environment(targetSpec, mock())

        // When
        viewEventHandler.onVisibilityStateChanged(VisibilityState.INVISIBLE, environment, component)
        viewEventHandler.onVisibilityStateChanged(VisibilityState.FULL_IMPRESSION_VISIBLE, environment, component)
        viewEventHandler.onVisibilityStateChanged(VisibilityState.UNFOCUSED_VISIBLE, environment, component)
        viewEventHandler.onVisibilityStateChanged(VisibilityState.VISIBLE, environment, component)

        // Then
        verifyNoMoreInteractions(tracker)
    }

    @Test
    fun `should call track for given track actions with default properties`() {
        // Given
        val component = getComponent()

        // When
        viewEventHandler.trackViewEvent("target_name", component)

        // Then
        verify(tracker).track(trackAction1.withDefaultProperties("target_name", component))
        verify(tracker).track(trackAction2.withDefaultProperties("target_name", component))
    }

    private fun getComponent(): MenuComponentModel {
        return MenuComponentModel(
            "alpha",
            "menu_item",
            ComponentModel.Metadata(
                "name1",
                "feature1",
                "Kumaoni"
            ),
            mapOf(
                "view" to setOf<Action>(
                    trackAction1, trackAction2
                )
            ),
            "title",
            "icon"
        )
    }
}
