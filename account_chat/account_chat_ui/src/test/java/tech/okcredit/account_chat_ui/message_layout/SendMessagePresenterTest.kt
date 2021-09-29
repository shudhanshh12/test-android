package tech.okcredit.account_chat_ui.message_layout

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.account_chat_sdk.AccountChatTracker
import tech.okcredit.account_chat_sdk.models.Message
import tech.okcredit.account_chat_sdk.use_cases.SendChatMessage

class SendMessagePresenterTest {

    lateinit var testObserver: TestObserver<SendMessageContract.State>

    private var initialState = SendMessageContract.State()
    private val sendChatMessage: SendChatMessage = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val trackerLazy: AccountChatTracker = mock()

    private lateinit var viewModel: SendMessageViewModel

    @Before
    fun setup() {

        viewModel = SendMessageViewModel(
            initialState,
            sendChatMessage,
            { getActiveBusinessId },
            { trackerLazy }
        )

        testObserver = viewModel.state().test()

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.io() } returns Schedulers.trampoline()
        every { Schedulers.single() } returns Schedulers.trampoline()
    }

    @Test
    fun `sendChatMessage when returns success`() {
        val preMessageReq: SendMessageContract.PreMessageState = mock()
        val responseMessage: Message = mock()
        whenever(
            sendChatMessage.execute(
                SendChatMessage.Request(
                    preMessageReq.message ?: "",
                    preMessageReq.accountID ?: "",
                    "merchant_id",
                    preMessageReq.role ?: "",
                    preMessageReq.recevierRole ?: "",
                    preMessageReq.accountName
                )
            )
        ).thenReturn(Observable.just(Result.Success(responseMessage)))

        viewModel.attachIntents(Observable.just(SendMessageContract.Intent.SendMessage(preMessageReq)))

        Truth.assertThat(
            testObserver.values()
                .first() == initialState
        )
        Truth.assertThat(
            testObserver.values()
                .last() == initialState.copy(
                editTextState = SendMessageContract.EditTextState.Empty,
                messageSentStatus = true
            )
        )
    }

    @Test
    fun `sendChatMessage when returns error`() {
        val mockError: Exception = mock()
        val preMessageReq: SendMessageContract.PreMessageState = mock()
        whenever(
            sendChatMessage.execute(
                SendChatMessage.Request(
                    preMessageReq.message ?: "",
                    preMessageReq.accountID ?: "",
                    "merchant_id",
                    preMessageReq.role ?: "",
                    preMessageReq.recevierRole ?: "",
                    preMessageReq.accountName
                )
            )
        ).thenReturn(Observable.error(mockError))

        viewModel.attachIntents(Observable.just(SendMessageContract.Intent.SendMessage(preMessageReq)))

        Truth.assertThat(
            testObserver.values()
                .first() == initialState
        )
        Truth.assertThat(
            testObserver.values()
                .last() == initialState
        )
    }

    @Test
    fun `Message when message is not empty and not blank`() {
        viewModel.attachIntents(Observable.just(SendMessageContract.Intent.Message("message")))

        Truth.assertThat(
            testObserver.values()
                .first() == initialState
        )
        Truth.assertThat(
            testObserver.values()
                .last() == initialState.copy(
                editTextState = SendMessageContract.EditTextState.Filled,
                sendButtonState = SendMessageContract.SendButtonState.Active
            )
        )
    }

    @Test
    fun `Message when message is not empty and is blank`() {
        viewModel.attachIntents(Observable.just(SendMessageContract.Intent.Message(" ")))

        Truth.assertThat(
            testObserver.values()
                .first() == initialState
        )
        Truth.assertThat(
            testObserver.values()
                .last() == initialState.copy(
                editTextState = SendMessageContract.EditTextState.Filled,
                sendButtonState = SendMessageContract.SendButtonState.Inactive
            )
        )
    }

    @Test
    fun `Message when message is empty`() {
        viewModel.attachIntents(Observable.just(SendMessageContract.Intent.Message("")))

        Truth.assertThat(
            testObserver.values()
                .first() == initialState
        )
        Truth.assertThat(
            testObserver.values()
                .last() == initialState.copy(
                editTextState = SendMessageContract.EditTextState.Empty,
                sendButtonState = SendMessageContract.SendButtonState.Inactive
            )
        )
    }

    @Test
    fun `TrackMessageStart when message is empty`() {
        viewModel.attachIntents(Observable.just(SendMessageContract.Intent.TrackMessageStart))

        Truth.assertThat(
            testObserver.values()
                .first() == initialState
        )
        verify(trackerLazy, times(1)).trackMessageStart(null, null, "", "Chat")
    }

    @Test
    fun `LoadInitialDataw hen getActiveMerchant return active merchant`() {
        val mockedMerchantId = "merchant_id"
        val loadInitialData: SendMessageContract.IntialData = mock()
        whenever(
            getActiveBusinessId.execute()
        ).thenReturn(Single.just(mockedMerchantId))

        viewModel.attachIntents(Observable.just(SendMessageContract.Intent.LoadInitialData(loadInitialData)))

        Truth.assertThat(
            testObserver.values()
                .last() == initialState
        )
    }

    @After
    fun cleanUp() {
        testObserver.dispose()
    }
}
