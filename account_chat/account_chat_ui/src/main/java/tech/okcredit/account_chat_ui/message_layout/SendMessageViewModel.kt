package tech.okcredit.account_chat_ui.message_layout

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.base.BaseLayout
import `in`.okcredit.shared.base.BaseLayoutViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import tech.okcredit.account_chat_sdk.AccountChatTracker
import tech.okcredit.account_chat_sdk.use_cases.SendChatMessage
import tech.okcredit.account_chat_sdk.utils.ChatUtils
import timber.log.Timber
import javax.inject.Inject

class SendMessageViewModel
@Inject constructor(
    var initialState: SendMessageContract.State,
    private val sendChatMessage: SendChatMessage,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val trackerLazy: Lazy<AccountChatTracker>
) :
    BaseLayoutViewModel<SendMessageContract.State, SendMessageContract.PartialState>(
        initialState,
        Schedulers.newThread(),
        Schedulers.newThread()
    ) {

    private var merchantId: String? = null
    private var account_Id: String? = null
    private var role: String? = null
    private lateinit var interactor: SendMessageContract.Interactor

    override fun handle(): Observable<out UiState.Partial<SendMessageContract.State>> {
        return Observable.mergeArray(
            intent<SendMessageContract.Intent.SendMessage>()
                .switchMap {
                    sendChatMessage.execute(
                        SendChatMessage.Request(
                            it.preMessageState.message!!,
                            it.preMessageState.accountID,
                            merchantId,
                            it.preMessageState.role,
                            it.preMessageState.recevierRole,
                            it.preMessageState.accountName
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Success -> {
                            interactor.postMessageSentActions()
                            trackerLazy.get().trackMessageSent(account_Id, ChatUtils.getAccountRole(role), it.value)
                            SendMessageContract.PartialState.PostMessageState(
                                true,
                                SendMessageContract.EditTextState.Empty
                            )
                        }
                        is Result.Failure -> {
                            SendMessageContract.PartialState.NoChange
                        }
                        is Result.Progress -> {
                            SendMessageContract.PartialState.SetMessageSentState(false)
                        }
                    }
                },
            intent<SendMessageContract.Intent.Message>()
                .map {
                    if (it.message.isNotEmpty()) {
                        if (it.message.isNotBlank()) {
                            SendMessageContract.PartialState.LayoutInputState(
                                SendMessageContract.SendButtonState.Active,
                                SendMessageContract.EditTextState.Filled
                            )
                        } else {
                            SendMessageContract.PartialState.LayoutInputState(
                                SendMessageContract.SendButtonState.Inactive,
                                SendMessageContract.EditTextState.Filled
                            )
                        }
                    } else {
                        SendMessageContract.PartialState.LayoutInputState(
                            SendMessageContract.SendButtonState.Inactive,
                            SendMessageContract.EditTextState.Empty
                        )
                    }
                },
            intent<SendMessageContract.Intent.LoadInitialData>()
                .map {
                    account_Id = it.initialData.accountID
                    role = it.initialData.role
                    SendMessageContract.PartialState.InitialData(it.initialData)
                },
            intent<SendMessageContract.Intent.LoadInitialData>()
                .switchMap { UseCase.wrapSingle(getActiveBusinessId.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> SendMessageContract.PartialState.NoChange
                        is Result.Success -> {
                            merchantId = it.value
                            SendMessageContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    SendMessageContract.PartialState.NoChange
                                }
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    SendMessageContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },
            intent<SendMessageContract.Intent.TrackMessageStart>()
                .map {
                    trackerLazy.get().trackMessageStart(account_Id, role, "", AccountChatTracker.Values.CHAT_SCREEN)
                    SendMessageContract.PartialState.NoChange
                }
        )
    }

    override fun reduce(
        currentState: SendMessageContract.State,
        partialState: SendMessageContract.PartialState
    ): SendMessageContract.State {
        return when (partialState) {
            is SendMessageContract.PartialState.NoChange -> currentState
            is SendMessageContract.PartialState.ErrorState -> currentState.copy(
                isLoading = false,
                error = true
            )
            is SendMessageContract.PartialState.SendMessage -> currentState
            is SendMessageContract.PartialState.InitialData -> currentState.copy(
                accountID = partialState.initialData.accountID,
                role = partialState.initialData.role,
                receiverRole = partialState.initialData.recevierRole,
                accountName = partialState.initialData.accountName
            )
            is SendMessageContract.PartialState.SendButtonState -> currentState.copy(sendButtonState = partialState.sendButtonState)
            is SendMessageContract.PartialState.PostMessageState -> currentState.copy(
                editTextState = partialState.editTextState,
                messageSentStatus = partialState.status
            )
            is SendMessageContract.PartialState.SetMessageSentState -> currentState.copy(messageSentStatus = partialState.status)
            is SendMessageContract.PartialState.LayoutInputState -> currentState.copy(
                editTextState = partialState.editTextState,
                sendButtonState = partialState.sendButtonState
            )
        }
    }

    override fun setNavigation(baseLayout: BaseLayout<SendMessageContract.State>) {
        this.interactor = genericCastOrNull<SendMessageContract.Interactor>(baseLayout)!!
    }
}
