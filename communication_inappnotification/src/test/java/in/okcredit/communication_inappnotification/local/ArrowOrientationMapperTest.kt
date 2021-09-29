package `in`.okcredit.communication_inappnotification.local

import com.google.common.truth.Truth
import com.skydoves.balloon.ArrowOrientation
import org.junit.Test

class ArrowOrientationMapperTest {

    @Test
    fun `given TOP return TOP string`() {
        // given
        val arrowOrientationMapper = ArrowOrientationMapper()
        val orientation = ArrowOrientation.TOP

        // when
        val string = arrowOrientationMapper.arrowOrientationToString(orientation)

        // then
        Truth.assertThat(string == orientation.name).isTrue()
    }

    @Test
    fun `given TOP string return TOP`() {
        // given
        val arrowOrientationMapper = ArrowOrientationMapper()
        val string = ArrowOrientation.TOP.name

        // when
        val orientation = arrowOrientationMapper.stringToArrowOrientation(string)

        // then
        Truth.assertThat(string == orientation.name).isTrue()
    }
}
