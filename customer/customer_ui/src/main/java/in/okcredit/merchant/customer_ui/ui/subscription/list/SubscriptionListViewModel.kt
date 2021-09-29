package `in`.okcredit.merchant.customer_ui.ui.subscription.list

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.customer_ui.data.server.model.request.DayOfWeek
import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionStatus
import `in`.okcredit.merchant.customer_ui.ui.subscription.list.SubscriptionListContract.*
import `in`.okcredit.merchant.customer_ui.ui.subscription.list.epoxy.SubscriptionItem
import `in`.okcredit.merchant.customer_ui.ui.subscription.usecase.ListSubscriptions
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class SubscriptionListViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam("customer_id") val customerId: String?,
    private val listSubscriptions: Lazy<ListSubscriptions>,
    private val getCustomer: Lazy<GetCustomer>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private val subscriptionList = mutableListOf<Subscription>()
    private var customerMobile: String? = null

    override fun handle(): Observable<PartialState> {
        return Observable.mergeArray(
            observeLoadCustomer(),
            observeListSubscriptions(),
            observeAddSubscription(),
            observeRefreshData(),
            observeItemClicked()
        )
    }

    private fun observeLoadCustomer() = intent<Intent.Load>().switchMap {
        getCustomer.get().execute(customerId)
    }.map {
        customerMobile = it.mobile
        PartialState.SetCustomerName(it.description)
    }

    private fun observeItemClicked() = intent<Intent.ItemClicked>().map { itemClicked ->
        val subscription = subscriptionList.firstOrNull { it.id == itemClicked.id }
        emitViewEvent(ViewEvent.GoToSubscriptionDetail(itemClicked.id, customerId, subscription))
        PartialState.NoChange
    }

    private fun observeRefreshData() = intent<Intent.RefreshData>().map {
        reload.onNext(Unit)
        PartialState.NoChange
    }

    private fun observeAddSubscription() = intent<Intent.AddSubscriptionClicked>().map {
        emitViewEvent(ViewEvent.GoToAddSubscription(customerId, customerMobile))
        PartialState.NoChange
    }

    private fun observeListSubscriptions() = Observable
        .merge(intent<Intent.Load>(), reload)
        .switchMap { listSubscriptions.get().execute(customerId) }
        .map {
            when (it) {
                is Result.Success -> {
                    subscriptionList.clear()
                    subscriptionList.addAll(it.value)
                    PartialState.SubscriptionList(it.value)
                }
                is Result.Progress -> PartialState.ShowProgress
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> {
                            PartialState.ShowNetworkError
                        }
                        else -> {
                            PartialState.ShowServerError
                        }
                    }
                }
            }
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            PartialState.ShowProgress -> currentState.copy(
                loading = true
            )
            is PartialState.ShowNetworkError -> {
                emitViewEvent(ViewEvent.NetworkErrorToast)
                currentState.copy(
                    loading = false
                )
            }
            is PartialState.ShowServerError -> {
                emitViewEvent(ViewEvent.ServerErrorToast)
                currentState.copy(
                    loading = false
                )
            }
            is PartialState.SubscriptionList -> currentState.copy(
                list = transformData(partialState.list),
                loading = false
            )
            is PartialState.SetCustomerName -> currentState.copy(
                customerName = partialState.name
            )
        }
    }

    private fun transformData(responseList: List<Subscription>): List<SubscriptionItem> {
        val list = mutableListOf<SubscriptionItem>()
        responseList
            .filter {
                SubscriptionStatus.getStatus(it.status) == SubscriptionStatus.ACTIVE || SubscriptionStatus.getStatus(
                    it.status
                ) == SubscriptionStatus.DELETED
            }
            .sortedByDescending { it.updateTime }
            .forEach { subscription ->
                val item = SubscriptionItem(
                    id = subscription.id,
                    name = subscription.name,
                    frequency = SubscriptionFrequency.getFrequency(subscription.frequency),
                    startDate = subscription.startDate,
                    daysInWeek = subscription.week?.map { day -> DayOfWeek.getDay(day) }
                )
                list.add(item)
            }
        return list
    }
}
