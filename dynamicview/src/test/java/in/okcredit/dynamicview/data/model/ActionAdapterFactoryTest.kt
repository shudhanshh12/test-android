package `in`.okcredit.dynamicview.data.model

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.junit.Test

class ActionAdapterFactoryTest {

    @Test
    fun `should return factory instance which can parse given actions`() {
        // Given
        val adapter = getMoshiAdapter()

        // When
        val trackResult = adapter.fromJson(
            "{\n" +
                "            \"action\": \"track\",\n" +
                "            \"event\": \"View Banner\",\n" +
                "            \"properties\": {\n" +
                "              \"debug\": \"true\"\n" +
                "            }\n" +
                "          }"
        )

        val navigateResult = adapter.fromJson(
            "{\n" +
                "            \"action\": \"navigate\",\n" +
                "            \"url\": \"https://google.co.in\"\n" +
                "          }"
        )

        // Then
        assertThat(trackResult).isEqualTo(Action.Track("View Banner", mapOf("debug" to "true")))
        assertThat(navigateResult).isEqualTo(Action.Navigate("https://google.co.in"))
    }

    @Test
    fun `should return factory instance which will return null for unknown actions`() {
        // Given
        val adapter = getMoshiAdapter()

        // When
        val result = adapter.fromJson(
            "{\n" +
                "            \"action\": \"close\",\n" +
                "            \"url\": \"https://google.co.in\"\n" +
                "          }"
        )

        // Then
        assertThat(result).isNull()
    }

    private fun getMoshiAdapter(): JsonAdapter<Action> {
        val actionAdapterFactory = ActionAdapterFactory(
            mapOf(
                Action.Navigate.NAME to Action.Navigate::class.java,
                Action.Track.NAME to Action.Track::class.java
            )
        )
        val moshi = Moshi.Builder().add(actionAdapterFactory.newInstance()).build()
        return moshi.adapter(Action::class.java)
    }
}
