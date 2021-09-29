package tech.okcredit.home.ui.home.supplier

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.supplier.home.tab.GetSupplierSortType
import `in`.okcredit.supplier.home.tab.SetSupplierSortType
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.home.ui.home.supplier.SupplierSortContract.Intent
import tech.okcredit.home.ui.home.supplier.SupplierSortContract.PartialState
import tech.okcredit.home.ui.home.supplier.SupplierSortContract.State
import tech.okcredit.home.ui.home.supplier.SupplierSortContract.ViewEvent
import javax.inject.Inject

class SupplierSortViewModel @Inject constructor(
    initialState: State,
    private val getSupplierSortType: Lazy<GetSupplierSortType>,
    private val setSupplierSortType: Lazy<SetSupplierSortType>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            load(),
            selectSortType()
        )
    }

    private fun load(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(getSupplierSortType.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> PartialState.SortType(it.value)
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun selectSortType(): Observable<PartialState> {
        return intent<Intent.SelectSortType>()
            .switchMap { sortType ->
                wrap(setSupplierSortType.get().execute(sortType.type))
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.ApplySort)
                        PartialState.SortType(it.value)
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.SortType -> currentState.copy(sortType = partialState.type)
        }
    }
}
