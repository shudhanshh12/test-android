package `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount

import `in`.okcredit.backend._offline.usecase.GetTransaction
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.usecase.UpdateTransactionAmount
import `in`.okcredit.shared.usecase.Result
import com.google.common.truth.Truth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

class UpdateTransactionAmountViewModelTest {

    private val initialState: UpdateTransactionAmountContract.State = UpdateTransactionAmountContract.State()
    private val transactionId: String = "transaction_id"
    private val updateTransactionAmount: UpdateTransactionAmount = mock()
    private val getTransaction: GetTransaction = mock()
    private val getCustomer: GetCustomer = mock()

    lateinit var testObserverViewEvent: TestObserver<UpdateTransactionAmountContract.ViewEvent>
    lateinit var testObserver: TestObserver<UpdateTransactionAmountContract.State>
    lateinit var viewModel: UpdateTransactionAmountViewModel

    private fun createViewModel(initialState: UpdateTransactionAmountContract.State) {
        viewModel = UpdateTransactionAmountViewModel(
            initialState = initialState,
            transactionId = transactionId,
            updateTransactionAmount = { updateTransactionAmount },
            getCustomer = { getCustomer },
            getTransaction = { getTransaction },
        )
    }

    @Before
    fun setup() {
        createViewModel(initialState)

        whenever(getTransaction.execute(transactionId)).thenReturn(Observable.just(TestData.TRANSACTION1))

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        testObserver = viewModel.state().test()

        testObserverViewEvent = viewModel.viewEvent().test()

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `update transaction amount  successfully`() {

        whenever(updateTransactionAmount.execute(UpdateTransactionAmount.Request(100L, transactionId))).thenReturn(
            Observable.just(Result.Success(Unit))
        )

        // provide intent
        viewModel.attachIntents(Observable.just(UpdateTransactionAmountContract.Intent.UpdateTransactionAmount(100L)))

        Truth.assertThat(testObserver.values().first() == initialState.copy(isLoading = true))
        Truth.assertThat(
            testObserverViewEvent.values().last() == UpdateTransactionAmountContract.ViewEvent.AmountUpdatedSuccessfully
        )
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                isLoading = false
            )
        )
    }

    @Test
    fun `update transaction amount returns error`() {
        // provide intent
        viewModel.attachIntents(Observable.just(UpdateTransactionAmountContract.Intent.UpdateTransactionAmount(100L)))

        whenever(updateTransactionAmount.execute(UpdateTransactionAmount.Request(100L, transactionId))).thenReturn(
            Observable.error(Exception())
        )
        Truth.assertThat(testObserver.values().first() == initialState.copy(isLoading = true))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                alertMessage = "Some failure occurred"
            )
        )
    }

    @Test
    fun `getCustomerPublishSubject return customer successfully`() {
        // provide intent
        viewModel.attachIntents(Observable.just(UpdateTransactionAmountContract.Intent.Load))

        whenever(getCustomer.execute(TestData.TRANSACTION1.customerId)).thenReturn(
            Observable.just(TestData.CUSTOMER)
        )
        Truth.assertThat(testObserver.values().first() == initialState)
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                customer = TestData.CUSTOMER
            )
        )
    }

    @Test
    fun `set show alert`() {
        // provide intent
        viewModel.attachIntents(Observable.just(UpdateTransactionAmountContract.Intent.ShowAlert("show alert")))

        Truth.assertThat(
            testObserver.values().first() == initialState.copy(
                alertMessage = "show alert"
            )
        )
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                isLoading = false
            )
        )
    }

    @After
    fun cleanup() {
        testObserver.dispose()
        testObserverViewEvent.dispose()
    }
}
