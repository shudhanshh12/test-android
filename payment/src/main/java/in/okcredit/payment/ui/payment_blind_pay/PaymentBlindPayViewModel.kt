package `in`.okcredit.payment.ui.payment_blind_pay

import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.BLIND_PAY_ENTER_AMOUNT_PAGE
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.contract.model.SupportType
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportData
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportType
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class PaymentBlindPayViewModel @Inject constructor(
    private val initialState: PaymentBlindPayContract.State,
    @ViewModelParam(PaymentBlindPayContract.ARG_ACCOUNT_TYPE) val ledgerType: LedgerType,
    @ViewModelParam(PaymentBlindPayContract.ARG_ACCOUNT_ID) val accountId: String,
    private val getCustomerSupportType: Lazy<GetCustomerSupportType>,
    private val paymentAnalyticsEvents: Lazy<PaymentAnalyticsEvents>,
    private val communicationRepository: Lazy<CommunicationRepository>,
    private val getCustomerSupportData: Lazy<GetCustomerSupportData>,
) : BaseViewModel<PaymentBlindPayContract.State, PaymentBlindPayContract.PartialState, PaymentBlindPayContract.ViewEvents>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<PaymentBlindPayContract.State>> {
        return Observable.mergeArray(
            setEnteredAmount(),
            getCustomerSupportType(),
            actionSupportClicked(),
            sendHelpWhatsAppMessage()
        )
    }

    private fun setEnteredAmount(): Observable<PaymentBlindPayContract.PartialState>? {
        return intent<PaymentBlindPayContract.Intent.SetAmountEntered>()
            .map {
                PaymentBlindPayContract.PartialState.SetAmountEntered(it.amount)
            }
    }

    private fun getCustomerSupportType(): Observable<PaymentBlindPayContract.PartialState> {
        return intent<PaymentBlindPayContract.Intent.Load>()
            .switchMap {
                wrap(getCustomerSupportType.get().execute())
            }
            .map {
                if (it is Result.Success) {
                    val supportNumber = getCustomerSupportData.get().getCustomerSupportNumber(it.value)
                    val support24x7String = getCustomerSupportData.get().get24x7String()
                    emitViewEvent(
                        PaymentBlindPayContract.ViewEvents.TrackPageSummaryEvent(
                            it.value.value,
                            supportNumber
                        )
                    )
                    if (it.value.value.isNotBlank())
                        PaymentBlindPayContract.PartialState.SetSupportData(
                            it.value,
                            supportNumber,
                            support24x7String
                        )
                    else PaymentBlindPayContract.PartialState.NoChange
                } else
                    PaymentBlindPayContract.PartialState.NoChange
            }
    }

    private fun actionSupportClicked(): Observable<PaymentBlindPayContract.PartialState> {
        return intent<PaymentBlindPayContract.Intent.SupportClicked>()
            .map {
                paymentAnalyticsEvents.get().trackCustomerSupportMsgClicked(
                    source = BLIND_PAY_ENTER_AMOUNT_PAGE,
                    type = getCurrentState().supportType.value,
                    relation = ledgerType.value.lowercase(),
                    supportMsg = it.msg
                )
                if (getCurrentState().supportType == SupportType.CALL)
                    emitViewEvent(PaymentBlindPayContract.ViewEvents.CallCustomerCare)
                else pushIntent(PaymentBlindPayContract.Intent.SendWhatsAppMessage(it.msg, it.number))
                PaymentBlindPayContract.PartialState.NoChange
            }
    }

    private fun sendHelpWhatsAppMessage() = intent<PaymentBlindPayContract.Intent.SendWhatsAppMessage>()
        .switchMap {
            wrap(
                communicationRepository.get().goToWhatsAppWithTextOnlyExtendedBahaviuor(
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
                    emitViewEvent(PaymentBlindPayContract.ViewEvents.SendWhatsAppMessage(it.value))
                }
                is Result.Failure -> {
                    if (it.error is IntentHelper.NoWhatsAppError) {
                        emitViewEvent(PaymentBlindPayContract.ViewEvents.ShowWhatsAppError)
                    } else {
                        emitViewEvent(PaymentBlindPayContract.ViewEvents.ShowDefaultError)
                    }
                }

                else -> {
                }
            }

            PaymentBlindPayContract.PartialState.NoChange
        }

    override fun reduce(
        currentState: PaymentBlindPayContract.State,
        partialState: PaymentBlindPayContract.PartialState,
    ): PaymentBlindPayContract.State {
        return when (partialState) {
            PaymentBlindPayContract.PartialState.NoChange -> currentState
            is PaymentBlindPayContract.PartialState.SetAmountEntered -> currentState.copy(
                currentAmountSelected = partialState.amount
            )
            is PaymentBlindPayContract.PartialState.SetSupportData -> currentState.copy(
                supportType = partialState.supportType,
                supportNumber = partialState.supportNumber,
                support24x7String = partialState.support24x7String,
            )
        }
    }
}
