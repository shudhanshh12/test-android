package `in`.okcredit.supplier.supplier_profile_bottom_sheet

import `in`.okcredit.supplier.R
import `in`.okcredit.supplier.SupplierTestData.SUPPLIER
import `in`.okcredit.supplier.usecase.GetSupplier
import android.content.Context
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import merchant.okcredit.supplier.contract.IsAccountChatEnabledForSupplier
import merchant.okcredit.supplier.contract.IsSupplierCollectionEnabled
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.account_chat_sdk.use_cases.GetChatUnreadMessageCount

class SupplierProfileViewModelTest {
    lateinit var testObserver: TestObserver<SupplierProfileContract.State>
    private val testObserverViewEvent =
        TestObserver<SupplierProfileContract.ViewEvents>()

    private val initialState =
        SupplierProfileContract.State()

    private val supplierId: String = "supplier_id"
    private val getChatUnreadMessages: GetChatUnreadMessageCount = mock()
    private val getSupplier: GetSupplier = mock()
    private val context: Context = mock()
    private val isAccountChatEnabledForSupplier: IsAccountChatEnabledForSupplier = mock()
    private val isSupplierCollectionEnabled: IsSupplierCollectionEnabled = mock()

    private lateinit var viewModel: SupplierProfileViewModel

    @Before
    fun setup() {

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.io() } returns Schedulers.trampoline()
        every { Schedulers.single() } returns Schedulers.trampoline()

        whenever(context.getString(R.string.supplier_network)).thenReturn("Network Error")
        whenever(context.getString(R.string.supplier_other_error)).thenReturn("Something went wrong")

        viewModel = SupplierProfileViewModel(
            initialState,
            supplierId,
            { getSupplier },
            { getChatUnreadMessages },
            { isAccountChatEnabledForSupplier },
            { isSupplierCollectionEnabled },
            { context }
        )

        testObserver = viewModel.state().test()
        viewModel.viewEvent().subscribe(testObserverViewEvent)
    }

    @Test
    fun `actionOnCall when supplier mobile is empty`() {
        val mockSupplier = SUPPLIER.copy(mobile = "")
        viewModel.supplier = mockSupplier
        viewModel.attachIntents(Observable.just(SupplierProfileContract.Intent.ActionOnCall))

        Truth.assertThat(
            testObserver.values()
                .last() == initialState
        ).isTrue()
        testObserverViewEvent.assertValue(
            SupplierProfileContract.ViewEvents.AddSupplierMobile(
                mockSupplier.id
            )
        )
    }

    @Test
    fun `actionOnCall when supplier mobile is null`() {
        val mockSupplier = SUPPLIER.copy(mobile = null)
        viewModel.supplier = mockSupplier
        viewModel.attachIntents(Observable.just(SupplierProfileContract.Intent.ActionOnCall))

        Truth.assertThat(
            testObserver.values()
                .last() == initialState
        ).isTrue()
        testObserverViewEvent.assertValue(
            SupplierProfileContract.ViewEvents.AddSupplierMobile(
                mockSupplier.id
            )
        )
    }

    @Test
    fun `actionOnCall when supplier mobile is not null or empty`() {
        viewModel.supplier = SUPPLIER
        viewModel.attachIntents(Observable.just(SupplierProfileContract.Intent.ActionOnCall))

        Truth.assertThat(
            testObserver.values()
                .last() == initialState
        ).isTrue()
        testObserverViewEvent.assertValue(
            SupplierProfileContract.ViewEvents.CallToSupplier(
                SUPPLIER.mobile
                    ?: ""
            )
        )
    }

    @Test
    fun `SendWhatsAppReminder successfully`() {
        val mockSupplier = SUPPLIER
        viewModel.supplier = mockSupplier
        viewModel.attachIntents(Observable.just(SupplierProfileContract.Intent.SendWhatsAppReminder))

        Truth.assertThat(
            testObserver.values()
                .last() == initialState
        ).isTrue()
        testObserverViewEvent.assertValue(
            SupplierProfileContract.ViewEvents.ShareWhatsappReminder(
                mockSupplier.mobile,
                mockSupplier.name
            )
        )
    }

    @Test
    fun `redirectToChatScreen successfully`() {
        val mockSupplier = SUPPLIER
        viewModel.supplier = mockSupplier
        viewModel.attachIntents(Observable.just(SupplierProfileContract.Intent.RedirectToChatScreen))

        Truth.assertThat(
            testObserver.values()
                .last() == initialState
        ).isTrue()
        testObserverViewEvent.assertValue(SupplierProfileContract.ViewEvents.RedirectToChatScreen)
    }

    @Test
    fun `gotoSupplierPaymentScreen successfully`() {
        val mockSupplier = SUPPLIER
        viewModel.supplier = mockSupplier
        viewModel.attachIntents(Observable.just(SupplierProfileContract.Intent.GoToSupplierPaymentScreen))

        Truth.assertThat(
            testObserver.values()
                .last() == initialState
        ).isTrue()
        testObserverViewEvent.assertValue(
            SupplierProfileContract.ViewEvents.GoToSupplierPaymentScreen(
                mockSupplier.id
            )
        )
    }

    @After
    fun cleanUp() {
        testObserver.dispose()
        testObserverViewEvent.dispose()
    }
}
