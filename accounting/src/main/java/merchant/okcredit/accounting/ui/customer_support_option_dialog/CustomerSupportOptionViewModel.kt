package merchant.okcredit.accounting.ui.customer_support_option_dialog

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.contract.model.SupportType
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportType
import merchant.okcredit.accounting.usecases.GetCustomerSupportDataImpl
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import javax.inject.Inject

class CustomerSupportOptionViewModel @Inject constructor(
    initialState: CustomerSupportOptionContract.State,
    private val getCustomerSupportType: Lazy<GetCustomerSupportType>,
    private val communicationRepository: Lazy<CommunicationRepository>,
    private val accountingEventTracker: Lazy<AccountingEventTracker>,
    private val getCustomerSupportData: Lazy<GetCustomerSupportDataImpl>,
) : BaseViewModel<CustomerSupportOptionContract.State, CustomerSupportOptionContract.PartialState, CustomerSupportOptionContract.ViewEvents>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<CustomerSupportOptionContract.State>> {
        return Observable.mergeArray(
            getCustomerSupportType(),
            sendWhatsAppMessage(),
            actionClicked(),
        )
    }

    private fun getCustomerSupportType(): Observable<CustomerSupportOptionContract.PartialState> {
        return intent<CustomerSupportOptionContract.Intent.Load>()
            .switchMap {
                wrap(getCustomerSupportType.get().execute())
            }
            .map { result ->
                if (result is Result.Success) {
                    CustomerSupportOptionContract.PartialState.SetSupportData(
                        supportType = result.value,
                        supportCallNumber = getCustomerSupportData.get().getCustomerCareCallNumber(),
                        supportChatNumber = getCustomerSupportData.get().getCustomerCareChatNumber(),
                        support24x7String = getCustomerSupportData.get().get24x7String()
                    )
                } else
                    CustomerSupportOptionContract.PartialState.NoChange
            }
    }

    private fun actionClicked() = intent<CustomerSupportOptionContract.Intent.ActionCallClicked>()
        .map {
            getCurrentState().let { state ->
                accountingEventTracker.get().trackLedgerPopUpAction(
                    accountId = state.accountId,
                    type = SupportType.CALL.value.lowercase(),
                    txnId = state.txnId,
                    amount = state.amount,
                    relation = state.ledgerType,
                    supportMsg = it.msg,
                    action = "clicked",
                    supportNumber = state.supportCallNumber,
                    source = state.source,
                )
                emitViewEvent(CustomerSupportOptionContract.ViewEvents.CallCustomerCare)
                CustomerSupportOptionContract.PartialState.SetActionClicked(true)
            }
        }

    private fun sendWhatsAppMessage() = intent<CustomerSupportOptionContract.Intent.SendWhatsAppMessage>()
        .switchMap {

            getCurrentState().let { state ->
                accountingEventTracker.get().trackLedgerPopUpAction(
                    accountId = state.accountId,
                    type = SupportType.CHAT.value.lowercase(),
                    txnId = state.txnId,
                    amount = state.amount,
                    relation = state.ledgerType,
                    supportMsg = it.msg,
                    action = "clicked",
                    supportNumber = state.supportChatNumber,
                    source = state.source,

                )
                wrap(
                    communicationRepository.get().goToWhatsApp(
                        ShareIntentBuilder(
                            shareText = it.msg,
                            phoneNumber = state.supportChatNumber
                        )
                    )
                )
            }
        }
        .map {
            when (it) {
                is Result.Progress -> CustomerSupportOptionContract.PartialState.NoChange
                is Result.Success -> {
                    emitViewEvent(CustomerSupportOptionContract.ViewEvents.SendWhatsAppMessage(it.value))
                    CustomerSupportOptionContract.PartialState.SetActionClicked(true)
                }
                is Result.Failure -> {
                    if (it.error is IntentHelper.NoWhatsAppError) {
                        emitViewEvent(CustomerSupportOptionContract.ViewEvents.ShowWhatsAppError)
                    } else {
                        emitViewEvent(CustomerSupportOptionContract.ViewEvents.ShowDefaultError)
                    }
                    CustomerSupportOptionContract.PartialState.NoChange
                }
            }
        }

    override fun reduce(
        currentState: CustomerSupportOptionContract.State,
        partialState: CustomerSupportOptionContract.PartialState,
    ): CustomerSupportOptionContract.State {
        return when (partialState) {
            CustomerSupportOptionContract.PartialState.NoChange -> currentState
            is CustomerSupportOptionContract.PartialState.SetSupportData -> currentState.copy(
                supportType = partialState.supportType,
                supportCallNumber = partialState.supportCallNumber,
                supportChatNumber = partialState.supportChatNumber,
                support24x7String = partialState.support24x7String,
            )
            is CustomerSupportOptionContract.PartialState.SetActionClicked -> currentState.copy(isActionClicked = partialState.clicked)
        }
    }
}
