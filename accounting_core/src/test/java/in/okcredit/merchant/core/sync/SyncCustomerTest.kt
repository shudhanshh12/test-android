package `in`.okcredit.merchant.core.sync

import `in`.okcredit.merchant.core.model.Customer
import `in`.okcredit.merchant.core.server.CoreRemoteSource
import `in`.okcredit.merchant.core.store.CoreLocalSource
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.NonNls
import org.junit.Test
import tech.okcredit.android.base.workmanager.OkcWorkManager

class SyncCustomerTest {
    private val remoteSource: CoreRemoteSource = mock()
    private val localSource: CoreLocalSource = mock()
    private val workManager: OkcWorkManager = mock()
    private val syncCustomerCommands: SyncCustomerCommands = mock()
    private val getActiveBusinessId: `in`.okcredit.merchant.contract.GetActiveBusinessId = mock()

    private val syncCustomer: SyncCustomer = SyncCustomer(
        { syncCustomerCommands },
        { remoteSource },
        { localSource },
        { workManager },
        { getActiveBusinessId }
    )

    @Test
    fun `execute() should get customer from server and save in database`() {
        runBlocking {
            @NonNls
            val customerId = "customer_id"
            val businessId = "business-id"
            val customer: Customer = mock()
            whenever(remoteSource.getCustomer(customerId, businessId)).thenReturn(Single.just(customer))
            whenever(localSource.putCustomer(customer, businessId)).thenReturn(Completable.complete())
            whenever(syncCustomerCommands.execute(customerId)).thenReturn(Unit)
            whenever(getActiveBusinessId.thisOrActiveBusinessId(anyOrNull())).thenReturn(Single.just(businessId))

            val testObserver = syncCustomer.execute(customerId).test()

            assert(testObserver.awaitTerminalEvent())
            verify(remoteSource, times(1)).getCustomer(customerId, businessId)
            verify(localSource, times(1)).putCustomer(customer, businessId)
        }
    }
}
