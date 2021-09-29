package `in`.okcredit.merchant.customer_ui.ui.subscription.add

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.data.server.model.request.DayOfWeek
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.merchant.customer_ui.ui.subscription.add.AddSubscriptionContract.*
import `in`.okcredit.merchant.customer_ui.ui.subscription.add.AddSubscriptionContract.PartialState.NoChange
import `in`.okcredit.merchant.customer_ui.ui.subscription.usecase.AddSubscription
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import androidx.annotation.VisibleForTesting
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class AddSubscriptionViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam("customer_id") val customerId: String?,
    private val getCustomer: Lazy<GetCustomer>,
    private val addSubscription: Lazy<AddSubscription>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var name: String? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var daysInWeek: List<DayOfWeek>? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var selectedFrequency: SubscriptionFrequency? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var startDate: Long? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var amount: Long = 0L

    override fun handle(): Observable<PartialState> {
        return Observable.mergeArray(
            observeInitialFrequency(),
            observeCalculatorCallback(),
            observeAddNameClicked(),
            observeNameAdded(),
            observeFrequencyClicked(),
            observeFrequencyAdded(),
            loadCustomerDetails(),
            observeSubmitClicked()
        )
    }

    private fun observeInitialFrequency() = intent<Intent.Load>().map {
        selectedFrequency = SubscriptionFrequency.DAILY
        PartialState.FrequencyAdded(SubscriptionFrequency.DAILY, null, null)
    }

    private fun observeSubmitClicked() = intent<Intent.SubmitClicked>()
        .switchMap {
            addSubscription.get().execute(
                customerId = customerId!!,
                amount = (amount),
                name = name!!,
                frequency = selectedFrequency!!,
                startDate = startDate?.div(1000),
                days = daysInWeek
            ).map {
                when (it) {
                    is Result.Progress -> PartialState.ShowProgress
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.Success(it.value.startDate))
                        NoChange
                    }
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.interent_error))
                                NoChange
                            }
                            else -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                                NoChange
                            }
                        }
                    }
                }
            }
        }

    private fun loadCustomerDetails() = intent<Intent.Load>()
        .take(1)
        .switchMap {
            UseCase.wrapObservable(getCustomer.get().execute(customerId ?: ""))
        }
        .map {
            when (it) {
                is Result.Success -> {
                    PartialState.CustomerData(it.value)
                }
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.interent_error))
                            NoChange
                        }
                        else -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                            NoChange
                        }
                    }
                }
                is Result.Progress -> PartialState.ShowProgress
            }
        }

    private fun observeFrequencyClicked() = intent<Intent.AddFrequencyClicked>().map {
        emitViewEvent(ViewEvent.GoToAddFrequency(selectedFrequency, daysInWeek, startDate))
        NoChange
    }

    private fun observeFrequencyAdded() = intent<Intent.FrequencyAdded>().map {
        this.selectedFrequency = it.selectedFrequency
        this.daysInWeek = it.daysInWeek
        this.startDate = it.startDate
        PartialState.FrequencyAdded(it.selectedFrequency, it.daysInWeek, it.startDate)
    }

    private fun observeAddNameClicked() = intent<Intent.AddNameClicked>().map {
        emitViewEvent(ViewEvent.GoToAddName(name))
        NoChange
    }

    private fun observeNameAdded() = intent<Intent.NameAdded>().map {
        this.name = it.name
        PartialState.NameAdded(it.name)
    }

    private fun observeCalculatorCallback() = intent<Intent.CalculatorData>()
        .map {
            this.amount = it.amount
            PartialState.CalculatorData(it.amount, it.replace)
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            NoChange -> currentState
            is PartialState.CalculatorData -> currentState.copy(
                amount = partialState.amount,
                amountCalculation = partialState.amountCalculation
            )
            is PartialState.NameAdded -> currentState.copy(
                name = partialState.name
            )
            is PartialState.FrequencyAdded -> currentState.copy(
                selectedFrequency = partialState.selectedFrequency,
                daysInWeek = partialState.daysInWeek,
                startDate = partialState.startDate
            )
            is PartialState.CustomerData -> currentState.copy(
                customer = partialState.value
            )
            PartialState.ShowProgress -> currentState
        }
    }
}
