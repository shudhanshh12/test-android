package `in`.okcredit.payment.ui.blindpay

import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.contract.model.SupportType
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportData
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportType
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import javax.inject.Inject

class BlindPayViewModel @Inject constructor(
    initialState: BlindPayContract.State,
    private val getCustomerSupportType: Lazy<GetCustomerSupportType>,
    private val communicationRepository: Lazy<CommunicationRepository>,
    private val paymentAnalyticsEvents: Lazy<PaymentAnalyticsEvents>,
    private val getCustomerSupportData: Lazy<GetCustomerSupportData>,
) : BaseViewModel<BlindPayContract.State, BlindPayContract.PartialState, BlindPayContract.ViewEvents>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<BlindPayContract.State>> {
        return Observable.mergeArray(
            getCustomerSupportType(),
            actionSupportClicked(),
            sendWhatsAppMessage(),
        )
    }

    private fun getCustomerSupportType(): Observable<BlindPayContract.PartialState> {
        return intent<BlindPayContract.Intent.Load>()
            .switchMap {
                wrap(getCustomerSupportType.get().execute())
            }
            .map {
                if (it is Result.Success) {
                    if (it.value.value.isNotBlank()) {
                        paymentAnalyticsEvents.get().trackCustomerSupportOptMsgShown(
                            accountId = getCurrentState().accountId,
                            supportType = it.value.value,
                            msg = getCurrentState().supportMsg,
                            number = getCustomerSupportData.get().getCustomerSupportNumber(it.value),
                            type = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_OPTIONS.lowercase()
                        )
                        BlindPayContract.PartialState.SetSupportData(
                            it.value,
                            getCustomerSupportData.get().getCustomerSupportNumber(it.value),
                            getCustomerSupportData.get().get24x7String()
                        )
                    } else BlindPayContract.PartialState.NoChange
                } else
                    BlindPayContract.PartialState.NoChange
            }
    }

    private fun sendWhatsAppMessage() = intent<BlindPayContract.Intent.SendWhatsAppMessage>()
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
                    emitViewEvent(BlindPayContract.ViewEvents.SendWhatsAppMessage(it.value))
                }
                is Result.Failure -> {
                    if (it.error is IntentHelper.NoWhatsAppError) {
                        emitViewEvent(BlindPayContract.ViewEvents.ShowWhatsAppError)
                    } else {
                        emitViewEvent(BlindPayContract.ViewEvents.ShowDefaultError)
                    }
                }

                else -> {
                }
            }

            BlindPayContract.PartialState.NoChange
        }

    private fun actionSupportClicked(): Observable<BlindPayContract.PartialState> {
        return intent<BlindPayContract.Intent.SupportClicked>()
            .map {
                paymentAnalyticsEvents.get().trackCustomerSupportMsgClicked(
                    source = PaymentAnalyticsEvents.PaymentPropertyValue.BLIND_PAY_CHOOSE_OPTION_PAGE,
                    type = getCurrentState().supportType.value,
                    relation = getCurrentState().ledgerType,
                    supportMsg = it.msg
                )
                if (getCurrentState().supportType == SupportType.CALL)
                    emitViewEvent(BlindPayContract.ViewEvents.CallCustomerCare)
                else pushIntent(BlindPayContract.Intent.SendWhatsAppMessage(it.msg, it.number))
                BlindPayContract.PartialState.NoChange
            }
    }

    override fun reduce(
        currentState: BlindPayContract.State,
        partialState: BlindPayContract.PartialState,
    ): BlindPayContract.State {
        return when (partialState) {
            BlindPayContract.PartialState.NoChange -> currentState
            is BlindPayContract.PartialState.SetSupportData -> currentState.copy(
                supportType = partialState.supportType,
                supportNumber = partialState.supportNumber,
                support24x7String = partialState.support24x7String,
            )
        }
    }
}
