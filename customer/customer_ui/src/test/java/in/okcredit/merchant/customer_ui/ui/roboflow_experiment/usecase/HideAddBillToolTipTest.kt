package `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase

import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class HideAddBillToolTipTest {
    private val mockCustomerRepositoryImpl: CustomerRepositoryImpl = mock()

    private val hideAddBillToolTip = HideAddBillToolTip { mockCustomerRepositoryImpl }

    @Test
    fun `should call setAddBillToolTipShowed`() {
        hideAddBillToolTip.execute()

        verify(mockCustomerRepositoryImpl, times(1)).setAddBillToolTipShowed()
    }
}
