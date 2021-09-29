package `in`.okcredit.frontend.ui.expense_manager

import `in`.okcredit.backend._offline.usecase.SubmitFeedbackImpl
import `in`.okcredit.backend.contract.Features
import `in`.okcredit.expense.models.Models
import `in`.okcredit.frontend.usecase.DeleteExpense
import `in`.okcredit.frontend.usecase.ExpenseOnBoarding
import `in`.okcredit.frontend.usecase.GetExpenses
import `in`.okcredit.shared.usecase.Result
import com.google.common.truth.Truth.assertThat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import junit.framework.Assert.assertTrue
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import java.util.concurrent.TimeUnit

class ExpenseManagerViewModelTest {

    private lateinit var viewModel: ExpenseManagerViewModel

    private val initialState: ExpenseManagerContract.State = mock()

    private val ab: AbRepository = mock()

    private val expenseOnBoarding: ExpenseOnBoarding = mock()

    private val getExpenses: GetExpenses = mock()

    private val deleteExpense: DeleteExpense = mock()

    private val submitFeedBack: SubmitFeedbackImpl = mock()

    private val navigator: ExpenseManagerContract.Navigator = mock()

    private lateinit var testScheduler: TestScheduler

    @Before
    fun setup() {

        testScheduler = TestScheduler()

        viewModel = ExpenseManagerViewModel(
            initialState = initialState,
            checkNetworkHealth = mock(),
            getActiveBusinessId = mock(),
            navigator = navigator,
            getExpenses = getExpenses,
            deleteExpense = deleteExpense,
            ab = ab,
            rxPreference = mock(),
            filterExpense = mock(),
            expenseOnBoarding = expenseOnBoarding,
            submitFeedback = submitFeedBack
        )

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `ab add expense is enabled`() {
        val feature = "expense_show_add_expense"
        whenever(ab.isFeatureEnabled(feature)).thenReturn(Observable.just(true))

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.Load))

        testObserver.values().contains(initialState.copy(showAddexpense = true))
        testObserver.dispose()
    }

    @Test
    fun `ab summary view is enabled`() {
        val feature = "expense_summary_view_ab"
        assertTrue(Features.EXPENSE_SUMMARY_VIEW_AB == feature)
        whenever(ab.isFeatureEnabled(feature)).thenReturn(Observable.just(true))

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.Load))

        testObserver.values().contains(initialState.copy(isSummaryViewAbEnabled = true))
        testObserver.dispose()
    }

    @Test
    fun `expense onBoarding`() {
        whenever(expenseOnBoarding.execute(Unit)).thenReturn(Observable.just(Result.Success(ExpenseManagerContract.OnBoardingVariant.v2)))

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.Load))

        testObserver.values()
            .contains(initialState.copy(onBoardingVariant = ExpenseManagerContract.OnBoardingVariant.v2))
        testObserver.dispose()
    }

    @Test
    fun `get all expenses on success`() {
        val testRequest = GetExpenses.Request("merchant_id")
        val testResponse =
            Models.ExpenseListResponse(expenseList = listOf(), totalAmount = 10.0, startDate = mock(), endDate = mock())
        whenever(getExpenses.execute(testRequest)).thenReturn(Observable.just(Result.Success(testResponse)))

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.GetAllExpenses))

        testObserver.values()
            .contains(
                initialState.copy(
                    list = testResponse.expenseList,
                    totalAmount = testResponse.totalAmount,
                    startDate = testResponse.startDate,
                    endDate = testResponse.endDate,
                    isLoading = false,
                    networkError = false
                )
            )
        testObserver.dispose()
    }

    @Test
    fun `get expenses on success`() {
        val startDate: DateTime = mock()
        val endDate: DateTime = mock()
        val testRequest: GetExpenses.Request = mock()
        val testResponse =
            Models.ExpenseListResponse(expenseList = listOf(), totalAmount = 10.0, startDate = mock(), endDate = mock())
        whenever(getExpenses.execute(testRequest)).thenReturn(Observable.just(Result.Success(testResponse)))

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.GetExpenses(startDate, endDate)))

        testObserver.values()
            .contains(
                initialState.copy(
                    list = testResponse.expenseList,
                    totalAmount = testResponse.totalAmount,
                    startDate = testResponse.startDate,
                    endDate = testResponse.endDate,
                    isLoading = false,
                    networkError = false
                )
            )
        testObserver.dispose()
    }

    @Test
    fun `delete expenses on success`() {
        whenever(deleteExpense.execute("id")).thenReturn(Observable.just(Result.Success(Unit)))

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.DeleteExpense("id")))

        testObserver.values()
            .contains(
                initialState.copy(
                    canShowDeleteConfirmDialog = false,
                    canShowDeleteLayout = false
                )
            )
        testObserver.dispose()
    }

    @Test
    fun `change filter to show all`() {

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.ChangeFilter(ExpenseManagerContract.Filter.ALL)))

        testObserver.values()
            .contains(
                initialState.copy(
                    filter = ExpenseManagerContract.Filter.ALL
                )
            )

        verify(navigator).showAll()
//        assertThat(testObserver.values().last() == initialState.copy(filter = ExpenseManagerContract.Filter.ALL)).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `change filter to show today`() {

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.ChangeFilter(ExpenseManagerContract.Filter.TODAY)))

        testObserver.values()
            .contains(
                initialState.copy(
                    filter = ExpenseManagerContract.Filter.TODAY
                )
            )

        verify(navigator).showToday()
        testObserver.dispose()
    }

    @Test
    fun `change filter to show this month`() {
        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.ChangeFilter(ExpenseManagerContract.Filter.THIS_MONTH)))

        testScheduler.advanceTimeBy(33, TimeUnit.SECONDS)
//  TODO(Commented TestCase)
//        testObserver.values()
//            .contains(
//                initialState.copy(
//                    filter = ExpenseManagerContract.Filter.THIS_MONTH
//                )
//            )
//
//        verify(navigator).showThisMonth()
        testObserver.dispose()
    }

    @Test
    fun `change filter to show last month`() {
        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.ChangeFilter(ExpenseManagerContract.Filter.LAST_MONTH)))

        testObserver.values()
            .contains(
                initialState.copy(
                    filter = ExpenseManagerContract.Filter.LAST_MONTH
                )
            )

        verify(navigator).showLastMonth()
        testObserver.dispose()
    }

    @Test
    fun `set date range`() {
        val startDate: DateTime = mock()
        val endDate: DateTime = mock()

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.SetDateRangeIntent(startDate, endDate)))

        testObserver.values()
            .contains(
                initialState.copy(
                    filter = ExpenseManagerContract.Filter.DATE_RANGE
                )
            )

        verify(navigator).showForSelectedRange(startDate, endDate)
        testObserver.dispose()
    }

    @Test
    fun `set can show add expense education`() {

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.SetFirstAddExpenseEducation(true)))

        testObserver.values()
            .contains(
                initialState.copy(
                    canShowAddExpenseEducation = true
                )
            )
        testObserver.dispose()
    }

    @Test
    fun `on add expense clicked`() {

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.OnAddExpenseClicked))

        assertThat(testObserver.values().last() == initialState)

        verify(navigator).goToAddExpenseScreen()
        testObserver.dispose()
    }

    @Test
    fun `set show delete layout`() {

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.ShowDeleteLayout(mock())))

        assertThat(testObserver.values().last() == initialState.copy(canShowDeleteLayout = true))

        testObserver.dispose()
    }

    @Test
    fun `set hide delete layout`() {

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.HideDeleteLayout))

        assertThat(testObserver.values().last() == initialState.copy(canShowDeleteLayout = false))

        testObserver.dispose()
    }

    @Test
    fun `set delete confirm dialog`() {

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.ShowDeleteConfirmDialog(true)))

        assertThat(testObserver.values().last() == initialState.copy(canShowDeleteConfirmDialog = true))

        testObserver.dispose()
    }

    @Test
    fun `submit feedback`() {

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(ExpenseManagerContract.Intent.SubmitFeedBack("msg")))

        assertThat(testObserver.values().last() == initialState)

        verify(submitFeedBack).schedule("msg", 7)
        testObserver.dispose()
    }
}
