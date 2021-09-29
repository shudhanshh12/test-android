package tech.okcredit.home.ui.dashboard

import `in`.okcredit.dynamicview.Targets
import `in`.okcredit.dynamicview.component.dashboard.recycler_card.RecyclerCardComponentModel
import `in`.okcredit.dynamicview.component.recycler.RecyclerComponentModel
import `in`.okcredit.dynamicview.data.model.ComponentModel
import `in`.okcredit.dynamicview.events.ViewEventHandler
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.home.ui.dashboard.DashboardContract.*
import tech.okcredit.home.ui.dashboard.DashboardContract.PartialState.DashboardCustomizationWithValues
import tech.okcredit.home.ui.dashboard.DashboardContract.PartialState.NoChange
import tech.okcredit.home.usecase.dashboard.GetDashboardCustomizationWithValues
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    initialState: Lazy<State>,
    private val getDashboardCustomizationWithValues: Lazy<GetDashboardCustomizationWithValues>,
    private val viewEventHandler: Lazy<ViewEventHandler>
) : BaseViewModel<State, PartialState, ViewEvent>(initialState.get()) {

    companion object {
        const val TAG = "DashboardPresenter"
    }

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            loadDashboardCustomizationWithValues()
        )
    }

    private fun loadDashboardCustomizationWithValues(): Observable<PartialState> {
        return wrap(getDashboardCustomizationWithValues.get().execute())
            .map {
                when (it) {
                    is Result.Progress -> NoChange
                    is Result.Success -> {
                        trackCustomizationEvents(it.value.component)
                        DashboardCustomizationWithValues(it.value)
                    }
                    is Result.Failure -> NoChange
                }
            }
    }

    private fun trackCustomizationEvents(component: ComponentModel?) {
        component?.let {
            viewEventHandler.get().trackViewEvent(Targets.DASHBOARD.value, component)
            when (component) {
                is RecyclerComponentModel -> component.items?.forEach { item -> trackCustomizationEvents(item) }
                is RecyclerCardComponentModel -> component.items?.forEach { item -> trackCustomizationEvents(item) }
                else -> return
            }
        }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is NoChange -> currentState
            is DashboardCustomizationWithValues -> currentState.copy(
                customization = partialState.customization,
                isLoading = false
            )
        }
    }
}
