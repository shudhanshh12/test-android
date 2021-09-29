package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase

import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class UpdateCalculatorEducationVisibilityTest {

    private lateinit var updateCalculatorEducationVisibility: UpdateCalculatorEducationVisibility

    private val customerRepositoryImpl: CustomerRepositoryImpl = mock()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns Dispatchers.Unconfined
        updateCalculatorEducationVisibility = UpdateCalculatorEducationVisibility(
            customerRepository = { customerRepositoryImpl },
        )
    }

    @Test
    fun `execute should reset count and calculator education flag`() {
        runBlocking {
            doNothing().whenever(customerRepositoryImpl).setTxnCountForCalculatorEducation(-1)

            val observer = updateCalculatorEducationVisibility.execute().test()
            verify(customerRepositoryImpl).setTxnCountForCalculatorEducation(-1)
            verify(customerRepositoryImpl).setShowCalculatorEducation(false)
            observer.assertComplete()
        }
    }
}
