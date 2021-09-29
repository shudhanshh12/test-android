package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase

import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.merchant.customer_ui.usecase.GetAllTransactionCount
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class GetCalculatorEducationVisibilityTest {

    private lateinit var getCalculatorEducationVisibility: GetCalculatorEducationVisibility

    private val ab: AbRepository = mock()
    private val getAllTransactionCount: GetAllTransactionCount = mock()
    private val customerRepositoryImpl: CustomerRepositoryImpl = mock()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        getCalculatorEducationVisibility = GetCalculatorEducationVisibility(
            abRepository = { ab },
            getAllTransactionCount = { getAllTransactionCount },
            customerRepository = { customerRepositoryImpl }
        )
    }

    @Test
    fun `feature not enabled should return false`() {
        mockFeatureEnabled(false)
        mockExperimentEnabled(true)
        mockShouldShowEducation(true)
        mockTransactionCount(1)
        mockExperimentVariant("")

        getCalculatorEducationVisibility.execute().test().assertValue { !it }
    }

    @Test
    fun `should show education not enabled then return false`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(true)
        mockShouldShowEducation(false)
        mockTransactionCount(1)
        mockExperimentVariant("")

        getCalculatorEducationVisibility.execute().test().assertValue { !it }
    }

    @Test
    fun `experiment not enabled then return false`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(false)
        mockShouldShowEducation(true)
        mockTransactionCount(1)
        mockExperimentVariant("")

        getCalculatorEducationVisibility.execute().test().assertValue { !it }
    }

    @Test
    fun `unknown experiment variant then return false`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(true)
        mockShouldShowEducation(true)
        mockTxnCountForEducation(2)
        mockTransactionCount(1)
        mockExperimentVariant("variant3")

        getCalculatorEducationVisibility.execute().test().assertValue { !it }
    }

    @Test
    fun `if showing education for first time then save in repo and return true`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(true)
        mockShouldShowEducation(true)
        mockTransactionCount(2)
        mockTxnCountForEducation(-1)
        mockExperimentVariant("consecutive_txns")
        doNothing().whenever(customerRepositoryImpl).setTxnCountForCalculatorEducation(2)

        val observer = getCalculatorEducationVisibility.execute().test()
        verify(customerRepositoryImpl).setTxnCountForCalculatorEducation(2)

        observer.assertValue { it }
    }

    @Test
    fun `if variant consecutive and txn count - education txn count == 1 then return true`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(true)
        mockShouldShowEducation(true)
        mockTransactionCount(3)
        mockTxnCountForEducation(2)
        mockExperimentVariant("consecutive_txns")

        getCalculatorEducationVisibility.execute().test().assertValue { it }
    }

    @Test
    fun `if variant consecutive and txn count - education txn count == 2 then return true`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(true)
        mockShouldShowEducation(true)
        mockTransactionCount(4)
        mockTxnCountForEducation(2)
        mockExperimentVariant("consecutive_txns")

        getCalculatorEducationVisibility.execute().test().assertValue { it }
    }

    @Test
    fun `if variant consecutive and txn count - education txn count == 3 then return false`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(true)
        mockShouldShowEducation(true)
        mockTransactionCount(5)
        mockTxnCountForEducation(2)
        mockExperimentVariant("consecutive_txns")

        getCalculatorEducationVisibility.execute().test().assertValue { !it }
    }

    @Test
    fun `if variant every third txn and txn count - education txn count == 1 then return false`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(true)
        mockShouldShowEducation(true)
        mockTransactionCount(3)
        mockTxnCountForEducation(2)
        mockExperimentVariant("every_third_txn")

        getCalculatorEducationVisibility.execute().test().assertValue { !it }
    }

    @Test
    fun `if variant third and txn count - education txn count == 2 then return false`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(true)
        mockShouldShowEducation(true)
        mockTransactionCount(4)
        mockTxnCountForEducation(2)
        mockExperimentVariant("every_third_txn")

        getCalculatorEducationVisibility.execute().test().assertValue { !it }
    }

    @Test
    fun `if variant third and txn count - education txn count == 3 then return true`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(true)
        mockShouldShowEducation(true)
        mockTransactionCount(5)
        mockTxnCountForEducation(2)
        mockExperimentVariant("every_third_txn")

        getCalculatorEducationVisibility.execute().test().assertValue { it }
    }

    @Test
    fun `if variant third and txn count - education txn count == 4 then return false`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(true)
        mockShouldShowEducation(true)
        mockTransactionCount(6)
        mockTxnCountForEducation(2)
        mockExperimentVariant("every_third_txn")

        getCalculatorEducationVisibility.execute().test().assertValue { !it }
    }

    @Test
    fun `if variant third and txn count - education txn count == 5 then return false`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(true)
        mockShouldShowEducation(true)
        mockTransactionCount(7)
        mockTxnCountForEducation(2)
        mockExperimentVariant("every_third_txn")

        getCalculatorEducationVisibility.execute().test().assertValue { !it }
    }

    @Test
    fun `if variant third and txn count - education txn count == 6 then return true`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(true)
        mockShouldShowEducation(true)
        mockTransactionCount(8)
        mockTxnCountForEducation(2)
        mockExperimentVariant("every_third_txn")

        getCalculatorEducationVisibility.execute().test().assertValue { it }
    }

    @Test
    fun `if variant third and txn count - education txn count == 7 then return false`() {
        mockFeatureEnabled(true)
        mockExperimentEnabled(true)
        mockShouldShowEducation(true)
        mockTransactionCount(9)
        mockTxnCountForEducation(2)
        mockExperimentVariant("every_third_txn")

        getCalculatorEducationVisibility.execute().test().assertValue { !it }
    }

    private fun mockTxnCountForEducation(mockCount: Int) {
        whenever(customerRepositoryImpl.getTxnCntForCalculatorEducation()).thenReturn(mockCount)
    }

    private fun mockShouldShowEducation(mockReturn: Boolean) {
        whenever(customerRepositoryImpl.canShowCalculatorEducation()).thenReturn(Observable.just(mockReturn))
    }

    private fun mockTransactionCount(mockCount: Int) {
        whenever(getAllTransactionCount.execute(Unit)).thenReturn(Observable.just(Result.Success(mockCount)))
    }

    private fun mockFeatureEnabled(mockReturn: Boolean) {
        whenever(ab.isFeatureEnabled("why_to_use_okc")).thenReturn(Observable.just(mockReturn))
    }

    private fun mockExperimentEnabled(mockReturn: Boolean) {
        whenever(ab.isExperimentEnabled("postlogin_android-all-calculator_education")).thenReturn(
            Observable.just(
                mockReturn
            )
        )
    }

    private fun mockExperimentVariant(mockReturn: String) {
        whenever(ab.getExperimentVariant("postlogin_android-all-calculator_education")).thenReturn(
            Observable.just(
                mockReturn
            )
        )
    }
}
