package `in`.okcredit.collection_ui.ui.passbook.refund

import `in`.okcredit.collection.contract.CollectionServerErrors
import `in`.okcredit.collection.contract.PayoutType
import `in`.okcredit.collection.contract.TriggerMerchantPayout
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.OnlineCollectionTracker
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.network.NetworkError
import javax.inject.Inject

class RefundConsentViewModel @Inject constructor(
    initialState: RefundConsentContract.State,
    private val triggerMerchantPayout: Lazy<TriggerMerchantPayout>,
    private val context: Lazy<Context>,
    private val onlineCollectionTracker: Lazy<OnlineCollectionTracker>,
) : BaseViewModel<RefundConsentContract.State, RefundConsentContract.PartialState, RefundConsentContract.ViewEvents>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<RefundConsentContract.State>> {
        return Observable.mergeArray(
            triggerMerchantRefund(),
            cancelRefund(),
            loadEvent(),
        )
    }

    private fun loadEvent() = intent<RefundConsentContract.Intent.Load>()
        .map {
            onlineCollectionTracker.get().trackRefundToCustomerPopUpOpened(getCurrentState().txnId)
            RefundConsentContract.PartialState.NoChange
        }

    private fun triggerMerchantRefund() = intent<RefundConsentContract.Intent.InitiateRefund>()
        .switchMap {
            onlineCollectionTracker.get().trackClickedOnRefundOnRefundDialog(getCurrentState().txnId)
            wrap(
                triggerMerchantPayout.get()
                    .executeRefund(
                        PayoutType.REFUND.value,
                        getCurrentState().collectionType,
                        getCurrentState().payoutId,
                        getCurrentState().paymentId,
                        getCurrentState().txnId,
                    )
            )
        }
        .map {
            when (it) {
                is Result.Progress -> RefundConsentContract.PartialState.StartLoader
                is Result.Success -> {
                    emitViewEvent(RefundConsentContract.ViewEvents.RefundSuccessful)
                    RefundConsentContract.PartialState.StopLoader
                }
                is Result.Failure -> {
                    when (it.error) {
                        is CollectionServerErrors.DuplicatePayoutRequest -> {
                            emitViewEvent(
                                RefundConsentContract.ViewEvents.ShowError(
                                    context.get()
                                        .getString(R.string.refund_already_initiated)
                                )
                            )
                        }
                        is NetworkError -> {
                            emitViewEvent(
                                RefundConsentContract.ViewEvents.ShowError(
                                    context.get()
                                        .getString(R.string.err_network)
                                )
                            )
                        }
                        else -> {
                            emitViewEvent(
                                RefundConsentContract.ViewEvents.ShowError(
                                    context.get()
                                        .getString(R.string.err_default)
                                )
                            )
                        }
                    }
                    RefundConsentContract.PartialState.StopLoader
                }
            }
        }

    private fun cancelRefund() =
        intent<RefundConsentContract.Intent.Cancel>()
            .map {
                onlineCollectionTracker.get().trackRefundToCustomerCancelled(getCurrentState().txnId)
                emitViewEvent(RefundConsentContract.ViewEvents.Cancel)
                RefundConsentContract.PartialState.NoChange
            }

    override fun reduce(
        currentState: RefundConsentContract.State,
        partialState: RefundConsentContract.PartialState,
    ): RefundConsentContract.State {
        return when (partialState) {
            RefundConsentContract.PartialState.NoChange -> currentState
            RefundConsentContract.PartialState.StartLoader -> currentState.copy(showLoader = true)
            RefundConsentContract.PartialState.StopLoader -> currentState.copy(showLoader = false)
        }
    }
}
