package `in`.okcredit.communication_inappnotification.local

import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DisplayStatusMapperTest {

    @Test
    fun `given TO_BE_DISPLAYED return TO_BE_DISPLAYED string`() {
        // given
        val displayStatusMapper = DisplayStatusMapper()
        val status = DisplayStatus.TO_BE_DISPLAYED

        // when
        val string = displayStatusMapper.statusToString(status)

        // then
        assertThat(string == status.name).isTrue()
    }

    @Test
    fun `given TO_BE_DISPLAYED string return TO_BE_DISPLAYED`() {
        // given
        val displayStatusMapper = DisplayStatusMapper()
        val string = DisplayStatus.TO_BE_DISPLAYED.name

        // when
        val status = displayStatusMapper.stringToStatus(string)

        // then
        assertThat(string == status.name).isTrue()
    }
}
