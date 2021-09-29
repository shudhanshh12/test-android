package tech.okcredit.android.base.mobile

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MobileTest {

    @Test
    fun `null is parsed as null`() {
        assertThat(parseMobile(null)).isEqualTo(null)
    }

    @Test
    fun `empty is parsed as null`() {
        assertThat(parseMobile("")).isEqualTo(null)
    }

    @Test
    fun `7760747507 is parsed as 7760747507`() {
        assertThat(parseMobile("7760747507")).isEqualTo("7760747507")
    }

    @Test
    fun `776-074-7507 is parsed as 7760747507`() {
        assertThat(parseMobile("776-074-7507")).isEqualTo("7760747507")
    }

    @Test
    fun `776 074 7507 is parsed as 7760747507`() {
        assertThat(parseMobile("776 074 7507")).isEqualTo("7760747507")
    }

    @Test
    fun `+91-7760747507 is parsed as 7760747507`() {
        assertThat(parseMobile("+91-7760747507")).isEqualTo("7760747507")
    }

    @Test
    fun `91-776 074 7507 is parsed as 7760747507`() {
        assertThat(parseMobile("91-776 074 7507")).isEqualTo("7760747507")
    }

    @Test
    fun `07760747507 is parsed as 7760747507`() {
        assertThat(parseMobile("07760747507")).isEqualTo("7760747507")
    }

    @Test
    fun `+917760747507 is parsed as 7760747507`() {
        assertThat(parseMobile("+917760747507")).isEqualTo("7760747507")
    }

    @Test
    fun `917760747507 is parsed as 7760747507`() {
        assertThat(parseMobile("917760747507")).isEqualTo("7760747507")
    }

    @Test
    fun `007760747507 is parsed as 7760747507`() {
        assertThat(parseMobile("007760747507")).isEqualTo("7760747507")
    }

    @Test
    fun `9760747507 is parsed as 9760747507`() {
        assertThat(parseMobile("9760747507")).isEqualTo("9760747507")
    }

    @Test
    fun `8760747507 is parsed as 8760747507`() {
        assertThat(parseMobile("8760747507")).isEqualTo("8760747507")
    }

    @Test
    fun `6760747507 is parsed as 6760747507`() {
        assertThat(parseMobile("6760747507")).isEqualTo("6760747507")
    }

    @Test
    fun `5760747507 is parsed as 5760747507`() {
        assertThat(parseMobile("5760747507")).isEqualTo("5760747507")
    }

    @Test
    fun `4760747507 is parsed as null`() {
        assertThat(parseMobile("4760747507")).isEqualTo(null)
    }

    @Test
    fun `760747507 is parsed as null`() {
        assertThat(parseMobile("760747507")).isEqualTo(null)
    }
}
