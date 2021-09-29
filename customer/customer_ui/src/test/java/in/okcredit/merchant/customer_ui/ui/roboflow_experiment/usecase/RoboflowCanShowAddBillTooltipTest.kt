package `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase

import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class RoboflowCanShowAddBillTooltipTest {
    private val customerRepositoryImpl: CustomerRepositoryImpl = mock()

    private val roboflowCanShowAddBillTooltip = RoboflowCanShowAddBillTooltip { customerRepositoryImpl }

    @Test
    fun `should return true when isAddBillTooltipShowed return false`() {
        whenever(customerRepositoryImpl.isAddBillTooltipShowed()).thenReturn(flowOf(false))

        val result = roboflowCanShowAddBillTooltip.execute().test()

        result.assertValue(true)
    }

    @Test
    fun `should return false when isAddBillTooltipShowed return true`() {
        whenever(customerRepositoryImpl.isAddBillTooltipShowed()).thenReturn(flowOf(true))

        val result = roboflowCanShowAddBillTooltip.execute().test()

        result.assertValue(false)
    }
}
