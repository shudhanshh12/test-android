package tech.okcredit.account_chat_ui.message_list_layout

import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.base.BaseLayout
import `in`.okcredit.shared.base.BaseLayoutViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import tech.okcredit.account_chat_sdk.use_cases.GetFirebaseUser
import timber.log.Timber
import javax.inject.Inject

class MessageListViewModel @Inject constructor(
    var initialState: MessageListContract.State,
    private val getActiveBusiness: GetActiveBusiness,
    private val getFirebaseUser: GetFirebaseUser
) : BaseLayoutViewModel<MessageListContract.State, MessageListContract.PartialState>(
    initialState,
    Schedulers.newThread(),
    Schedulers.newThread()
) {

    private var merchantId: String? = null

    private lateinit var interactor: MessageListContract.Interactor

    override fun handle(): Observable<out UiState.Partial<MessageListContract.State>> {
        return Observable.mergeArray(
            intent<MessageListContract.Intent.LoadInitialData>()
                .map {
                    MessageListContract.PartialState.InitialData(it.initialData)
                },
            intent<MessageListContract.Intent.LoadInitialData>()
                .switchMap { UseCase.wrapObservable(getActiveBusiness.execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> MessageListContract.PartialState.NoChange
                        is Result.Success -> {
                            merchantId = it.value.id
                            MessageListContract.PartialState.SetBusiness(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    MessageListContract.PartialState.NoChange
                                }
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    MessageListContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },
            intent<MessageListContract.Intent.LoadInitialData>()
                .switchMap { getFirebaseUser.execute(Unit) }
                .map {
                    when (it) {
                        is Result.Progress -> MessageListContract.PartialState.NoChange
                        is Result.Success -> {
                            MessageListContract.PartialState.SetCurrentUserId(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    MessageListContract.PartialState.NoChange
                                }
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    MessageListContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                }
        )
    }

    override fun reduce(
        currentState: MessageListContract.State,
        partialState: MessageListContract.PartialState
    ): MessageListContract.State {
        return when (partialState) {
            is MessageListContract.PartialState.NoChange -> currentState
            is MessageListContract.PartialState.ErrorState -> currentState.copy(
                isLoading = false,
                error = true
            )
            is MessageListContract.PartialState.SendMessage -> currentState
            is MessageListContract.PartialState.InitialData -> currentState.copy(
                accountID = partialState.initialData.accountID,
                unreadMessageCount = partialState.initialData.unreadMessageCount,
                firstUnreadMessageId = partialState.initialData.firstUnreadMessageId
            )
            is MessageListContract.PartialState.EditTextState -> currentState.copy(editTextState = partialState.state)
            is MessageListContract.PartialState.SendButtonState -> currentState.copy(sendButtonState = partialState.sendButtonState)
            is MessageListContract.PartialState.SetBusiness -> currentState.copy(merchatId = partialState.business!!.id)
            is MessageListContract.PartialState.SetCurrentUserId -> currentState.copy(uid = partialState.uid)
        }
    }

    override fun setNavigation(baseLayout: BaseLayout<MessageListContract.State>) {
        this.interactor = genericCastOrNull<MessageListContract.Interactor>(baseLayout)!!
    }
}
