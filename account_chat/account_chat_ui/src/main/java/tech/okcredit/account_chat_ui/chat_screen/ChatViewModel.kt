package tech.okcredit.account_chat_ui.chat_screen

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.suppliercredit.GetSupplier
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.exceptions.CompositeException
import io.reactivex.subjects.PublishSubject
import tech.okcredit.account_chat_contract.CHAT_INTENT_EXTRAS
import tech.okcredit.account_chat_contract.FEATURE.CHAT_TOOTTIP
import tech.okcredit.account_chat_contract.STRING_CONSTANTS
import tech.okcredit.account_chat_sdk.AccountChatTracker
import tech.okcredit.account_chat_ui.R
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    val initialState: ChatContract.State,
    @ViewModelParam(CHAT_INTENT_EXTRAS.ACCOUNT_ID) val accountID: String?,
    @ViewModelParam(CHAT_INTENT_EXTRAS.ROLE) val role: String?,
    @ViewModelParam(CHAT_INTENT_EXTRAS.UNREAD_MESSAGE_COUNT) val unreadMessageCount: String?,
    @ViewModelParam(CHAT_INTENT_EXTRAS.FIRST_UNSEEN_MESSAGE_ID) val firstUnseenMessageId: String?,
    private val getCustomer: Lazy<GetCustomer>,
    private val getSupplier: Lazy<GetSupplier>,
    private val communicationApi: Lazy<CommunicationRepository>,
    private val context: Lazy<Context>,
    private val accountChatTracker: Lazy<AccountChatTracker>,
    private val ab: Lazy<AbRepository>
) : BaseViewModel<ChatContract.State, ChatContract.PartialState, ChatContract.ViewEvent>(
    initialState
) {

    // TODO: Clean Directory Name and file names
    companion object {
        const val FILE_NAME = "reminder.jpg"
        const val FOLDER_NAME = "reminder_images"
    }

    private var mobile: String? = null
    private var getCustomerDetails = PublishSubject.create<String>()
    private val showAlertPublicSubject: PublishSubject<String> = PublishSubject.create()
    private var getSupplierDetails = PublishSubject.create<String>()
    override fun handle(): Observable<out UiState.Partial<ChatContract.State>> {
        return Observable.mergeArray(
            showAlert(),
            onload(),
            getCustomerDetails(),
            getSupplierDetails(),
            appPromotion(),
            phoneDialer(),
            pageViewed(),
            canShowToolTip()
        )
    }

    private fun canShowToolTip(): ObservableSource<out ChatContract.PartialState>? {
        return intent<ChatContract.Intent.Load>()
            .switchMap { ab.get().isFeatureEnabled(CHAT_TOOTTIP) }
            .map {
                ChatContract.PartialState.CanShowChatTooltip(it)
            }
    }

    private fun showAlert(): Observable<ChatContract.PartialState> {
        return showAlertPublicSubject
            .switchMap {
                Observable.timer(2, TimeUnit.SECONDS)
                    .map<ChatContract.PartialState> { ChatContract.PartialState.HideAlert }
                    .startWith(
                        ChatContract.PartialState.ShowAlert(
                            it
                        )
                    )
            }
    }

    fun onload(): Observable<ChatContract.PartialState> {
        return intent<ChatContract.Intent.Load>()
            .map {
                if (role != null) {
                    if (role == STRING_CONSTANTS.SELLER) {
                        getCustomerDetails.onNext(accountID!!)
                    } else if (role == STRING_CONSTANTS.BUYER) {
                        getSupplierDetails.onNext(accountID!!)
                    }
                    ChatContract.PartialState.SetIntentExtras(
                        role,
                        accountID!!,
                        unreadMessageCount,
                        firstUnseenMessageId
                    )
                } else {
                    ChatContract.PartialState.NoChange
                }
            }
    }

    private fun getCustomerDetails(): Observable<ChatContract.PartialState> {
        return getCustomerDetails.switchMap {
            UseCase.wrapObservable(getCustomer.get().execute(accountID))
        }
            .map {
                when (it) {
                    is Result.Progress -> ChatContract.PartialState.NoChange
                    is Result.Success -> {
                        mobile = it.value.mobile
                        ChatContract.PartialState.SetAccount(
                            it.value.id,
                            it.value.description,
                            it.value.profileImage,
                            STRING_CONSTANTS.BUYER,
                            it.value.isRegistered(),
                            it.value.mobile,
                            unreadMessageCount,
                            firstUnseenMessageId
                        )
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                ChatContract.PartialState.NoChange
                            }
                            else -> {
                                Timber.e(it.error, "ErrorState")
                                ChatContract.PartialState.ErrorState
                            }
                        }
                    }
                }
            }
    }

    private fun getSupplierDetails(): Observable<ChatContract.PartialState> {
        return getSupplierDetails.switchMap {
            UseCase.wrapObservable(getSupplier.get().executeObservable(accountID!!))
        }
            .map {
                when (it) {
                    is Result.Progress -> ChatContract.PartialState.NoChange
                    is Result.Success -> {
                        mobile = it.value.mobile
                        ChatContract.PartialState.SetAccount(
                            it.value.id,
                            it.value.name,
                            it.value.profileImage,
                            STRING_CONSTANTS.SELLER,
                            it.value.registered,
                            it.value.mobile,
                            unreadMessageCount,
                            firstUnseenMessageId
                        )
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                ChatContract.PartialState.NoChange
                            }
                            else -> {
                                Timber.e(it.error, "ErrorState")
                                ChatContract.PartialState.ErrorState
                            }
                        }
                    }
                }
            }
    }

    private fun appPromotion(): Observable<ChatContract.PartialState.NoChange> {
        return intent<ChatContract.Intent.ShareAppPromotion>()
            .switchMap {
                UseCase.wrapSingle(
                    communicationApi.get().goToWhatsApp(
                        ShareIntentBuilder(
                            shareText = it.sharingText,
                            phoneNumber = mobile,
                            imageFrom = ImagePath.ImageUriFromBitMap(
                                it.bitmap,
                                context.get(),
                                FOLDER_NAME,
                                FILE_NAME
                            ),
                        )
                    )
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> ChatContract.PartialState.NoChange
                    is Result.Success -> {
                        emitViewEvent(ChatContract.ViewEvent.OpenWhatsAppPromotionShare(it.value))
                        ChatContract.PartialState.NoChange
                    }
                    is Result.Failure -> {
                        when {
                            it.error is IntentHelper.NoWhatsAppError ||
                                it.error is CompositeException && (it.error as CompositeException).exceptions.find { e -> e is IntentHelper.NoWhatsAppError } != null -> {
                                showAlertPublicSubject.onNext(context.get().getString(R.string.whatsapp_not_installed))
                                ChatContract.PartialState.NoChange
                            }
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(ChatContract.ViewEvent.GotoLogin)
                                ChatContract.PartialState.NoChange
                            }
                            isInternetIssue(it.error) -> {
                                showAlertPublicSubject.onNext(context.get().getString(R.string.no_internet_msg))
                                ChatContract.PartialState.NoChange
                            }
                            else -> {
                                showAlertPublicSubject.onNext(context.get().getString(R.string.err_default))
                                Timber.e(it.error, "CustomerScreenPresenter SharePayment Link")
                                ChatContract.PartialState.NoChange
                            }
                        }
                    }
                }
            }
    }

    private fun phoneDialer(): Observable<ChatContract.PartialState.NoChange> {
        return intent<ChatContract.Intent.GoToPhoneDialer>()
            .map {
                emitViewEvent(ChatContract.ViewEvent.GotoCallCustomer(mobile))
                ChatContract.PartialState.NoChange
            }
    }

    private fun pageViewed(): Observable<ChatContract.PartialState.NoChange> {
        return intent<ChatContract.Intent.PageViewed>()
            .map {
                accountChatTracker.get().trackPageViewed(it.screen, accountID, unreadMessageCount, role)
                ChatContract.PartialState.NoChange
            }
    }

    override fun reduce(
        currentState: ChatContract.State,
        partialState: ChatContract.PartialState
    ): ChatContract.State {
        return when (partialState) {
            ChatContract.PartialState.NoChange -> currentState
            is ChatContract.PartialState.SetAccountId -> currentState.copy(
                accountId = partialState.accountId,
                token = partialState.token
            )
            is ChatContract.PartialState.SetRole -> currentState.copy(role = partialState.role)
            is ChatContract.PartialState.SetAccount -> currentState.copy(
                accountId = partialState.accountId,
                accountName = partialState.name,
                accountPic = partialState.rofileImage,
                recevierRole = partialState.receiverRole,
                isRegistered = partialState.isRegistered,
                mobile = partialState.mobile,
                unreadMessageCount = partialState.unreadMesssageCount,
                firstUnseenMessageId = partialState.firstUnseenMessageId
            )
            ChatContract.PartialState.ErrorState -> TODO()
            is ChatContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            ChatContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is ChatContract.PartialState.SetIntentExtras -> currentState.copy(
                role = partialState.role,
                accountId = partialState.accountID,
                firstUnseenMessageId = partialState.firstUnseenMessageId,
                unreadMessageCount = partialState.unreadMesssageCount
            )
            is ChatContract.PartialState.CanShowChatTooltip -> currentState.copy(canShowChatTooltip = partialState.canShowChatTooltip)
        }
    }
}
