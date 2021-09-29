package `in`.okcredit.merchant.core.sync

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.model.Customer
import `in`.okcredit.merchant.core.server.CoreRemoteSource
import `in`.okcredit.merchant.core.store.CoreLocalSource
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkContinuation
import com.nhaarman.mockitokotlin2.*
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import tech.okcredit.android.base.utils.LogUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.OkcWorkManager

class SyncCustomersTest {

    private val mockSyncCustomerCommands: SyncCustomerCommands = mock()
    private val mockRemoteSource: CoreRemoteSource = mock()
    private val mockLocalSource: CoreLocalSource = mock()
    private val mockWorkManager: OkcWorkManager = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val syncCustomers = SyncCustomers(
        { mockSyncCustomerCommands },
        { mockRemoteSource },
        { mockLocalSource },
        { mockWorkManager },
        { getActiveBusinessId },
    )

    @Before
    fun setup() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.newThread() } returns Schedulers.trampoline()
    }

    @Test
    fun `execute() should get customers from server and save in database`() {
        val businessId = "business-id"
        val customerList: List<Customer> = listOf(mock(), mock(), mock(), mock(), mock(), mock())
        runBlocking { `when`(mockSyncCustomerCommands.execute(false)).thenReturn(Unit) }
        whenever(mockRemoteSource.listCustomers(null, businessId)).thenReturn(Single.just(customerList))
        whenever(mockLocalSource.resetCustomerList(customerList, businessId)).thenReturn(Completable.complete())
        whenever(getActiveBusinessId.thisOrActiveBusinessId(anyOrNull())).thenReturn(Single.just(businessId))
        val testObserver = syncCustomers.execute().test()

        assert(testObserver.awaitTerminalEvent())
        verify(mockRemoteSource, times(1)).listCustomers(null, businessId)
        verify(mockLocalSource, times(1)).resetCustomerList(customerList, businessId)
    }

    // @Test // Todo: Mocking constructor with param not possible with mockk (https://github.com/mockk/mockk/issues/209)
    fun `schedule() should schedule worker`() {
        mockkStatic(LogUtils::class)
        val workRequest: OneTimeWorkRequest = mock()
        val businessId = "business-id"
        whenever(workRequest.id).thenReturn(mock())
//        every { LogUtils.enableWorkerLogging(workRequest) } returns Unit
        val builder: OneTimeWorkRequest.Builder = mock()
//        mockkConstructor(OneTimeWorkRequest.Builder::class)
//        every { anyConstructed<OneTimeWorkRequest.Builder>(SyncCustomers.Worker::class.java) } returns builder
        Mockito.`when`(OneTimeWorkRequest.Builder(SyncCustomers.Worker::class.java)).thenReturn(builder)
        whenever(builder.addTag(any())).thenReturn(builder)
        whenever(builder.setConstraints(any())).thenReturn(builder)
        whenever(builder.setBackoffCriteria(any(), any(), any())).thenReturn(builder)
        whenever(builder.build()).thenReturn(workRequest)
        val workContinuation: WorkContinuation = mock()
        whenever(workContinuation.enqueue()).thenReturn(mock())

        val testObserver = syncCustomers.schedule(businessId).test()

        testObserver.assertComplete()
    }
}
