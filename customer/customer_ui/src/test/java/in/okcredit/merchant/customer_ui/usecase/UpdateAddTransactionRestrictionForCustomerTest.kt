package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UpdateAddTransactionRestrictionForCustomerTest {
    private lateinit var updateAddTransactionRestrictionForCustomer: UpdateAddTransactionRestrictionForCustomer

    private val customerRepository: CustomerRepo = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val accountId = TestData.CUSTOMER.id
    private val businessId = TestData.BUSINESS_ID

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        updateAddTransactionRestrictionForCustomer = UpdateAddTransactionRestrictionForCustomer(
            { customerRepository },
            { getActiveBusinessId }
        )
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
    }

    @Test
    fun `execute calls repository update add transaction`() {
        whenever(
            customerRepository.updateAddTransactionRestrictedLocally(
                accountId,
                businessId
            )
        ).thenReturn(Completable.complete())
        // call execute
        val testObserver = updateAddTransactionRestrictionForCustomer.execute(UpdateAddTransactionRestrictionForCustomer.Request(accountId)).test()
        // assert update add txn count is called
        verify(customerRepository).updateAddTransactionRestrictedLocally(accountId, businessId)

        testObserver.dispose()
    }

    @Test
    fun `execute returns repository show education result`() {
        // mock repository to return false
        whenever(
            customerRepository.updateAddTransactionRestrictedLocally(
                accountId,
                businessId
            )
        ).thenReturn(Completable.complete())
        // call execute
        val testObserver = updateAddTransactionRestrictionForCustomer.execute(
            UpdateAddTransactionRestrictionForCustomer.Request(accountId)
        ).test()

        // assert correct value being sent
        Assert.assertTrue(testObserver.values().last() == Result.Success(Unit))

        // cleanup
        testObserver.dispose()
    }
}
