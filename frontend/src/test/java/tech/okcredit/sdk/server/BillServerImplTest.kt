package tech.okcredit.sdk.server

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import tech.okcredit.android.base.utils.ThreadUtils

class BillServerImplTest {
    private val apiClient: BillApiClient = mock()
    private val billRemoteSource = BillRemoteSourceImpl(Lazy { apiClient })

    @Before
    fun setup() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `getBills() when api call successful then return response body`() {
        val request: BillApiMessages.ListBillsRequest = mock()
        val response: BillApiMessages.ListBillsResponse = mock()
        val businessId = "business-id"
        whenever(apiClient.listBills(request, businessId)).thenReturn(Single.just(Response.success(response)))

        val testObserver = billRemoteSource.getBills(request, businessId).test()

        testObserver.assertValue(response)
        verify(apiClient, times(1)).listBills(request, businessId)
    }

    @Test
    fun `createBill() when api call successful then return response body`() {
        val listBillOperations: List<BillApiMessages.BillOperation> = mock()
        val request = BillApiMessages.BillSyncRequest(listBillOperations)
        val response: BillApiMessages.BillSyncResponse = mock()
        val businessId = "business-id"
        whenever(apiClient.postBills(request, businessId)).thenReturn(Single.just(Response.success(response)))

        val testObserver = billRemoteSource.createBill(listBillOperations, "", businessId).test()

        testObserver.assertValue(response)
        verify(apiClient, times(1)).postBills(request, businessId)
    }

    @Test
    fun `getBills() when startdate and source is provided when api call successful then return response body`() {
        val request: BillApiMessages.ListBillsRequest = BillApiMessages.ListBillsRequest(
            start_time_ms = 12234456,
            order_by = 1,
            exclude_deleted = false
        )
        val response: BillApiMessages.ListBillsResponse = mock()
        val businessId = "business-id"
        whenever(apiClient.listBills(request, businessId)).thenReturn(Single.just(Response.success(response)))

        val testObserver = billRemoteSource.getBills(12234456, "source", businessId).test()

        testObserver.assertValue(response)
        verify(apiClient, times(1)).listBills(request, businessId)
    }

    @Test
    fun `getTransactionFile()  when api call successful then return response body`() {
        val request = BillApiMessages.GetBillFileRequest("id")
        val businessId = "business-id"
        val response: BillApiMessages.GetBillFileResponse = mock()
        whenever(apiClient.getBillFile(request, businessId)).thenReturn(Single.just(Response.success(response)))

        val testObserver = billRemoteSource.getTransactionFile("id", businessId).test()

        testObserver.assertValue(response)
        verify(apiClient, times(1)).getBillFile(request, businessId)
    }

    @Test
    fun `deletedBill()  when api call successful then return response body`() {
        val request: BillApiMessages.BillSyncRequest = mock()
        val response: BillApiMessages.GetBillFileResponse = mock()
        val businessId = "business-id"
        whenever(apiClient.deletedBill(request, businessId)).thenReturn(Single.just(Response.success(response)))

        val testObserver = billRemoteSource.deletedBill(request, businessId).test()

        testObserver.assertValue(response)
        verify(apiClient, times(1)).deletedBill(request, businessId)
    }

    @Test
    fun `uploadNewBillDocs()  when api call successful then return response body`() {
        val request: BillApiMessages.BillSyncRequest = mock()
        val response: BillApiMessages.BillSyncResponse = mock()
        val businessId = "business-id"
        whenever(apiClient.postBills(request, businessId)).thenReturn(Single.just(Response.success(response)))

        val testObserver = billRemoteSource.uploadNewBillDocs(request, businessId).test()

        testObserver.assertValue(response)
        verify(apiClient, times(1)).postBills(request, businessId)
    }

    @Test
    fun `updateNote()  when api call successful then return response body`() {
        val request: BillApiMessages.BillSyncRequest = mock()
        val response: BillApiMessages.BillSyncResponse = mock()
        val businessId = "business-id"
        whenever(apiClient.postBills(request, businessId)).thenReturn(Single.just(Response.success(response)))

        val testObserver = billRemoteSource.updateNote(request, businessId).test()

        testObserver.assertValue(response)
        verify(apiClient, times(1)).postBills(request, businessId)
    }

    @Test
    fun `deleteBillDoc()  when api call successful then return response body`() {
        val request: BillApiMessages.BillSyncRequest = mock()
        val response: BillApiMessages.BillSyncResponse = mock()
        val businessId = "business-id"
        whenever(apiClient.postBills(request, businessId)).thenReturn(Single.just(Response.success(response)))

        val testObserver = billRemoteSource.deleteBillDoc(request, businessId).test()

        testObserver.assertValue(response)
        verify(apiClient, times(1)).postBills(request, businessId)
    }
}
