package `in`.okcredit.merchant.ui.switch_business

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.merchant.R
import `in`.okcredit.merchant.ui.switch_business.SwitchBusinessContract.*
import `in`.okcredit.merchant.usecase.GetBusinessList
import `in`.okcredit.merchant.usecase.SwitchBusiness
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class SwitchBusinessViewModel @Inject constructor(
    state: State,
    private val getBusinessList: Lazy<GetBusinessList>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val switchBusiness: Lazy<SwitchBusiness>,
    private val switchBusinessAnalytics: Lazy<SwitchBusinessAnalytics>,
    private val context: Lazy<Context>,
) : BaseViewModel<State, PartialState, ViewEvent>(state) {
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            loadBusinessList(),
            handleCreateNewBusiness(),
            handleSetActiveBusiness()
        )
    }

    private fun loadBusinessList() = intent<Intent.Load>()
        .switchMap {
            wrap(
                Single.zip(
                    getBusinessList.get().execute(),
                    getActiveBusinessId.get().execute(),
                    { businessList, activeBusinessId ->
                        return@zip Pair(businessList, activeBusinessId)
                    }
                )
            )
        }
        .map {
            when (it) {
                is Result.Success -> {
                    val businessList = it.value.first
                    val activeBusinessId = it.value.second
                    val businessModelList = businessList
                        .map { business -> BusinessModel(business, business.id == activeBusinessId) }
                        .sortedByDescending { business -> business.isActive }
                    PartialState.SetBusinessList(businessModelList)
                }
                else -> PartialState.NoChange
            }
        }

    private fun handleCreateNewBusiness() = intent<Intent.CreateNewBusiness>()
        .map {
            emitViewEvent(ViewEvent.ShowCreateBusinessDialog)
            PartialState.NoChange
        }

    private fun handleSetActiveBusiness() = intent<Intent.SetActiveBusiness>()
        .take(1)
        .switchMap { wrap { switchBusiness.get().execute(it.businessId, it.businessName, it.weakActivity) } }
        .map {
            if (it is Result.Failure) {
                switchBusinessAnalytics.get().trackSwitchBusinessError(it.error)
                emitViewEvent(ViewEvent.ShowError(context.get().getString(R.string.err_default)))
            }
            PartialState.NoChange
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            is PartialState.SetBusinessList -> currentState.copy(
                businessModelList = partialState.businessModelList
            )
        }
    }
}
