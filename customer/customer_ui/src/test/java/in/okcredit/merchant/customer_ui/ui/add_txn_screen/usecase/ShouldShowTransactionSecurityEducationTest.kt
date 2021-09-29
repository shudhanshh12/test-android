package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class ShouldShowTransactionSecurityEducationTest {

    private lateinit var shouldShowTransactionSecurityEducation: ShouldShowTransactionSecurityEducation

    private val abRepository: AbRepository = mock()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        shouldShowTransactionSecurityEducation = ShouldShowTransactionSecurityEducation(
            abRepository = { abRepository },
        )
    }

    @Test
    fun `overall experiment not enabled then return none`() {
        mockExperimentEnabled(false)
        mockExperimentVariant("new_screen")

        // call execute method
        val testObserver = shouldShowTransactionSecurityEducation.execute().test()

        // assert correct value being sent

        Assert.assertTrue(testObserver.values().last() == (TransactionSuccessScreenVariant.OLD_DESIGN))

        // cleanup
        testObserver.dispose()
    }

    @Test
    fun `variant experiment other then return old design`() {
        mockExperimentEnabled(true)
        mockExperimentVariant("third_design")

        // call execute method
        val testObserver = shouldShowTransactionSecurityEducation.execute().test()

        // assert correct value being sent
        Assert.assertTrue(testObserver.values().last() == (TransactionSuccessScreenVariant.OLD_DESIGN))

        // cleanup
        testObserver.dispose()
    }

    @Test
    fun `experiment variant new design`() {
        mockExperimentEnabled(true)
        mockExperimentVariant("new_screen")

        // call execute method
        val testObserver = shouldShowTransactionSecurityEducation.execute().test()

        // assert correct value being sent
        Assert.assertTrue(testObserver.values().last() == (TransactionSuccessScreenVariant.NEW_DESIGN))

        // cleanup
        testObserver.dispose()
    }

    @Test
    fun `experiment variant expanded`() {
        mockExperimentEnabled(true)
        mockExperimentVariant("old_screen")

        // call execute method
        val testObserver = shouldShowTransactionSecurityEducation.execute().test()

        // assert correct value being sent
        Assert.assertTrue(testObserver.values().last() == (TransactionSuccessScreenVariant.OLD_DESIGN))

        // cleanup
        testObserver.dispose()
    }

    private fun mockExperimentEnabled(mockReturn: Boolean) {
        whenever(abRepository.isExperimentEnabled("postlogin_android-all-transaction_complete_screen")).thenReturn(
            Observable.just(
                mockReturn
            )
        )
    }

    private fun mockExperimentVariant(mockReturn: String) {
        whenever(abRepository.getExperimentVariant("postlogin_android-all-transaction_complete_screen")).thenReturn(
            Observable.just(
                mockReturn
            )
        )
    }
}
