package `in`.okcredit.collection_ui.ui.defaulters

import `in`.okcredit.collection_ui.ui.defaulters.DefaulterListContract.*
import `in`.okcredit.collection_ui.ui.defaulters.DefaulterListContract.PartialState.DefaulterList
import `in`.okcredit.collection_ui.ui.defaulters.DefaulterListContract.PartialState.NoChange
import `in`.okcredit.collection_ui.usecase.GetDefaulterList
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class DefaulterListViewModel @Inject constructor(
    initialState: Lazy<State>,
    private val getDefaulterList: Lazy<GetDefaulterList>
) : BaseViewModel<State, PartialState, ViewEvent>(initialState.get()) {

    companion object {
        const val TAG = "DefaulterListPresenter"
    }

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            getDefaulterList()
        )
    }

    private fun getDefaulterList(): Observable<PartialState> {
        return getDefaulterList.get().execute(Unit)
            .map {
                when (it) {
                    is Result.Progress -> NoChange
                    is Result.Success -> DefaulterList(it.value)
                    is Result.Failure -> NoChange
                }
            }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is NoChange -> currentState
            is DefaulterList -> currentState.copy(defaulterList = partialState.defaulterList, isLoading = false)
        }
    }
}
