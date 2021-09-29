package `in`.okcredit.collection_ui.ui.passbook.add_to_khata

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection_ui.usecase.GetCollectionOnlinePayment
import `in`.okcredit.collection_ui.usecase.TagOnlinePaymentWithCustomer
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import com.google.common.truth.Truth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class AddToKhataViewModelTest {

    lateinit var testObserver: TestObserver<AddToKhataContract.State>
    lateinit var viewModel: AddToKhataViewModel

    private val initialState: AddToKhataContract.State = AddToKhataContract.State()
    private val customerId = "customerId"
    private val paymentId = "paymentId"
    private val getCustomer: GetCustomer = mock()
    private val context: Context = mock()
    private val getCollectionOnlinePaymentLazy: Lazy<GetCollectionOnlinePayment> = mock()
    private val getCollectionOnlinePayment: GetCollectionOnlinePayment = mock()
    private val tagOnlinePaymentWithCustomerLazy: Lazy<TagOnlinePaymentWithCustomer> = mock()
    private val tagOnlinePaymentWithCustomer: TagOnlinePaymentWithCustomer = mock()

    fun createViewModel(initialState: AddToKhataContract.State) {
        viewModel = AddToKhataViewModel(
            initialState = initialState,
            customerId = customerId,
            paymentId = paymentId,
            getCustomer = { getCustomer },
            getCollectionOnlinePayment = getCollectionOnlinePaymentLazy,
            tagOnlinePaymentWithCustomer = tagOnlinePaymentWithCustomerLazy,
            context = context
        )
    }

    @Before
    fun setup() {
        whenever(getCollectionOnlinePaymentLazy.get()).thenReturn(getCollectionOnlinePayment)
        whenever(tagOnlinePaymentWithCustomerLazy.get()).thenReturn(tagOnlinePaymentWithCustomer)

        createViewModel(initialState)
        testObserver = TestObserver()
        viewModel.state().subscribe(testObserver)

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `getCustomer()`() {
        val customer: Customer = mock()
        val collectionOnlinePayment: CollectionOnlinePayment = mock()
        whenever(getCustomer.execute("id")).thenReturn(Observable.just(customer))
        whenever(getCollectionOnlinePaymentLazy.get().execute("id")).thenReturn(
            Observable.just(
                Result.Success(
                    collectionOnlinePayment
                )
            )
        )

        viewModel.attachIntents(Observable.just(AddToKhataContract.Intent.Load))

        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                customer = customer,
                collectionOnlinePayment = collectionOnlinePayment
            )
        )

        testObserver.dispose()
    }

    @Test
    fun `tagCustomer()`() {
        val request: TagOnlinePaymentWithCustomer.Request = mock()

        whenever(
            tagOnlinePaymentWithCustomerLazy.get().execute(request)
        ).thenReturn(Observable.just(Result.Success(Unit)))

        viewModel.attachIntents(Observable.just(AddToKhataContract.Intent.TagCustomer))

        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState
        )

        testObserver.dispose()
    }
}
