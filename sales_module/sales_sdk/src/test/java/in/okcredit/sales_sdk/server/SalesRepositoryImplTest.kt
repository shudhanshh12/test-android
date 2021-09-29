package `in`.okcredit.sales_sdk.server

import `in`.okcredit.sales_sdk.SalesRepositoryImpl
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_sdk.models.Models
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Before
import org.junit.Test

class SalesRepositoryImplTest {
    private val salesRemoteSource: SalesRemoteSource = mock()
    lateinit var salesRepositoryImpl: SalesRepositoryImpl

    companion object {
        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")

        val updateSale = Models.UpdateSale(name = "name", mobile = "mobile", saleDate = dt)
        val updateSalesItemRequest = Models.UpdateSaleItemRequest(updateSale, listOf("maskList"))
        val businessId = "business-id"
        val sale = Models.Sale(
            amount = 0.0,
            createdAt = dt,
            deletedAt = null,
            id = "saleId",
            note = "",
            buyerName = null,
            buyerMobile = null,
            updatedAt = null,
            saleDate = dt,
            billedItems = null
        )
        val saleItemResponse = Models.SaleItemResponse(sale)

        val salesListResponse = Models.SalesListResponse(
            salesList = listOf(),
            totalAmount = 0.0,
            totalNumberOfSales = 0.0,
            startDate = null,
            endDate = null
        )
        val addSale = Models.AddSale(
            merchantId = "",
            amount = 0.0,
            note = "",
            saleDate = dt,
            buyerName = null,
            buyerMobile = null,
            billedItems = null
        )
        val addBillItem = BillModel.AddBillItem(name = "", rate = 0.0)
        val addSaleResponse = Models.AddSaleResponse(sale)
        val salesRequest = Models.SaleRequestModel(addSale)
        val billItemListRequest = BillModel.BillItemListResponse(items = listOf())
        val addBillItemRequest = BillModel.AddBillItemRequest(addBillItem)
        val billItem = BillModel.BillItem(id = "", name = "", rate = 0.0, quantity = 0.0)
        val billItemResponse = BillModel.BillItemResponse(billItem)
        val updateBillItemRequest = BillModel.UpdateBillItemRequest(addBillItem, listOf())
    }

    @Before
    fun setUp() {
        salesRepositoryImpl = SalesRepositoryImpl(salesRemoteSource)
    }

    @Test
    fun `test UpdateSale should retrun salesItemResponse`() {
        // given
        whenever(salesRemoteSource.updateSale("salesId", updateSalesItemRequest, businessId)).thenReturn(
            Single.just(
                saleItemResponse
            )
        )

        // when
        val result = salesRepositoryImpl.updateSale("salesId", updateSalesItemRequest, businessId).test()

        // then
        result.assertValue {
            it.equals(saleItemResponse)
        }
    }

    @Test
    fun `test getSales should return SalesListResponse`() {
        // given
        whenever(salesRemoteSource.getSales(0L, 10L, businessId)).thenReturn(Single.just(salesListResponse))

        // when
        val result = salesRepositoryImpl.getSales(0L, 10L, businessId).test()

        // then
        result.assertValue {
            it.equals(salesListResponse)
        }
    }

    @Test
    fun `test submitSale return AddSaleResponse`() {
        // given
        whenever(salesRemoteSource.submit(salesRequest, businessId)).thenReturn(Single.just(addSaleResponse))

        // when
        val result = salesRepositoryImpl.submitSale(salesRequest, businessId).test()

        // then
        result.assertValue {
            it.equals(addSaleResponse)
        }
    }

    @Test
    fun deleteSaleTest() {
        // given
        whenever(salesRemoteSource.deleteSale("id", businessId)).thenReturn(Completable.complete())

        // when
        val result = salesRepositoryImpl.deleteSale("id", businessId).test()

        // then
        result.assertComplete()
    }

    @Test
    fun getBillItemsTest() {
        // given
        whenever(salesRemoteSource.getBillItems(businessId)).thenReturn(Single.just(billItemListRequest))

        // when
        val result = salesRepositoryImpl.getBillItems(businessId).test()

        // then
        result.assertValue {
            it.equals(billItemListRequest)
        }
    }

    @Test
    fun addBillItemTest() {
        // given
        whenever(
            salesRemoteSource.addBillItem(
                addBillItemRequest,
                businessId
            )
        ).thenReturn(Single.just(billItemResponse))

        // when
        val result = salesRepositoryImpl.addBillItem(addBillItemRequest, businessId).test()

        // then
        result.assertValue {
            it.equals(billItemResponse)
        }
    }

    @Test
    fun updateBillItemTest() {
        // given
        whenever(salesRemoteSource.updateBillItem("billId", updateBillItemRequest, businessId)).thenReturn(
            Single.just(
                billItemResponse
            )
        )

        // when
        val result = salesRepositoryImpl.updateBillItem("billId", updateBillItemRequest, businessId).test()

        // then
        result.assertValue {
            it.equals(billItemResponse)
        }
    }
}
