package `in`.okcredit.sales_ui.ui.bill_summary

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.sales_ui.usecase.DeleteSale
import `in`.okcredit.sales_ui.usecase.GetCashSaleItem
import `in`.okcredit.sales_ui.usecase.UpdateSale
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.auth.AccessTokenProvider

class BillSummaryViewModelTest {

    private val initialState = BillSummaryContract.State()
    private val getCashSaleItem: GetCashSaleItem = mock()
    private val updateSale: UpdateSale = mock()
    private val deleteSale: DeleteSale = mock()
    private val context: Context = mock()
    private val tokenProvider: AccessTokenProvider = mock()
    private val getActiveBusiness: GetActiveBusiness = mock()

    @Before
    fun setUp() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    private fun createViewModel(saleId: String = "", editable: Boolean = false): BillSummaryViewModel {
        return BillSummaryViewModel(
            initialState = initialState,
            getCashSaleItem = { getCashSaleItem },
            updateSale = { updateSale },
            deleteSale = { deleteSale },
            context = { context },
            tokenProvider = { tokenProvider },
            getActiveBusiness = { getActiveBusiness },
            saleId = saleId,
            isEditable = editable,
        )
    }

    @Test
    fun `set sale id on load`() {
        val saleId = "sale_id"
        val viewModel = createViewModel(saleId)
        val testIntent = BillSummaryContract.Intent.Load
        val business: Business = mock()
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(business))
        whenever(tokenProvider.getAccessToken()).thenReturn("token")
        val testStateObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(testIntent))
        testStateObserver.values().contains(BillSummaryContract.State(saleId = saleId))

        testStateObserver.dispose()
    }

    @Test
    fun `set editable on load`() {
        val viewModel = createViewModel(editable = true)
        val testIntent = BillSummaryContract.Intent.Load
        val business: Business = mock()
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(business))
        whenever(tokenProvider.getAccessToken()).thenReturn("token")
        val testStateObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(testIntent))
        testStateObserver.values().contains(BillSummaryContract.State(isEditable = true))

        testStateObserver.dispose()
    }

    @Test
    fun `getMerchant on success`() {
        val viewModel = createViewModel()
        val testIntent = BillSummaryContract.Intent.Load
        val business: Business = mock()
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(business))
        whenever(tokenProvider.getAccessToken()).thenReturn("token")
        val testStateObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(testIntent))
        testStateObserver.values().contains(initialState)

        testStateObserver.dispose()
    }

    @Test
    fun `getCashSaleItem on success`() {
        val viewModel = createViewModel()
        val testRequest = GetCashSaleItem.Request("sale_id")
        val testResponse: Models.SaleItemResponse = mock()
        val testIntent = BillSummaryContract.Intent.Load
        val business: Business = mock()
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(business))
        whenever(getCashSaleItem.execute(testRequest)).thenReturn(Observable.just(Result.Success(testResponse)))
        whenever(tokenProvider.getAccessToken()).thenReturn("token")
        val testStateObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(testIntent))
        testStateObserver.values().contains(BillSummaryContract.State(sale = testResponse.sale))

        testStateObserver.dispose()
    }

    @Test
    fun `getAuthToken on success`() {
        val viewModel = createViewModel()
        val testIntent = BillSummaryContract.Intent.Load
        val business: Business = mock()
        val testToken = "token"
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(business))
        whenever(tokenProvider.getAccessToken()).thenReturn(testToken)
        val testStateObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(testIntent))
        testStateObserver.values().contains(BillSummaryContract.State(authToken = testToken))

        testStateObserver.dispose()
    }

    @Test
    fun `updateSale on success`() {
        val viewModel = createViewModel()
        val updateSaleItemRequest: Models.UpdateSaleItemRequest = mock()
        val testRequest = UpdateSale.Request("sale_id", updateSaleItemRequest)
        val testResponse: Models.SaleItemResponse = mock()
        val testIntent = BillSummaryContract.Intent.UpdateBillingDataIntent(updateSaleItemRequest)
        whenever(updateSale.execute(testRequest)).thenReturn(Observable.just(Result.Success(testResponse)))
        val testStateObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(testIntent))
        testStateObserver.values().contains(BillSummaryContract.State(sale = testResponse.sale))

        testStateObserver.dispose()
    }

    @Test
    fun `deleteSale on success`() {
        val viewModel = createViewModel()
        val saleId = "sale_id"
        val testRequest = DeleteSale.Request(saleId)
        val testIntent = BillSummaryContract.Intent.DeleteSaleIntent(saleId)
        whenever(deleteSale.execute(testRequest)).thenReturn(Observable.just(Result.Success(Unit)))
        val testStateObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(testIntent))
        testStateObserver.values().contains(initialState)

        testStateObserver.dispose()
    }

    @Test
    fun `show Loader`() {
        val viewModel = createViewModel()
        val testIntent = BillSummaryContract.Intent.ShowLoaderIntent(true)

        val testStateObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(testIntent))
        val intent = getResultIntent(viewModel, testIntent) as BillSummaryContract.Intent.ShowLoaderIntent
        testStateObserver.onNext(BillSummaryContract.State(isLoading = intent.canShow))
        testStateObserver.values().contains(BillSummaryContract.State(isLoading = true))

        testStateObserver.dispose()
    }

    private fun getResultIntent(viewModel: BillSummaryViewModel, testIntent: BillSummaryContract.Intent): UserIntent {
        val testIntentObserver = viewModel.intents().test(true)
        testIntentObserver.onNext(testIntent)
        return testIntentObserver.values().last()
    }
}
