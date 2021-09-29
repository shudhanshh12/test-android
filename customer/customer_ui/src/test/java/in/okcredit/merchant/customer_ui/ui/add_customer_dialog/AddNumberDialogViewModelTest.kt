package `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog

import `in`.okcredit.backend._offline.usecase.UpdateCustomer
import `in`.okcredit.backend._offline.usecase.UpdateSupplier
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class AddNumberDialogViewModelTest {

    private lateinit var viewModel: AddNumberDialogViewModel

    private val updateCustomer: Lazy<UpdateCustomer> = mock()
    private val updateSupplier: Lazy<UpdateSupplier> = mock()

    lateinit var testSchedulers: TestScheduler

    @Before
    fun setup() {
        testSchedulers = TestScheduler()
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.computation() } returns testSchedulers
    }

    fun createViewModel(
        initialState: AddNumberDialogContract.State,
        customerId: String,
        description: String,
        mobile: String? = null,
        isSkipAndSend: Boolean = false,
        isSupplier: Boolean = false
    ) {
        viewModel = AddNumberDialogViewModel(
            initialState = initialState,
            customerId = customerId,
            description = description,
            isSkipAndSend = isSkipAndSend,
            isSupplier = isSupplier,
            mobile = mobile,
            updateCustomer = updateCustomer,
            updateSupplier = updateSupplier,
            syncSupplierEnabledCustomerIds = mock(),
            screen = null
        )
    }

    @Test
    fun `should set bundle properties passed to viewModel on load`() {
        val customerId = "1234"
        val description = "Add Phone Number"
        val isSkipAndSend = true
        val mobile = "8882946897"
        val initialState = AddNumberDialogContract.State()
        createViewModel(initialState, customerId, description, mobile, isSkipAndSend)

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(AddNumberDialogContract.Intent.Load))
        testSchedulers.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        testObserver.values().contains(initialState)
        val last = testObserver.values().last()
        assertThat(
            last.customerId == customerId && last.description == description && last.mobile == mobile && last.isSkipAndSend == isSkipAndSend
        ).isTrue()
        testObserver.dispose()
    }

//    @Test
//    fun `should set hasFocus true when editText has focus`() {
//        val customerId = "1234"
//        val description = "Add Phone Number"
//        val initialState = AddNumberDialogContract.State()
//        createViewModel(initialState, customerId, description)
//
//        val testObserver = viewModel.state().test()
//        viewModel.attachIntents(Observable.just(AddNumberDialogContract.Intent.SetEditTextFocus(true)))
//
//        testObserver.values().contains(initialState)
//        assertThat(testObserver.values().last() == initialState.copy(hasFocus = false)).isTrue()
//
//        testObserver.dispose()
//    }

    @Test
    fun `should set hasFocus false when editText doesn't have focus`() {
        val customerId = "1234"
        val description = "Add Phone Number"
        val initialState = AddNumberDialogContract.State()
        createViewModel(initialState, customerId, description)

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(AddNumberDialogContract.Intent.SetEditTextFocus(false)))

        testObserver.values().contains(initialState)
        assertThat(testObserver.values().last() == initialState.copy(hasFocus = false)).isTrue()

        testObserver.dispose()
    }
}
