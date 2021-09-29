package `in`.okcredit.sales_ui.ui.add_bill_dialog

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_ui.usecase.AddBillItem
import `in`.okcredit.sales_ui.usecase.UpdateBillItem
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test

class AddBillViewModelTest {

    private val initialState = AddBillContract.State()
    private val navigator: AddBillContract.Navigator = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val addBillItem: AddBillItem = mock()
    private val updateBillItem: UpdateBillItem = mock()
    private val context: Context = mock()

    private val testScheduler = TestScheduler()

    private fun createViewModel(): AddBillViewModel {
        return AddBillViewModel(
            initialState = initialState,
            navigator = navigator,
            addBillItem = addBillItem,
            updateBillItem = updateBillItem,
            context = context,
            getActiveBusinessId = { getActiveBusinessId }
        )
    }

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
    }

    @Test
    fun `on add bill item`() {
        val viewModel = createViewModel()
        val testAddBillItem = BillModel.AddBillItem("test", 0.0)
        val testRequest = BillModel.AddBillItemRequest(testAddBillItem)
        val testBillItem = BillModel.BillItem("bill_id", "name", 0.0, 1.0)
        val testBillItemResponse = BillModel.BillItemResponse(testBillItem)
        val testAddBillItemRequest = AddBillItem.Request("", testRequest)
        val testIntent = AddBillContract.Intent.AddBillItemIntent(testRequest)
        whenever(addBillItem.execute(testAddBillItemRequest))
            .thenReturn(Observable.just(Result.Success(testBillItemResponse)))

        val testStateObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(testIntent))

        testStateObserver.values().contains(initialState)

        verify(addBillItem).execute(testAddBillItemRequest)
        verify(navigator).onBillItemAdded(testBillItemResponse.item)
        testStateObserver.dispose()
    }

    @Test
    fun `on update bill item`() {
        val viewModel = createViewModel()
        val testRequest = BillModel.UpdateBillItemRequest(BillModel.AddBillItem("name", 1.0), listOf("name", "rate"))
        val testBillItem = BillModel.BillItem("", "name", 1.0, 1.0)
        val testBillItemResponse = BillModel.BillItemResponse(testBillItem)
        val testUpdateBillItemRequest = UpdateBillItem.Request("", testRequest)
        val testIntent = AddBillContract.Intent.UpdateBillItemIntent(
            testUpdateBillItemRequest.billId,
            testUpdateBillItemRequest.updateBillItemRequest
        )
        whenever(updateBillItem.execute(testUpdateBillItemRequest))
            .thenReturn(Observable.just(Result.Success(testBillItemResponse)))

        val testStateObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(testIntent))

        testStateObserver.values().contains(initialState)

        verify(updateBillItem).execute(testUpdateBillItemRequest)
        verify(navigator).onBillItemUpdated(testBillItemResponse.item)
        testStateObserver.dispose()
    }

    @Test
    fun `on update bill name`() {
        val viewModel = createViewModel()
        val testRequest = BillModel.UpdateBillItemRequest(BillModel.AddBillItem("updateName", 1.0), listOf("name"))
        val testBillItem = BillModel.BillItem("", "updateName", 0.0, 1.0)
        val testBillItemResponse = BillModel.BillItemResponse(testBillItem)
        val testUpdateBillItemRequest = UpdateBillItem.Request("bill_id", testRequest)
        val testIntent = AddBillContract.Intent.UpdateBillItemIntent(
            testUpdateBillItemRequest.billId,
            testUpdateBillItemRequest.updateBillItemRequest
        )
        whenever(updateBillItem.execute(testUpdateBillItemRequest))
            .thenReturn(Observable.just(Result.Success(testBillItemResponse)))

        val testStateObserver = viewModel.state().test()

        viewModel.attachIntents(Observable.just(testIntent))

        testStateObserver.values().contains(initialState)

        verify(updateBillItem).execute(testUpdateBillItemRequest)
        verify(navigator).onBillItemUpdated(testBillItemResponse.item)
        testStateObserver.dispose()
    }

    @Test
    fun `on update bill rate`() {
        val viewModel = createViewModel()
        val testRequest = BillModel.UpdateBillItemRequest(BillModel.AddBillItem("name", 1.0), listOf("rate"))
        val testBillItem = BillModel.BillItem("bill_id", "", 1.0, 1.0)
        val testBillItemResponse = BillModel.BillItemResponse(testBillItem)
        val testUpdateBillItemRequest = UpdateBillItem.Request("bill_id", testRequest)
        val testIntent = AddBillContract.Intent.UpdateBillItemIntent(
            testUpdateBillItemRequest.billId,
            testUpdateBillItemRequest.updateBillItemRequest
        )
        whenever(updateBillItem.execute(testUpdateBillItemRequest))
            .thenReturn(Observable.just(Result.Success(testBillItemResponse)))

        val testStateObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(testIntent))

        testStateObserver.values().contains(initialState)

        verify(updateBillItem).execute(testUpdateBillItemRequest)
        verify(navigator).onBillItemUpdated(testBillItemResponse.item)
        testStateObserver.dispose()
    }

    @Test
    fun `on set item name`() {
        val viewModel = createViewModel()
        val testItemName = "name"
        val testIntent = AddBillContract.Intent.SetNameIntent(testItemName)
        val expectedState = AddBillContract.State(name = testItemName)

        val testStateObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(testIntent))
        val intent = getResultIntent(viewModel, testIntent) as AddBillContract.Intent.SetNameIntent
        testStateObserver.onNext(AddBillContract.State(name = intent.name))
        testStateObserver.values().contains(expectedState)
        testStateObserver.dispose()
    }

    @Test
    fun `on set item rate`() {
        val viewModel = createViewModel()
        val testRate = "10.0"
        val testIntent = AddBillContract.Intent.SetRateIntent(testRate)
        val expectedState = AddBillContract.State(rate = testRate.toDouble())
        val testScheduler = TestScheduler()
        val testStateObserver = viewModel.state().subscribeOn(testScheduler).observeOn(testScheduler).test()
        viewModel.attachIntents(Observable.just(testIntent))
        val intent = getResultIntent(viewModel, testIntent) as AddBillContract.Intent.SetRateIntent
        testStateObserver.onNext(AddBillContract.State(rate = intent.rate.toDouble()))
        testStateObserver.assertValue(expectedState)
        testStateObserver.dispose()
    }

    @Test
    fun `on set item quantity`() {
        val viewModel = createViewModel()
        val testQuantity = "10.0"
        val testIntent = AddBillContract.Intent.SetQuantityIntent(testQuantity)
        val expectedState = AddBillContract.State(quantity = testQuantity.toDouble())

        val testStateObserver = viewModel.state().subscribeOn(testScheduler).observeOn(testScheduler).test()
        val intent = getResultIntent(viewModel, testIntent) as AddBillContract.Intent.SetQuantityIntent
        testStateObserver.onNext(AddBillContract.State(quantity = intent.quantity.toDouble()))
        testStateObserver.assertValue(expectedState)
        testStateObserver.dispose()
    }

    @Test
    fun `on set data`() {
        val viewModel = createViewModel()
        val testBillItem = BillModel.BillItem("bill_id", "name", 0.0, 1.0)

        val testIntent = AddBillContract.Intent.SetDataIntent(testBillItem)
        val expectedState =
            AddBillContract.State(name = testBillItem.name, rate = testBillItem.rate, quantity = testBillItem.quantity)
        val testStateObserver = viewModel.state().subscribeOn(testScheduler).observeOn(testScheduler).test()
        val intent = getResultIntent(viewModel, testIntent) as AddBillContract.Intent.SetDataIntent
        testStateObserver.onNext(
            AddBillContract.State(
                name = intent.billItem!!.name,
                rate = intent.billItem!!.rate,
                quantity = intent.billItem!!.quantity
            )
        )
        testStateObserver.assertValue(expectedState)
        testStateObserver.dispose()
    }

    private fun getResultIntent(viewModel: AddBillViewModel, testIntent: AddBillContract.Intent): UserIntent {
        val testIntentObserver = viewModel.intents().test(true)
        testIntentObserver.onNext(testIntent)
        return testIntentObserver.values().last()
    }
}
