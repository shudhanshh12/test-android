package `in`.okcredit.frontend.ui.add_expense

import `in`.okcredit.backend.contract.Features
import `in`.okcredit.expense.models.Models
import `in`.okcredit.frontend.usecase.GetExpenses
import `in`.okcredit.frontend.usecase.GetUserExpenseTypes
import `in`.okcredit.frontend.usecase.SubmitExpenseUseCase
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class AddExpenseViewModelTest {

    private lateinit var viewModel: AddExpenseViewModel

    private val initialState: AddExpenseContract.State = mock()

    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getUserExpenseTypes: GetUserExpenseTypes = mock()

    private val submitExpenseUseCase: SubmitExpenseUseCase = mock()

    private val ab: AbRepository = mock()

    private val getExpenses: GetExpenses = mock()

    private val checkNetworkHealth: CheckNetworkHealth = mock()

    @Before
    fun setUp() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        viewModel = AddExpenseViewModel(
            initialState = initialState,
            checkNetworkHealth = { checkNetworkHealth },
            getActiveBusinessId = { getActiveBusinessId },
            getUserExpenseTypes = getUserExpenseTypes,
            submitExpense = submitExpenseUseCase,
            ab = ab,
            getExpenses = getExpenses
        )

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `get merchant id on load`() {
        val merchantId = "merchant-id"
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(mock())
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(merchantId))

        viewModel.attachIntents(Observable.just(AddExpenseContract.Intent.Load))
        val testObserver = viewModel.state().test()
        testObserver.values().contains(initialState)
        testObserver.dispose()
    }

    @Test
    fun `get user expense types`() {
        val response: Models.UserExpenseTypes = mock()
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(mock())
        whenever(getUserExpenseTypes.execute(mock())).thenReturn(Observable.just(Result.Success(response)))

        viewModel.attachIntents(Observable.just(AddExpenseContract.Intent.Load))
        val testObserver = viewModel.state().test()
        testObserver.values().contains(initialState.copy(userExpenseTypes = response.expenseTypes))
        testObserver.dispose()
    }

    @Test
    fun `is hide date tag ab enabled`() {
        val feature = "expense_hide_date_tag"
        assertTrue(Features.EXPENSE_HIDE_DATE_TAG == feature)
        whenever(ab.isFeatureEnabled(feature)).thenReturn(Observable.just(true))

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(AddExpenseContract.Intent.Load))

        testObserver.values().contains(initialState.copy(hideDateTag = true))
        testObserver.dispose()
    }

    @Test
    fun `is first transaction`() {
        val expenseList: Models.ExpenseListResponse = mock()
        whenever(getExpenses.execute(mock())).thenReturn(Observable.just(Result.Success(expenseList)))

        viewModel.attachIntents(Observable.just(AddExpenseContract.Intent.Load))
        val testObserver = viewModel.state().test()
        testObserver.values().contains(initialState.copy(isFirstTransaction = expenseList.expenseList.isNullOrEmpty()))
        testObserver.dispose()
    }

//    @Test
//    fun `submit expense`() {
//        whenever(submitExpenseUseCase.execute(mock())).thenReturn(Observable.just(Result.Success(mock())))
//
//        viewModel.attachIntents(Observable.just(AddExpenseContract.Intent.SubmitExpense(mock())))
//        val testObserver = viewModel.state().test()
//        testObserver.values().contains(initialState)
//
//        verify(navigator).goBack()
//        testObserver.dispose()
//    }

    @Test
    fun `on change date`() {
        val date: DateTime = mock()
        viewModel.attachIntents(Observable.just(AddExpenseContract.Intent.OnChangeDate(date)))
        val testObserver = viewModel.state().test()
        testObserver.values().contains(initialState.copy(date = date))

        testObserver.dispose()
    }

//    @Test
//    fun `on show suggestion`() {
//        val list: List<String?>? = mock()
//
//        viewModel.attachIntents(Observable.just(AddExpenseContract.Intent.ShowSuggestions(list)))
//        val testObserver = viewModel.state().test()
//        testObserver.values().contains(initialState.copy(suggestions = list))
//
//        testObserver.dispose()
//    }

//    @Test
//    fun `show submit cta`() {
//        viewModel.attachIntents(Observable.just(AddExpenseContract.Intent.ShowSubmitCTA(true)))
//        val testObserver = viewModel.state().test()
//        testObserver.values().contains(initialState.copy(showSubmitCTA = true))
//
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `show hand education`() {
//        viewModel.attachIntents(Observable.just(AddExpenseContract.Intent.ShowHandEducationIntent(true)))
//        val testObserver = viewModel.state().test()
//        testObserver.values().contains(initialState.copy(canShowHandEducation = true))
//
//        testObserver.dispose()
//    }
}
