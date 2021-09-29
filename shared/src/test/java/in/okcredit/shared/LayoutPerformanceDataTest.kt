package `in`.okcredit.shared

import `in`.okcredit.shared.performance.PerformanceTracker
import `in`.okcredit.shared.performance.layout_perf.LayoutPerformanceData
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Test

class LayoutPerformanceDataTest {

    private val layoutPerformanceData: LayoutPerformanceData = LayoutPerformanceData()
    private val performanceTracker: PerformanceTracker = mock()

    @Before
    fun setup() {
        layoutPerformanceData.shouldLogLayoutPerf = true
        layoutPerformanceData.layoutName = "TestView"
    }

    @Test
    fun `onMeasure() should count no of times of onMeasure and sum of onMeasures till first onLayout`() {
        layoutPerformanceData.onMeasureCalled(1)
        layoutPerformanceData.onMeasureCalled(2)
        layoutPerformanceData.onMeasureCalled(1)
        layoutPerformanceData.onLayoutCalled(1)
        layoutPerformanceData.onMeasureCalled(1)

        Truth.assertThat(layoutPerformanceData.totalOnMeasureCountBeforeFirstOnLayout == 3).isTrue()
        Truth.assertThat(layoutPerformanceData.totalOnMeasureDurationBeforeFirstOnLayout == 4L).isTrue()
    }

    @Test
    fun `onMeasure() should count no of times of onMeasure and sum of onMeasures between first and second onLayout`() {
        layoutPerformanceData.onMeasureCalled(1)
        layoutPerformanceData.onMeasureCalled(2)
        layoutPerformanceData.onMeasureCalled(1)
        layoutPerformanceData.onLayoutCalled(1)
        layoutPerformanceData.onMeasureCalled(1)
        layoutPerformanceData.onMeasureCalled(5)
        layoutPerformanceData.onLayoutCalled(1)
        layoutPerformanceData.onMeasureCalled(1)
        layoutPerformanceData.onMeasureCalled(1)

        Truth.assertThat(layoutPerformanceData.totalOnMeasureCountBeforeSecondOnLayout == 2).isTrue()
        Truth.assertThat(layoutPerformanceData.totalOnMeasureDurationBeforeSecondOnLayout == 6L).isTrue()
    }

    @Test
    fun `onMeasure() should update no of times of onMeasure and sum of onMeasures`() {
        layoutPerformanceData.onMeasureCalled(1)
        layoutPerformanceData.onMeasureCalled(2)
        layoutPerformanceData.onMeasureCalled(3)

        Truth.assertThat(layoutPerformanceData.totalOnMeasureCount == 3).isTrue()
        Truth.assertThat(layoutPerformanceData.totalOnMeasureDuration == 6L).isTrue()
    }

    @Test
    fun `onMeasure() should update maximum of onMeasure()`() {
        layoutPerformanceData.onMeasureCalled(1)
        layoutPerformanceData.onMeasureCalled(2)
        layoutPerformanceData.onMeasureCalled(10)
        layoutPerformanceData.onMeasureCalled(3)
        layoutPerformanceData.onMeasureCalled(3)

        Truth.assertThat(layoutPerformanceData.maxOfOnMeasure == 10L).isTrue()
    }

    @Test
    fun `onLayout() should update no of times onLayout and duration 1`() {
        layoutPerformanceData.onLayoutCalled(3)

        Truth.assertThat(layoutPerformanceData.totalOnLayoutCount == 1).isTrue()
        Truth.assertThat(layoutPerformanceData.totalOnLayoutDuration == 3L).isTrue()
        Truth.assertThat(layoutPerformanceData.isFirstOnLayoutCalled).isTrue()
        Truth.assertThat(layoutPerformanceData.isSecondOnLayoutCalled).isFalse()
    }

    @Test
    fun `onLayout() should update no of times onLayout and duration 2`() {
        layoutPerformanceData.onLayoutCalled(3)
        layoutPerformanceData.onLayoutCalled(4)
        layoutPerformanceData.onLayoutCalled(1)

        Truth.assertThat(layoutPerformanceData.totalOnLayoutCount == 3).isTrue()
        Truth.assertThat(layoutPerformanceData.totalOnLayoutDuration == 8L).isTrue()
        Truth.assertThat(layoutPerformanceData.isFirstOnLayoutCalled).isTrue()
        Truth.assertThat(layoutPerformanceData.isSecondOnLayoutCalled).isTrue()
    }

    @Test
    fun `onDrawCalled() should update no of times onDraw and duration `() {
        layoutPerformanceData.onDrawCalled(3)
        layoutPerformanceData.onDrawCalled(4)
        layoutPerformanceData.onDrawCalled(1)

        Truth.assertThat(layoutPerformanceData.totalOnDrawCount == 3).isTrue()
        Truth.assertThat(layoutPerformanceData.totalOnDrawDuration == 8L).isTrue()
    }

    @Test
    fun `not update any values if tracker is off`() {
        layoutPerformanceData.shouldLogLayoutPerf = false

        layoutPerformanceData.onMeasureCalled(1)
        layoutPerformanceData.onMeasureCalled(2)
        layoutPerformanceData.onMeasureCalled(1)
        layoutPerformanceData.onLayoutCalled(1)
        layoutPerformanceData.onMeasureCalled(1)
        layoutPerformanceData.onDrawCalled(1)

        Truth.assertThat(layoutPerformanceData.totalOnDrawDuration == 0L).isTrue()
        Truth.assertThat(layoutPerformanceData.totalOnMeasureDuration == 0L).isTrue()
        Truth.assertThat(layoutPerformanceData.totalOnLayoutDuration == 0L).isTrue()
    }

    @Test
    fun `make tracking disable only it tracked the values`() {

        layoutPerformanceData.track { performanceTracker }

        Truth.assertThat(layoutPerformanceData.shouldLogLayoutPerf).isFalse()
    }
}
