package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.DueInfoRepo
import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class GetCustomerDueInfoTest {
    private val customerDueInfoRepo: DueInfoRepo = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getCustomerDueInfoTest: GetCustomerDueInfo = GetCustomerDueInfo(Lazy { customerDueInfoRepo }, { getActiveBusinessId })

    @Before
    fun setup() {
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `getCustomerDueInfo() is a success`() {
        val response: DueInfo = mock()
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerDueInfoRepo.getDueInfoForCustomer("customer_id", businessId)).thenReturn(Observable.just(response))

        val testObserver = getCustomerDueInfoTest.execute(GetCustomerDueInfo.Request("customer_id")).test()

        testObserver.assertValues(response)

        testObserver.dispose()
    }
}
