package `in`.okcredit.dynamicview.data.model

import `in`.okcredit.dynamicview.component.menu.MenuComponentModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ActionTest {

    @Test
    fun `should set properties from component and return track object`() {
        // Given
        val action = Action.Track("Customization Viewed", mapOf("default key" to "default value"))
        val component = MenuComponentModel(
            "alpha",
            "menu_item",
            ComponentModel.Metadata(
                "name1",
                "feature1",
                "Kumaoni"
            ),
            null,
            "title",
            "icon"
        )

        // When
        val result = action.withDefaultProperties("side_menu", component)

        // Then
        val expectedAction = Action.Track(
            "Customization Viewed",
            mapOf(
                "default key" to "default value",
                "target" to "side_menu",
                "component_version" to "alpha",
                "component_kind" to "menu_item",
                "name" to "name1",
                "feature" to "feature1",
                "lang" to "Kumaoni"
            )
        )
        assertThat(result).isEqualTo(expectedAction)
    }
}
