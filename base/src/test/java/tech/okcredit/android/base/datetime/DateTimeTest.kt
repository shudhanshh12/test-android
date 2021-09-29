package tech.okcredit.android.base.datetime

import com.google.common.truth.Truth.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test

class DateTimeTest {

    @Test
    fun `toEpoch() returns 0 for null datetime`() {
        assertThat(toEpoch(null)).isEqualTo(0)
    }

    @Test
    fun `toEpoch() returns 7 for 1970-01-01 00_00_07 UTC`() {
        assertThat(toEpoch(DateTime.parse("1970-01-01T00:00:07Z"))).isEqualTo(7)
    }

    @Test
    fun `fromEpoch() returns 1970-01-01 00_00_07 UTC for epoch 7`() {
        assertThat(fromEpoch(7)?.withZone(DateTimeZone.UTC)).isEqualTo(DateTime.parse("1970-01-01T00:00:07Z"))
    }

    @Test
    fun `fromEpoch() returns null for epoch 0`() {
        assertThat(fromEpoch(0)).isNull()
    }

    @Test
    fun `fromEpoch() returns null for negative epoch`() {
        assertThat(fromEpoch(-1)).isNull()
    }
}
