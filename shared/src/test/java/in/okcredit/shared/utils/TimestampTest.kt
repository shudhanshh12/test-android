package `in`.okcredit.shared.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class TimestampTest {

    @Test
    fun `currentTimestamp() should return 10 digit seconds`() {
        val timestamp = TimestampUtils.currentTimestamp()

        assertEquals(13, "${timestamp.epoch}".length)
        assertEquals(10, "${timestamp.seconds}".length)
    }

    @Test
    fun `toTimestamp() given long should convert seconds into milliseconds`() {
        val longTimestamp = 1590638990L
        val timestamp = longTimestamp.toTimestamp()

        assertEquals(longTimestamp * 1000, timestamp.epoch)
        assertEquals(longTimestamp, timestamp.seconds)
    }

    @Test
    fun `toTimestamp() given string should convert seconds into milliseconds`() {
        val stringTimestamp = "1590638990"
        val timestamp = stringTimestamp.toTimestamp()

        assertEquals("${stringTimestamp}000", timestamp.epoch.toString())
        assertEquals(stringTimestamp, timestamp.seconds.toString())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toTimestamp() given illegal string should throw exception`() {
        val stringTimestamp = "illegal"
        val timestamp = stringTimestamp.toTimestamp()
    }
}
