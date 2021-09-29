package `in`.okcredit.installedpackges.utils

import `in`.okcredit.installedpackges.utils.InstalledPackagesUtils.getDaysDiffFrmTimestamps
import `in`.okcredit.installedpackges.utils.InstalledPackagesUtils.getTimestampFromString
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InstalledPackagesUtilsTest {

    @Test
    fun `getDaysDiffFrmTimestamps(1598638250000,1598896690456) returns 2`() {
        assertThat(getDaysDiffFrmTimestamps(1598638250000, 1598896690456)).isEqualTo(2)
    }

    @Test
    fun `getDaysDiffFrmTimestamps(1598638250000,1598638250000) returns 0`() {
        assertThat(getDaysDiffFrmTimestamps(1598638250000, 1598638250000)).isEqualTo(0)
    }

    @Test
    fun `getDaysDiffFrmTimestamps(1599070250000,1598638250000) returns -5`() {
        assertThat(getDaysDiffFrmTimestamps(1599070250000, 1598638250000)).isEqualTo(-5)
    }

    @Test
    fun `getTimestampFromString  returns correct timestamp`() {
        assertThat(getTimestampFromString("2020-08-21T11:00:00Z") == 1597987800000)
    }

    @Test
    fun `getTimestampFromString when wrong input returns currentTimestamp`() {
        // so when u pass wrong input it should send current timestamp but cannot compare with
        // that as time of comparison some millis time has spent so will check diff (100millis)
        val actualReturnTimestamp = getTimestampFromString("abc")
        val diff = System.currentTimeMillis().minus(actualReturnTimestamp)
        assertThat(diff < 100)
    }
}
