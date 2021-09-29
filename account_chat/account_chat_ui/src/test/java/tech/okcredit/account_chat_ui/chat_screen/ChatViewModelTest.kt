package tech.okcredit.account_chat_ui.chat_screen

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.suppliercredit.GetSupplier
import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import tech.okcredit.account_chat_sdk.AccountChatTracker
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.communication.CommunicationRepository

class ChatViewModelTest {

    private lateinit var viewModel: ChatViewModel
    private val getCustomer: GetCustomer = mock()
    private val getSupplier: GetSupplier = mock()
    private val communicationRepository: CommunicationRepository = mock()
    private val context: Context = mock()
    private val accountChatTracker: AccountChatTracker = mock()
    private val ab: AbRepository = mock()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        viewModel = ChatViewModel(
            ChatContract.State(), "123", "SELLER", "1", "123",
            { getCustomer },
            { getSupplier },
            { communicationRepository },
            { context },
            { accountChatTracker },
            { ab }
        )
    }

//    @Test
//    fun `phone dialer`() {
//        val testObserver = TestObserver<ChatContract.State>()
//        viewModel.state().subscribe(testObserver)
//        viewModel.attachIntents(Observable.just(ChatContract.Intent.GoToPhoneDialer))
//        testObserver.assertValue(ChatContract.State())
//    }
}
