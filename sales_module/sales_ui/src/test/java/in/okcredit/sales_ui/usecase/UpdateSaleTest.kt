package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Test

class UpdateSaleTest {
    private val sales: SalesRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val updateSale = UpdateSale(sales, { getActiveBusinessId })

    companion object {
        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")

        val updateSaleItemRequest = Models.UpdateSaleItemRequest(Models.UpdateSale(), listOf("mask"))
        val sale = Models.Sale(
            amount = 120.toDouble(),
            createdAt = dt,
            id = "salesId",
            note = "note",
            saleDate = dt,
            buyerName = "name",
            buyerMobile = "mobile",
            billedItems = null
        )
        val businessId = "business-id"
    }

    @Test
    fun execute() {
        // given
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(
            sales.updateSale(
                "salesId",
                updateSaleItemRequest,
                businessId
            ),
        ).thenReturn(Single.just(Models.SaleItemResponse(sale)))

        // when
        val result = updateSale.execute(UpdateSale.Request("salesId", updateSaleItemRequest)).test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success(Models.SaleItemResponse(sale))
        )
    }
}
