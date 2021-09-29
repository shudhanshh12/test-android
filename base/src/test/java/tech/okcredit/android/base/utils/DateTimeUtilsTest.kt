package tech.okcredit.android.base.utils

import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test

class DateTimeUtilsTest {

    @Test
    fun `isTimeWithinLast10Second should return false if time lesser than 10sec`() {
        val currentTime = DateTime().millis
        val decreasedTime = currentTime.minus(10001)
        Assert.assertEquals(false, DateTimeUtils.isTimeWithinLast10Second(decreasedTime.toString()))
    }
}
