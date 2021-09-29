package merchant.okcredit.accounting.ui.customer_support_exit_dialog

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.contract.model.SupportType
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportType
import merchant.okcredit.accounting.usecases.GetCustomerSupportDataImpl
import merchant.okcredit.accounting.usecases.GetCustomerSupportPreference
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import javax.inject.Inject

class CustomerSupportExitViewModel @Inject constructor(
    initialState: CustomerSupportExitContract.State,
    private val getCustomerSupportType: Lazy<GetCustomerSupportType>,
    private val communicationRepository: Lazy<CommunicationRepository>,
    private val accountingEventTracker: Lazy<AccountingEventTracker>,
    private val getCustomerSupportPreference: Lazy<GetCustomerSupportPreference>,
    private val getCustomerSupportData: Lazy<GetCustomerSupportDataImpl>,
) : BaseViewModel<CustomerSupportExitContract.State, CustomerSupportExitContract.PartialState, CustomerSupportExitContract.ViewEvents>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<CustomerSupportExitContract.State>> {
        return Observable.mergeArray(
            getCustomerSupportType(),
            sendWhatsAppMessage(),
            actionClicked(),
            setDialogShownPreference()
        )
    }

    private fun setDialogShownPreference(): Observable<CustomerSupportExitContract.PartialState> {
        return intent<CustomerSupportExitContract.Intent.Load>()
            .switchMap {
                wrap(getCustomerSupportPreference.get().setCustomerSupportExitPreference())
            }
            .map {
                CustomerSupportExitContract.PartialState.NoChange
            }
    }

    private fun getCustomerSupportType(): Observable<CustomerSupportExitContract.PartialState> {
        return intent<CustomerSupportExitContract.Intent.Load>()
            .switchMap {
                wrap(getCustomerSupportType.get().execute())
            }
            .map { result ->
                if (result is `in`.okcredit.shared.usecase.Result.Success) {
                    CustomerSupportExitContract.PartialState.SetSupportData(
                        supportType = result.value,
                        supportNumber = getCustomerSupportData.get().getCustomerSupportNumber(result.value),
                        support24x7String = getCustomerSupportData.get().get24x7String(),
                    )
                } else
                    CustomerSupportExitContract.PartialState.NoChange
            }
    }

    private fun actionClicked() = intent<CustomerSupportExitContract.Intent.ActionClicked>()
        .map {
            getCurrentState().let { state ->
                accountingEventTracker.get().trackExitPopUpAction(
                    accountId = state.accountId,
                    source = state.source,
                    type = state.supportType.value,
                    relation = state.ledgerType.lowercase(),
                    supportMsg = state.supportMsg,
                    action = "clicked",
                    supportNumber = it.number
                )
                if (getCurrentState().supportType == SupportType.CALL)
                    emitViewEvent(CustomerSupportExitContract.ViewEvents.CallCustomerCare)
                else pushIntent(
                    CustomerSupportExitContract.Intent.SendWhatsAppMessage(
                        getCurrentState().supportMsg,
                        it.number
                    )
                )
                CustomerSupportExitContract.PartialState.SetActionClicked(true)
            }
        }

    private fun sendWhatsAppMessage() = intent<CustomerSupportExitContract.Intent.SendWhatsAppMessage>()
        .switchMap {
            wrap(
                communicationRepository.get().goToWhatsApp(
                    ShareIntentBuilder(
                        shareText = it.msg,
                        phoneNumber = it.number
                    )
                )
            )
        }
        .map {
            when (it) {
                is Result.Success -> {
                    emitViewEvent(CustomerSupportExitContract.ViewEvents.SendWhatsAppMessage(it.value))
                }
                is Result.Failure -> {
                    if (it.error is IntentHelper.NoWhatsAppError) {
                        emitViewEvent(CustomerSupportExitContract.ViewEvents.ShowWhatsAppError)
                    } else {
                        emitViewEvent(CustomerSupportExitContract.ViewEvents.ShowDefaultError)
                    }
                }

                else -> {
                }
            }

            CustomerSupportExitContract.PartialState.NoChange
        }

    override fun reduce(
        currentState: CustomerSupportExitContract.State,
        partialState: CustomerSupportExitContract.PartialState,
    ): CustomerSupportExitContract.State {
        return when (partialState) {
            CustomerSupportExitContract.PartialState.NoChange -> currentState
            is CustomerSupportExitContract.PartialState.SetSupportData -> currentState.copy(
                supportType = partialState.supportType,
                supportNumber = partialState.supportNumber,
                support24x7String = partialState.support24x7String,
            )
            is CustomerSupportExitContract.PartialState.SetActionClicked -> currentState.copy(isActionClicked = partialState.clicked)
        }
    }
}
