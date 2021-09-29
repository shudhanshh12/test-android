package `in`.okcredit.payment.ui.payment_destination

import `in`.okcredit.payment.R
import `in`.okcredit.payment.server.internal.PaymentDestinationType
import `in`.okcredit.payment.ui.payment_destination.PaymentDestinationContract.*
import `in`.okcredit.payment.usecases.SetPaymentDestinationToServer
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class PaymentDestinationViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam(PaymentDestinationDialog.ARG_SOURCE) private val adoptionSource: String,
    private val setPaymentDestination: Lazy<SetPaymentDestinationToServer>,
    private val context: Lazy<Context>
) : BaseViewModel<State, PartialState, ViewEvents>(initialState) {

    private var destinationType = PaymentDestinationType.BANK.value
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(

            setAdoptionModeObservable(),

            setBankDestinationObservable(),

            enteredAccountNumberObservable(),

            enteredIfscObservable(),

            enteredUpiObservable()
        )
    }

    private fun enteredUpiObservable(): Observable<PartialState.EnteredUpi>? {
        return intent<Intent.EnteredUPI>()
            .map {
                PartialState.EnteredUpi(it.enteredUPI)
            }
    }

    private fun enteredIfscObservable(): Observable<PartialState.EnteredIfsc>? {
        return intent<Intent.EnteredIfsc>()
            .map {
                PartialState.EnteredIfsc(it.enteredIfsc)
            }
    }

    private fun enteredAccountNumberObservable(): Observable<PartialState.EnteredAccountNumber>? {
        return intent<Intent.EnteredAccountNumber>()
            .map {
                PartialState.EnteredAccountNumber(it.enteredAccountNumber)
            }
    }

    private fun setAdoptionModeObservable(): Observable<PartialState>? {
        return intent<Intent.SetAdoptionMode>()
            .map {
                if (it.adoptionMode == PaymentDestinationType.BANK.value) {
                    PartialState.ShowBankUi
                } else {
                    PartialState.ShowUpiUi
                }
            }
    }

    private fun setBankDestinationObservable(): Observable<PartialState>? {
        return intent<Intent.SetDestinationToServer>()
            .switchMap {
                destinationType = it.paymentType
                setPaymentDestination.get().execute(
                    adoptionSource = adoptionSource,
                    paymentType = it.paymentType,
                    paymentAddress = it.paymentAddress
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.ShowValidateUi
                    is Result.Success -> {
                        emitViewEvent(
                            ViewEvents.OnAccountAddedSuccessfully
                        )
                        PartialState.ShowSuccessUi(it.value.destination.name)
                    }
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> {
                                emitViewEvent(
                                    ViewEvents.ShowError(
                                        context.get().getString(R.string.payment_no_internet_connection)
                                    )
                                )
                            }
                            else -> {
                                if (it.error.message.isNotNullOrBlank()) {
                                    emitViewEvent(ViewEvents.ShowError(it.error.message!!))
                                } else {
                                    emitViewEvent(
                                        ViewEvents.ShowError(
                                            context.get().getString(R.string.payment_error_not_able_to_Add_details)
                                        )
                                    )
                                }
                            }
                        }
                        if (destinationType == PaymentDestinationType.BANK.value) {
                            PartialState.ShowBankUi
                        } else {
                            PartialState.ShowUpiUi
                        }
                    }
                }
            }
    }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            is PartialState.EnteredAccountNumber -> currentState.copy(enteredAccountNumber = partialState.enteredAccountNumber)
            is PartialState.EnteredIfsc -> currentState.copy(enteredIfsc = partialState.enteredIfsc)
            is PartialState.EnteredUpi -> currentState.copy(enteredUpi = partialState.enteredUPI)
            is PartialState.ShowSuccessUi -> currentState.copy(
                showUi = UiScreenType.SUCCESS,
                accountHolderName = partialState.accountHolderName
            )
            PartialState.ShowValidateUi -> currentState.copy(showUi = UiScreenType.VALIDATE)
            PartialState.ShowBankUi -> currentState.copy(
                showUi = UiScreenType.BANK,
                adoptionMode = PaymentDestinationType.BANK.value
            )
            PartialState.ShowUpiUi -> currentState.copy(
                showUi = UiScreenType.UPI,
                adoptionMode = PaymentDestinationType.UPI.value
            )
        }
    }
}
