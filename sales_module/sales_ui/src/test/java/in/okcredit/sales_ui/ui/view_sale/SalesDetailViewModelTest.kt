package `in`.okcredit.sales_ui.ui.view_sale

import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.sales_ui.usecase.DeleteSale
import `in`.okcredit.sales_ui.usecase.GetCashSaleItem
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

class SalesDetailViewModelTest {

    private val initialState = SalesDetailContract.State()
    private val navigator: SalesDetailContract.Navigator = mock()
    private val deleteSale: DeleteSale = mock()
    private val getSale: GetCashSaleItem = mock()
    private val context: Context = mock()
    private val dateTime: DateTime = mock()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
    }

    private fun createViewModel(saleId: String = ""): SalesDetailViewModel {
        return SalesDetailViewModel(
            initialState = initialState,
            saleId = saleId,
            navigator = navigator,
            deleteSale = deleteSale,
            getSale = getSale,
            context = context
        )
    }

    @Test
    fun `get sale success on load `() {
        val saleId = "sale_id"
        val testRequest = GetCashSaleItem.Request(saleId)
        val testSaleItem = Models.SaleItems(listOf())
        val testSale =
            Models.Sale(0.0, dateTime, dateTime, "sale_id", "note", "name", "mobile", dateTime, dateTime, testSaleItem)
        val viewModel = createViewModel(saleId)
        whenever(getSale.execute(testRequest))
            .thenReturn(Observable.just(Result.Success(Models.SaleItemResponse(testSale))))

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(SalesDetailContract.Intent.Load))

        testObserver.values().contains(initialState)

        verify(getSale).execute(testRequest)
    }
}
