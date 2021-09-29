package `in`.okcredit.sales_ui.ui.add_bill_items

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_ui.usecase.AddSale
import `in`.okcredit.sales_ui.usecase.BillItemOperations
import `in`.okcredit.sales_ui.usecase.GetBillItems
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

class AddBillItemsViewModelTest {
    private val initialState = AddBillItemsContract.State()
    private val context: Context = mock()
    private val getBillItems: GetBillItems = mock()
    private val billItemOperations: BillItemOperations = mock()
    private val addSale: AddSale = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    @Before
    fun setUp() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    private fun createViewModel(
        initialState: AddBillItemsContract.State = AddBillItemsContract.State(),
        billItems: BillModel.BillItems? = null
    ): AddBillItemsViewModel {
        return AddBillItemsViewModel(
            initialState = initialState,
            getBillItems = getBillItems,
            context = context,
            billItems = billItems,
            billItemsOperations = billItemOperations,
            addSale = { addSale },
            getActiveBusinessId = { getActiveBusinessId }
        )
    }

    private fun generateBilledItem(billItems: List<BillModel.BillItem>): BillModel.BilledItems {
        val items = mutableListOf<BillModel.BilledItem>()
        billItems.forEach {
            items.add(BillModel.BilledItem(it.id, it.quantity))
        }
        val total = billItems.sumByDouble { it.rate * it.quantity }
        return BillModel.BilledItems(total.toString(), items)
    }

    @Test
    fun `on load inventory items`() {
        val viewModel = createViewModel()
        val testResponse = BillModel.BillItemListResponse(listOf())
        val testRequest = GetBillItems.Request("")
        whenever(getBillItems.execute(testRequest))
            .thenReturn(Observable.just(Result.Success(testResponse)))
        val testStateObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(AddBillItemsContract.Intent.Load))

        testStateObserver.values().contains(initialState)
//        verify(getBillItems).execute(testRequest)
    }

    @Test
    fun `on load bill items`() {
        val testBillItem1 = BillModel.BillItem("a", "item 1", 10.0, 1.0)
        val testBillItem2 = BillModel.BillItem("b", "item 2", 100.0, 3.0)
        val billItems = BillModel.BillItems(listOf(testBillItem1, testBillItem2))
        val viewModel = createViewModel(billItems = billItems)

        val billedItems = generateBilledItem(billItems.items)
        val testBillOpRequest = BillItemOperations.Request(AddBillItemsContract.Intent.Load, listOf(), billItems.items)
        val testBillOpResponse = BillItemOperations.Response(billedItems, billItems.items, listOf())
        whenever(billItemOperations.execute(testBillOpRequest)).thenReturn(
            Observable.just(
                Result.Success(
                    testBillOpResponse
                )
            )
        )

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(AddBillItemsContract.Intent.Load))
        testObserver.values().contains(AddBillItemsContract.State(billedItems = billedItems))
        testObserver.dispose()
    }

//    @Test
//    fun `on add bill items`() {
//        val testBillItem1 = BillModel.BillItem("a", "item 1", 10.0, 1.0)
//        val testBillItem2 = BillModel.BillItem("b", "item 2", 100.0, 3.0)
//        val billItems = BillModel.BillItems(listOf(testBillItem1))
//        val billItems2 = BillModel.BillItems(listOf(testBillItem1, testBillItem2))
//
//        val billedItemsBefore = generateBilledItem(billItems.items)
//        val billedItemsResult = generateBilledItem(billItems2.items)
//
//        val initialState = AddBillItemsContract.State(
//            billedItems = billedItemsBefore
//        )
//        val finalState = AddBillItemsContract.State(
//            billedItems = billedItemsResult
//        )
//        val testIntent = AddBillItemsContract.Intent.AddBillItemIntent(testBillItem2)
//        val viewModel = createViewModel(initialState)
//
//
//        val testObserver = viewModel.state().test()
//        viewModel.attachIntents(Observable.just(testIntent))
//        val intent = getResultIntent(viewModel, testIntent)
//        testObserver.assertValue(finalState)
//        testObserver.dispose()
//    }
//
//
//    @Test
//    fun `on update bill items`() {
//        val testBillItem1 = BillModel.BillItem("a", "item 1", 10.0, 1.0)
//        val testBillItem1Updated = BillModel.BillItem("a", "item 1", 100.0, 2.0)
//        val billItems = BillModel.BillItems(listOf(testBillItem1))
//        val billItemsUpdated = BillModel.BillItems(listOf(testBillItem1Updated))
//
//        val billedItemsBefore = generateBilledItem(billItems.items)
//        val billedItemsUpdated = generateBilledItem(billItemsUpdated.items)
//        val initialState = AddBillItemsContract.State(
//            billedItems = billedItemsBefore
//        )
//        val finalState = AddBillItemsContract.State(
//            billedItems = billedItemsUpdated
//        )
//
//        val viewModel = createViewModel(initialState)
//        val testObserver = viewModel.state().test()
//        viewModel.attachIntents(Observable.just(AddBillItemsContract.Intent.UpdateBillItemIntent(testBillItem1Updated)))
//        testObserver.values().contains(
//            AddBillItemsContract.State(
//                billedItems = billedItemsBefore
//            )
//        )
//        testObserver.values().contains(
//            AddBillItemsContract.State(
//                billedItems = billedItemsUpdated
//            )
//        )
//
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `on remove bill items`() {
//        val testBillItem1 = BillModel.BillItem("a", "item 1", 10.0, 1.0)
//        val testBillItem2 = BillModel.BillItem("b", "item 2", 100.0, 3.0)
//        val billItems = BillModel.BillItems(listOf(testBillItem1, testBillItem2))
//        val billItemsUpdated = BillModel.BillItems(listOf(testBillItem1))
//
//        val billedItemsBefore = generateBilledItem(billItems.items)
//        val billedItemsUpdated = generateBilledItem(billItemsUpdated.items)
//
//        val initialState = AddBillItemsContract.State(
//            billedItems = billedItemsBefore
//        )
//        val finalState = AddBillItemsContract.State(
//            billedItems = billedItemsUpdated
//        )
//
//        val viewModel = createViewModel(initialState)
//
//        val testScheduler = TestScheduler()
//
//        viewModel.attachIntents(Observable.just(AddBillItemsContract.Intent.RemoveBillItemIntent(testBillItem2)))
//        val t = viewModel.intents().observeOn(intentScheduler).subscribeOn(testScheduler).test()
//        val testObserver = viewModel.state().subscribeOn(testScheduler).observeOn(testScheduler).test()
//        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
//        t.assertValue(AddBillItemsContract.Intent.RemoveBillItemIntent(testBillItem2))
// //        testObserver.assertValue(finalState)
//        testObserver.dispose()
//    }

    private fun getResultIntent(viewModel: AddBillItemsViewModel, testIntent: AddBillItemsContract.Intent): UserIntent {
        val testIntentObserver = viewModel.intents().test(true)
        testIntentObserver.onNext(testIntent)
        return testIntentObserver.values().last()
    }
}
