package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend.contract.GetTotalTxnCount
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class GetAllTransactionCountTest {

    private lateinit var getAllTransactionCount: GetAllTransactionCount

    private val getTotalTxnCount: GetTotalTxnCount = mock()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        getAllTransactionCount = GetAllTransactionCount { getTotalTxnCount }
    }

    @Test
    fun `get counts calls transaction repo all transaction count`() {
        whenever(getTotalTxnCount.execute()).thenReturn(Single.just(10))

        val testObserver = getAllTransactionCount.execute(Unit).test()

        verify(getTotalTxnCount).execute()

        assert(testObserver.values().last() == Result.Success(10))
    }
}
