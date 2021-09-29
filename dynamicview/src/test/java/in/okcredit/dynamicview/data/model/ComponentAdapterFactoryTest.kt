package `in`.okcredit.dynamicview.data.model

import `in`.okcredit.dynamicview.component.banner.BannerComponentModel
import `in`.okcredit.dynamicview.component.menu.MenuComponentModel
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.junit.Test

class ComponentAdapterFactoryTest {

    @Test
    fun `should return factory instance which can parse given components`() {
        // Given
        val adapter = getMoshiAdapter()

        // When
        val menuResult = adapter.fromJson(
            "{\n" +
                "          \"version\": \"v1-alpha\",\n" +
                "          \"kind\": \"menu_item\",\n" +
                "          \"title\": \"WhatsApp us\",\n" +
                "          \"icon\": \"https://pbs.twimg.com/profile_images/724435763287326720/7Ntlvkey_400x400.jpg\"\n" +
                "        }"
        )
        val bannerResult = adapter.fromJson(
            "{\n" +
                "   \"version\": \"v2-alpha\",\n" +
                "   \"kind\": \"banner\",\n" +
                "   \"title\": \"Online Medicines\",\n" +
                "   \"subtitle\": \"Now you can get online medicines online\",\n" +
                "   \"icon\": \"https://firebasestorage.googleapis.com/v0/b/okcredit-6cb68.appspot.com/o/link_pay.webp?alt=media&token=13001152-7a9d-4a97-9186-5e23020920f6\"\n" +
                "}"
        )

        // Then
        assertThat(menuResult).isEqualTo(
            MenuComponentModel(
                "v1-alpha",
                "menu_item",
                null,
                null,
                "WhatsApp us",
                "https://pbs.twimg.com/profile_images/724435763287326720/7Ntlvkey_400x400.jpg"
            )
        )

        assertThat(bannerResult).isEqualTo(
            BannerComponentModel(
                "v2-alpha",
                "banner",
                null,
                null,
                "Online Medicines",
                "Now you can get online medicines online",
                "https://firebasestorage.googleapis.com/v0/b/okcredit-6cb68.appspot.com/o/link_pay.webp?alt=media&token=13001152-7a9d-4a97-9186-5e23020920f6"
            )
        )
    }

    @Test
    fun `should return factory instance which will return null for unknown component`() {
        // Given
        val adapter = getMoshiAdapter()

        // When
        val result = adapter.fromJson(
            "{\n" +
                "      \"version\": \"v1-alpha\",\n" +
                "      \"kind\": \"toolbar\",\n" +
                "      \"icon\": \"https://pbs.twimg.com/profile_images/724435763287326720/7Ntlvkey_400x400.jpg\"\n" +
                "  }"
        )

        // Then
        assertThat(result).isNull()
    }

    private fun getMoshiAdapter(): JsonAdapter<ComponentModel> {
        val actionAdapterFactory = ActionAdapterFactory(
            mapOf(
                Action.Navigate.NAME to Action.Navigate::class.java,
                Action.Track.NAME to Action.Track::class.java
            )
        )
        val componentAdapterFactory = ComponentAdapterFactory(
            mapOf(
                MenuComponentModel.KIND to MenuComponentModel::class.java,
                BannerComponentModel.KIND to BannerComponentModel::class.java
            )
        )
        val moshi = Moshi.Builder()
            .add(actionAdapterFactory.newInstance())
            .add(componentAdapterFactory.newInstance())
            .build()
        return moshi.adapter(ComponentModel::class.java)
    }
}
