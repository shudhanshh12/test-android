package tech.okcredit.home.ui.home.supplier

import `in`.okcredit.supplier.home.tab.GetSupplierSortType
import `in`.okcredit.supplier.home.tab.SetSupplierSortType
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import merchant.okcredit.accounting.contract.HomeSortType
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class SupplierSortViewModelTest {

    private val initialState = SupplierSortContract.State()
    private val getSupplierSortType: GetSupplierSortType = mock()
    private val setSupplierSortType: SetSupplierSortType = mock()
    private lateinit var viewModel: SupplierSortViewModel
    private lateinit var testScheduler: TestScheduler
    private lateinit var stateObserver: TestObserver<SupplierSortContract.State>
    private lateinit var viewEventObserver: TestObserver<SupplierSortContract.ViewEvent>

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler

        viewModel = SupplierSortViewModel(initialState, { getSupplierSortType }, { setSupplierSortType })
        stateObserver = viewModel.state().test()
        viewEventObserver = viewModel.viewEvent().test()
    }

    @After
    fun teardown() {
        stateObserver.dispose()
        viewEventObserver.dispose()
    }

    @Test
    fun `should update sort type in UI state on load intent`() {
        whenever(getSupplierSortType.execute()).thenReturn(Observable.just(HomeSortType.NAME))

        viewModel.attachIntents(Observable.just(SupplierSortContract.Intent.Load))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        stateObserver.assertValues(initialState, initialState.copy(HomeSortType.NAME))
    }

    @Test
    fun `should update sort type in UI state on SelectSortType intent`() {
        whenever(setSupplierSortType.execute(HomeSortType.NAME)).thenReturn(Single.just(HomeSortType.NAME))

        viewModel.attachIntents(Observable.just(SupplierSortContract.Intent.SelectSortType(HomeSortType.NAME)))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        stateObserver.assertValues(initialState, initialState.copy(HomeSortType.NAME))
        viewEventObserver.assertValue(SupplierSortContract.ViewEvent.ApplySort)
    }
}
