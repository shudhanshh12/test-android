package `in`.okcredit.dynamicview

import `in`.okcredit.dynamicview.component.banner.BannerComponentModel
import `in`.okcredit.dynamicview.component.menu.MenuComponentModel
import `in`.okcredit.dynamicview.component.toolbar.ToolbarComponentModel
import `in`.okcredit.dynamicview.data.model.ComponentModel
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test

class EnvironmentTest {

    @Test
    fun `should return true when spec contains the passed class`() {
        // Given
        val spec = TargetSpec("target_name", setOf(MenuComponentModel::class.java, BannerComponentModel::class.java))
        val environment = Environment(spec, mock())
        val component = MenuComponentModel(
            "alpha",
            "VerticalList",
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
        val result = environment.isValidComponent(component)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when spec does not contain the passed class`() {
        // Given
        val spec = TargetSpec("target_name", setOf(ToolbarComponentModel::class.java, BannerComponentModel::class.java))
        val environment = Environment(spec, mock())
        val component = MenuComponentModel(
            "alpha",
            "VerticalList",
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
        val result = environment.isValidComponent(component)

        // Then
        assertThat(result).isFalse()
    }
}
