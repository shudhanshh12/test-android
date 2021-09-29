package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before

class GetOnlinePaymentsTest {

    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val customerRepo: CustomerRepo = mock()

    private val getOnlinePayment = GetOnlinePayments(collectionRepository, customerRepo, { getActiveBusinessId })

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { Schedulers.newThread() } returns Schedulers.trampoline()
    }

//    @Test
//    fun `execute should return list of onlineCollectionData`() {
//        val request = GetOnlinePayments.Request(mock(), mock())
//        val customers: List<Customer> = listOf(mock(), mock())
//        val collectionOnlinePayments: List<CollectionOnlinePayment> = listOf(mock(), mock())
//        val onlineCollectionDataList: List<GetOnlinePayments.OnlineCollectionData> = listOf(mock(), mock())
//
//        whenever(collectionApi.listOnlinePayments()).thenReturn(Observable.just(collectionOnlinePayments))
//        whenever(customerRepo.listCustomers()).thenReturn(Observable.just(customers))
//        whenever(collectionApi.setOnlinePaymentsDataRead()).thenReturn(Completable.complete())
//
//        val testObserver = getOnlinePayment.execute(request).test()
//        testObserver.assertValues(Result.Progress(), Result.Success(onlineCollectionDataList))
//
//        testObserver.dispose()
//
//    }
}
