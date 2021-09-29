// package `in`.okcredit.sales_sdk.server
//
// import `in`.okcredit.sales_sdk.models.BillModel
// import `in`.okcredit.sales_sdk.models.Models
// import com.nhaarman.mockitokotlin2.any
// import com.nhaarman.mockitokotlin2.mock
// import com.nhaarman.mockitokotlin2.times
// import com.nhaarman.mockitokotlin2.verify
// import com.nhaarman.mockitokotlin2.whenever
// import io.mockk.every
// import io.mockk.mockkObject
// import io.mockk.mockkStatic
// import io.reactivex.Completable
// import io.reactivex.Single
// import io.reactivex.schedulers.Schedulers
// import org.joda.time.DateTime
// import org.junit.Before
// import org.junit.Test
// import retrofit2.Response
// import tech.okcredit.android.base.error.Error
// import tech.okcredit.android.base.utils.ThreadUtils
//
// class ServerImplTest {
//
//    private val apiClient: ApiClient = mock()
//    private val server: Server = ServerImpl(apiClient)
//
//    @Before
//    fun setup() {
//        mockkStatic(Response::class)
//        mockkStatic("in.okcredit.sales_sdk.server.ServerImpl")
//        mockkObject(ThreadUtils)
//        every { ThreadUtils.api() } returns Schedulers.trampoline()
//        every { ThreadUtils.worker() } returns Schedulers.trampoline()
//    }
//
//    @Test
//    fun `get sale on success`() {
//        val testRequest = "sale_id"
//        val testResponse: Models.SaleItemResponse = mock()
//        whenever(apiClient.getSale(testRequest)).thenReturn(Single.just(Response.success(testResponse)))
//
//        val testObserver = server.getSale(testRequest).test()
//
//        testObserver.assertValue(testResponse)
//        verify(apiClient, times(1)).getSale(testRequest)
//    }
//
//    @Test
//    fun `getSale() when api call unsuccessful returns error`() {
//        val testRequest = "sale_id"
//        val testResponse: Response<Models.SaleItemResponse> = mock()
//        whenever(apiClient.getSale(testRequest)).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.getSale(testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).getSale(testRequest)
//    }
//
//    @Test
//    fun `getSale() when body is null returns error`() {
//        val testRequest = "sale_id"
//        val testResponse: Response<Models.SaleItemResponse> = mock()
//        whenever(apiClient.getSale(any())).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(true)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.getSale(testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).getSale(testRequest)
//    }
//
//    @Test
//    fun `getSale() when api call unsuccessful and body is not null returns error`() {
//        val testRequest = "sale_id"
//        val responseBody: Models.SaleItemResponse = mock()
//        val testResponse: Response<Models.SaleItemResponse> = mock()
//        whenever(apiClient.getSale(any())).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(mock())
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.getSale(testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).getSale(testRequest)
//    }
//
//    @Test
//    fun `update sale on success`() {
//        val saleId = "sale_id"
//        val testRequest: Models.UpdateSaleItemRequest = mock()
//        val testResponse: Models.SaleItemResponse = mock()
//        whenever(apiClient.updateSaleItem(saleId, testRequest)).thenReturn(Single.just(Response.success(testResponse)))
//
//        val testObserver = server.updateSale(saleId, testRequest).test()
//
//        testObserver.assertValue(testResponse)
//        verify(apiClient, times(1)).updateSaleItem(saleId, testRequest)
//    }
//
//    @Test
//    fun `updateSale() when api call unsuccessful returns error`() {
//        val saleId = "sale_id"
//        val testRequest: Models.UpdateSaleItemRequest = mock()
//        val testResponse: Response<Models.SaleItemResponse> = mock()
//        whenever(apiClient.updateSaleItem(any(), any())).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.updateSale(saleId, testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).updateSaleItem(saleId, testRequest)
//    }
//
//    @Test
//    fun `updateSale() when body is null returns error`() {
//        val saleId = "sale_id"
//        val testRequest: Models.UpdateSaleItemRequest = mock()
//        val testResponse: Response<Models.SaleItemResponse> = mock()
//        whenever(apiClient.updateSaleItem(any(), any())).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(true)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.updateSale(saleId, testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).updateSaleItem(saleId, testRequest)
//    }
//
//    @Test
//    fun `updateSale() when api call unsuccessful and body is not null returns error`() {
//        val saleId = "sale_id"
//        val testRequest: Models.UpdateSaleItemRequest = mock()
//        val responseBody: Models.SaleItemResponse = mock()
//        val testResponse: Response<Models.SaleItemResponse> = mock()
//        whenever(apiClient.updateSaleItem(any(), any())).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(responseBody)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.updateSale(saleId, testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).updateSaleItem(saleId, testRequest)
//    }
//
//
//    @Test
//    fun `get sales on success with startTime and endTime`() {
//        val merchantId = "sale_id"
//        val startTime = DateTime.now().millis
//        val endTime = DateTime.now().millis
//        val testResponse: Models.SalesListResponse = mock()
//        whenever(apiClient.getSales(merchantId, startTime, endTime)).thenReturn(
//            Single.just(
//                Response.success(
//                    testResponse
//                )
//            )
//        )
//
//        val testObserver = server.getSales(merchantId, startTime, endTime).test()
//
//        testObserver.assertValue(testResponse)
//        verify(apiClient, times(1)).getSales(merchantId, startTime, endTime)
//    }
//
//    @Test
//    fun `get sales on success with startTime`() {
//        val merchantId = "sale_id"
//        val startTime = DateTime.now().millis
//        val endTime = null
//        val testResponse: Models.SalesListResponse = mock()
//        whenever(apiClient.getSales(merchantId, startTime, endTime)).thenReturn(
//            Single.just(
//                Response.success(
//                    testResponse
//                )
//            )
//        )
//
//        val testObserver = server.getSales(merchantId, startTime, endTime).test()
//
//        testObserver.assertValue(testResponse)
//        verify(apiClient, times(1)).getSales(merchantId, startTime, endTime)
//    }
//
//    @Test
//    fun `get sales on success with no range`() {
//        val merchantId = "sale_id"
//        val startTime = null
//        val endTime = null
//        val testResponse: Models.SalesListResponse = mock()
//        whenever(apiClient.getSales(merchantId, startTime, endTime)).thenReturn(
//            Single.just(
//                Response.success(
//                    testResponse
//                )
//            )
//        )
//
//        val testObserver = server.getSales(merchantId, startTime, endTime).test()
//
//        testObserver.assertValue(testResponse)
//        verify(apiClient, times(1)).getSales(merchantId, startTime, endTime)
//    }
//
//    @Test
//    fun `getSales() when api call unsuccessful returns error`() {
//        val merchantId = "sale_id"
//        val startTime = null
//        val endTime = null
//        val testResponse: Response<Models.SalesListResponse> = mock()
//        whenever(apiClient.getSales(merchantId, startTime, endTime)).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.getSales(merchantId, startTime, endTime).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).getSales(merchantId, startTime, endTime)
//    }
//
//    @Test
//    fun `getSales() when response body is null returns error`() {
//        val merchantId = "sale_id"
//        val startTime = null
//        val endTime = null
//        val testResponse: Response<Models.SalesListResponse> = mock()
//        whenever(apiClient.getSales(merchantId, startTime, endTime)).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(true)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.getSales(merchantId, startTime, endTime).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).getSales(merchantId, startTime, endTime)
//    }
//
//    @Test
//    fun `getSales() when api call unsuccessful and response body is not null returns error`() {
//        val merchantId = "sale_id"
//        val startTime = null
//        val endTime = null
//        val testResponse: Response<Models.SalesListResponse> = mock()
//        val responseBody: Models.SalesListResponse = mock()
//        whenever(apiClient.getSales(merchantId, startTime, endTime)).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(responseBody)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.getSales(merchantId, startTime, endTime).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).getSales(merchantId, startTime, endTime)
//    }
//
//    @Test
//    fun `submit() on success`() {
//        val testRequest: Models.SaleRequestModel = mock()
//        val testResponse: Models.AddSaleResponse = mock()
//        whenever(apiClient.submitSale(any())).thenReturn(Single.just(Response.success(testResponse)))
//
//        val testObserver = server.submit(testRequest).test()
//
//        testObserver.assertValue(testResponse)
//        verify(apiClient, times(1)).submitSale(testRequest)
//    }
//
//    @Test
//    fun `submit() when api call unsuccessful returns error`() {
//        val testRequest: Models.SaleRequestModel = mock()
//        val testResponse: Response<Models.AddSaleResponse> = mock()
//        whenever(apiClient.submitSale(any())).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.submit(testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).submitSale(testRequest)
//    }
//
//    @Test
//    fun `submit() when response body is null returns error`() {
//        val testRequest: Models.SaleRequestModel = mock()
//        val testResponse: Response<Models.AddSaleResponse> = mock()
//        whenever(apiClient.submitSale(any())).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(true)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.submit(testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).submitSale(testRequest)
//    }
//
//    @Test
//    fun `submit() when api call unsuccessful and response body is not null returns error`() {
//        val testRequest: Models.SaleRequestModel = mock()
//        val responseBody: Models.AddSaleResponse = mock()
//        val testResponse: Response<Models.AddSaleResponse> = mock()
//        whenever(apiClient.submitSale(any())).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(responseBody)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.submit(testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).submitSale(testRequest)
//    }
//
//    @Test
//    fun `deleteSale() should return Completable`() {
//        whenever(apiClient.deleteSale("id")).thenReturn(Completable.complete())
//
//        val testObserver = server.deleteSale("id").test()
//
//        testObserver.assertComplete()
//        verify(apiClient).deleteSale("id")
//        testObserver.dispose()
//
//    }
//
//    @Test
//    fun `get bill items on success`() {
//        val testResponse: BillModel.BillItemListResponse = mock()
//        whenever(apiClient.getBillItems()).thenReturn(Single.just(Response.success(testResponse)))
//
//        val testObserver = server.getBillItems().test()
//
//        testObserver.assertValue(testResponse)
//        verify(apiClient, times(1)).getBillItems()
//    }
//
//    @Test
//    fun `getBillItems() when api call unsuccessful returns error`() {
//        val testResponse: Response<BillModel.BillItemListResponse> = mock()
//        whenever(apiClient.getBillItems()).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.getBillItems().test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).getBillItems()
//    }
//
//    @Test
//    fun `getBillItems() when response body returns null`() {
//        val testResponse: Response<BillModel.BillItemListResponse> = mock()
//        whenever(apiClient.getBillItems()).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(true)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.getBillItems().test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).getBillItems()
//    }
//
//    @Test
//    fun `getBillItems() when api call unsuccessful and response body returns not null`() {
//        val responseBody: BillModel.BillItemListResponse = mock()
//        val testResponse: Response<BillModel.BillItemListResponse> = mock()
//        whenever(apiClient.getBillItems()).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(responseBody)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.getBillItems().test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).getBillItems()
//    }
//
//    @Test
//    fun `add bill item on success`() {
//        val testRequest: BillModel.AddBillItemRequest = mock()
//        val testResponse: BillModel.BillItemResponse = mock()
//        whenever(apiClient.addBillItem(any())).thenReturn(Single.just(Response.success(testResponse)))
//
//        val testObserver = server.addBillItem(testRequest).test()
//
//        testObserver.assertValue(testResponse)
//        verify(apiClient, times(1)).addBillItem(testRequest)
//    }
//
//    @Test
//    fun `addBillItem() when api call unsuccessful returns error`() {
//        val testRequest: BillModel.AddBillItemRequest = mock()
//        val testResponse: Response<BillModel.BillItemResponse> = mock()
//        whenever(apiClient.addBillItem(any())).thenReturn(Single.just(testResponse))
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.addBillItem(testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).addBillItem(testRequest)
//    }
//
//    @Test
//    fun `addBillItem() when api call returns error with error code different than 403`() {
//        val testRequest: BillModel.AddBillItemRequest = mock()
//        val testResponse: Response<BillModel.BillItemResponse> = mock()
//        whenever(apiClient.addBillItem(any())).thenReturn(Single.just(testResponse))
//
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(null)
//
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//        whenever(testError.code).thenReturn(404)
//        whenever(testError.message).thenReturn("error")
//
//        val testObserver = server.addBillItem(testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).addBillItem(testRequest)
//    }
//
//    @Test
//    fun `addBillItem() when api call unsuccessful with error code different than 403 and body is not null`() {
//        val testRequest: BillModel.AddBillItemRequest = mock()
//        val testResponse: Response<BillModel.BillItemResponse> = mock()
//        val responseBody: BillModel.BillItemResponse = mock()
//        whenever(apiClient.addBillItem(any())).thenReturn(Single.just(testResponse))
//
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(responseBody)
//
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//        whenever(testError.code).thenReturn(404)
//        whenever(testError.message).thenReturn("error")
//
//        val testObserver = server.addBillItem(testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).addBillItem(testRequest)
//    }
//
//    @Test
//    fun `addBillItem() when api call returns error with error 403`() {
//        val testRequest: BillModel.AddBillItemRequest = mock()
//        val testResponse: Response<BillModel.BillItemResponse> = mock()
//        whenever(apiClient.addBillItem(any())).thenReturn(Single.just(testResponse))
//
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(null)
//
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//        whenever(testError.code).thenReturn(403)
//        whenever(testError.message).thenReturn("error_403_add_bill_item")
//
//        val testObserver = server.addBillItem(testRequest).test()
//
//        testObserver.assertError {
//            it.message == testError.message
//        }
//        verify(apiClient, times(1)).addBillItem(testRequest)
//    }
//
//    @Test
//    fun `addBillItem() when api call returns error with error 403 and body is not null`() {
//        val testRequest: BillModel.AddBillItemRequest = mock()
//        val testResponse: Response<BillModel.BillItemResponse> = mock()
//        val responseBody: BillModel.BillItemResponse = mock()
//        whenever(apiClient.addBillItem(any())).thenReturn(Single.just(testResponse))
//
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(responseBody)
//
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//        whenever(testError.code).thenReturn(403)
//        whenever(testError.message).thenReturn("error_403_add_bill_item")
//
//        val testObserver = server.addBillItem(testRequest).test()
//
//        testObserver.assertError {
//            it.message == testError.message
//        }
//        verify(apiClient, times(1)).addBillItem(testRequest)
//    }
//
//    @Test
//    fun `update bill item on success`() {
//        val billId = "bill_id"
//        val testRequest: BillModel.UpdateBillItemRequest = mock()
//        val testResponse: BillModel.BillItemResponse = mock()
//        whenever(apiClient.updateBillItem(any(), any())).thenReturn(Single.just(Response.success(testResponse)))
//
//        val testObserver = server.updateBillItem(billId, testRequest).test()
//
//        testObserver.assertValue(testResponse)
//        verify(apiClient, times(1)).updateBillItem(billId, testRequest)
//    }
//
//    @Test
//    fun `updateBillItem() when api call unsuccessful returns error`() {
//        val billId = "bill_id"
//        val testRequest: BillModel.UpdateBillItemRequest = mock()
//        val testResponse: Response<BillModel.BillItemResponse> = mock()
//        whenever(apiClient.updateBillItem(any(), any())).thenReturn(Single.just(testResponse))
//
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.updateBillItem(billId, testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).updateBillItem(billId, testRequest)
//    }
//
//    @Test
//    fun `updateBillItem() when api response body returns null`() {
//        val billId = "bill_id"
//        val testRequest: BillModel.UpdateBillItemRequest = mock()
//        val testResponse: Response<BillModel.BillItemResponse> = mock()
//        whenever(apiClient.updateBillItem(any(), any())).thenReturn(Single.just(testResponse))
//
//        whenever(testResponse.isSuccessful).thenReturn(true)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.updateBillItem(billId, testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).updateBillItem(billId, testRequest)
//    }
//
//    @Test
//    fun `updateBillItem() when api call unsuccessful and api response body returns not null`() {
//        val billId = "bill_id"
//        val testRequest: BillModel.UpdateBillItemRequest = mock()
//        val responseBody: BillModel.BillItemResponse = mock()
//        val testResponse: Response<BillModel.BillItemResponse> = mock()
//        whenever(apiClient.updateBillItem(any(), any())).thenReturn(Single.just(testResponse))
//
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(responseBody)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//
//        val testObserver = server.updateBillItem(billId, testRequest).test()
//
//        testObserver.assertError(testError)
//        verify(apiClient, times(1)).updateBillItem(billId, testRequest)
//    }
//
//    @Test
//    fun `updateBillItem() when api call returns error 403`() {
//        val billId = "bill_id"
//        val testRequest: BillModel.UpdateBillItemRequest = mock()
//        val testResponse: Response<BillModel.BillItemResponse> = mock()
//        whenever(apiClient.updateBillItem(billId, testRequest)).thenReturn(Single.just(testResponse))
//
//        whenever(testResponse.isSuccessful).thenReturn(false)
//        whenever(testResponse.body()).thenReturn(null)
//        val testError: Error = mock()
//        every { Error.parse(testResponse) } returns testError
//        whenever(testError.code).thenReturn(403)
//        whenever(testError.message).thenReturn("some_error")
//        val exception = Exception(testError.message)
//
//        val testObserver = server.updateBillItem(billId, testRequest).test()
//
//        testObserver.assertError {
//            it.message == testError.message
//        }
//        verify(apiClient, times(1)).updateBillItem(billId, testRequest)
//    }
// }
