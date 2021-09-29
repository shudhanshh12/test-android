package `in`.okcredit.merchant.customer_ui.ui.subscription.detail

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.data.server.model.request.DayOfWeek
import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionStatus
import `in`.okcredit.merchant.customer_ui.ui.subscription.detail.SubscriptionDetailContract.*
import `in`.okcredit.merchant.customer_ui.ui.subscription.detail.SubscriptionDetailContract.Companion.ARG_CUSTOMER_ID
import `in`.okcredit.merchant.customer_ui.ui.subscription.detail.SubscriptionDetailContract.Companion.ARG_SUBSCRIPTION_ID
import `in`.okcredit.merchant.customer_ui.ui.subscription.detail.SubscriptionDetailContract.Companion.ARG_SUBSCRIPTION_OBJECT
import `in`.okcredit.merchant.customer_ui.ui.subscription.usecase.DeleteSubscription
import `in`.okcredit.merchant.customer_ui.ui.subscription.usecase.GetSubscription
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class SubscriptionDetailViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam(ARG_CUSTOMER_ID) val customerId: String,
    @ViewModelParam(ARG_SUBSCRIPTION_ID) val subscriptionId: String,
    @ViewModelParam(ARG_SUBSCRIPTION_OBJECT) var subscription: Subscription?,
    private val getCustomer: Lazy<GetCustomer>,
    private val getSubscription: Lazy<GetSubscription>,
    private val deleteSubscription: Lazy<DeleteSubscription>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    override fun handle(): Observable<PartialState> {
        return Observable.mergeArray(
            observeLoadCustomer(),
            observeLoadIntent(),
            observeDeleteIntent(),
            observeDeleteConfirmIntent()
        )
    }

    private fun observeLoadCustomer() = intent<Intent.Load>()
        .switchMap {
            getCustomer.get().execute(customerId)
        }.map {
            emitViewEvent(ViewEvent.CustomerLoaded(customerId, it.mobile))
            PartialState.SetCustomerDetail(it)
        }

    private fun observeDeleteIntent() = intent<Intent.DeleteSubscription>().map {
        emitViewEvent(ViewEvent.ShowDeleteConfirm)
        PartialState.NoChange
    }

    private fun observeDeleteConfirmIntent() = intent<Intent.DeleteConfirmed>()
        .switchMap {
            emitViewEvent(ViewEvent.ShowDeleteLoader)
            deleteSubscription.get().execute(
                subscription!!.copy(status = SubscriptionStatus.DELETED.value)
            )
        }
        .map {
            when (it) {
                is Result.Success -> {
                    emitViewEvent(ViewEvent.HideDeleteLoader)
                    PartialState.SubscriptionDeleted
                }
                is Result.Progress -> PartialState.ShowProgress
                is Result.Failure -> {
                    emitViewEvent(ViewEvent.HideDeleteLoader)
                    when {
                        isInternetIssue(it.error) -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.interent_error))
                        }
                        else -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                        }
                    }
                    PartialState.NoChange
                }
            }
        }

    private fun observeLoadIntent() = intent<Intent.Load>()
        .switchMap {
            if (subscription != null) {
                Observable.just(Result.Success(subscription!!))
            } else {
                getSubscription.get().execute(subscriptionId = subscriptionId)
            }
        }.map {
            when (it) {
                is Result.Success -> {
                    this.subscription = it.value
                    PartialState.SubscriptionDetail(it.value)
                }
                is Result.Progress -> PartialState.ShowProgress
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.interent_error))
                        }
                        else -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                        }
                    }
                    PartialState.NoChange
                }
            }
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            PartialState.ShowProgress -> currentState
            PartialState.SubscriptionDeleted -> currentState.copy(
                status = SubscriptionStatus.DELETED
            )
            is PartialState.SubscriptionDetail -> currentState.copy(
                amount = partialState.subscription.amount,
                name = partialState.subscription.name,
                frequency = SubscriptionFrequency.getFrequency(partialState.subscription.frequency),
                status = SubscriptionStatus.getStatus(partialState.subscription.status),
                startDate = partialState.subscription.startDate,
                nexDate = partialState.subscription.nextSchedule,
                daysInWeek = partialState.subscription.week?.map { DayOfWeek.getDay(it) }
            )
            is PartialState.SetCustomerDetail -> currentState.copy(
                customer = partialState.customer
            )
        }
    }
}
