package `in`.okcredit.merchant.customer_ui.ui.buyer_txn_alert

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.customer_ui.ui.buyer_txn_alert.CustomerTxnAlertDialogContract.*
import `in`.okcredit.merchant.customer_ui.usecase.GetLatestPaymentAddedByCustomer
import `in`.okcredit.merchant.customer_ui.usecase.SchedulerCustomerTxnALertDialogWorker
import `in`.okcredit.merchant.customer_ui.usecase.TxnAlertAllowAction
import `in`.okcredit.merchant.customer_ui.usecase.TxnAlertAllowAction.Action.ALLOWED
import `in`.okcredit.merchant.customer_ui.usecase.TxnAlertDenyAction
import `in`.okcredit.merchant.customer_ui.usecase.TxnAlertDenyAction.Action
import `in`.okcredit.merchant.customer_ui.usecase.UpdateAddTransactionRestrictionForCustomer
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class CustomerTxnAlertDialogViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam("customer_id") val customerId: String,
    @ViewModelParam("description") val description: String,
    @ViewModelParam("mobile") val mobile: String?,
    @ViewModelParam("profilePic") val profilePic: String?,
    private val navigator: Navigator,
    private val getLatestPaymentAddedByCustomer: Lazy<GetLatestPaymentAddedByCustomer>,
    private val getCustomer: Lazy<GetCustomer>,
    private val txnAlertAllowAction: Lazy<TxnAlertAllowAction>,
    private val txnAlertDenyAction: Lazy<TxnAlertDenyAction>,
    private val schedulerCustomerTxnAlertDialogWorker: Lazy<SchedulerCustomerTxnALertDialogWorker>,
    private val updateAddTransactionRestrictionForCustomer: Lazy<UpdateAddTransactionRestrictionForCustomer>
) : BasePresenter<State, PartialState>(initialState) {

    private var denySubject = PublishSubject.create<Unit>()
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.Load>()
                .switchMap { UseCase.wrapSingle(getLatestPaymentAddedByCustomer.get().execute(customerId)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetAmount(it.value.amountV2.div(100), it.value.type)
                        }
                        is Result.Failure -> {
                            PartialState.NoChange
                        }
                    }
                },
            intent<Intent.Load>()
                .switchMap { UseCase.wrapObservable(getCustomer.get().execute(customerId)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetCustomer(mobile, description, profilePic, customerId)
                        }
                        is Result.Failure -> {
                            PartialState.NoChange
                        }
                    }
                },
            intent<Intent.AllowAction>()
                .switchMap {
                    txnAlertAllowAction.get()
                        .execute(TxnAlertAllowAction.Request(accountID = customerId, action = ALLOWED))
                }.map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            navigator.showAllowPopUp()
                            PartialState.NoChange
                        }
                        is Result.Failure -> {
                            PartialState.NoChange
                        }
                    }
                },

            intent<Intent.DenyAction>()
                .switchMap {
                    txnAlertDenyAction.get()
                        .execute(
                            TxnAlertDenyAction.Request(
                                accountID = customerId,
                                action = Action.DENY
                            )
                        )
                }.map {

                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            denySubject.onNext(Unit)
                            PartialState.NoChange
                        }
                        is Result.Failure -> {
                            PartialState.NoChange
                        }
                    }
                },
            intent<Intent.Dismiss>()
                .switchMap {
                    schedulerCustomerTxnAlertDialogWorker.get().execute(SchedulerCustomerTxnALertDialogWorker.Request(customerId))
                }.map {
                    PartialState.NoChange
                },
            denySubject.switchMap {
                updateAddTransactionRestrictionForCustomer.get()
                    .execute(UpdateAddTransactionRestrictionForCustomer.Request(customerId))
            }.map {
                navigator.showDenyPopUp()
                PartialState.NoChange
            }
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.SetAmount -> currentState.copy(amount = partialState.amount, type = partialState.type)
            is PartialState.SetCustomer -> currentState.copy(
                name = partialState.description,
                mobile = partialState.mobile,
                profilePic = partialState.profilePic,
                customerId = partialState.customerId
            )
        }
    }
}
