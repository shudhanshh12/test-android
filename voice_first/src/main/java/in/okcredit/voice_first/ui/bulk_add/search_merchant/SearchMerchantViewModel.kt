package `in`.okcredit.voice_first.ui.bulk_add.search_merchant

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.voice_first.R
import `in`.okcredit.voice_first.ui.bulk_add.search_merchant.SearchMerchantContract.*
import `in`.okcredit.voice_first.ui.bulk_add.search_merchant.search_results_list.SearchItem
import `in`.okcredit.voice_first.usecase.bulk_add.GetSearchMerchantData
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.rx2.asObservable
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import javax.inject.Inject

class SearchMerchantViewModel @Inject constructor(
    initialState: Lazy<State>,
    private val getSearchMerchantData: Lazy<GetSearchMerchantData>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    private val onSearchPublishSubject: BehaviorSubject<String> = BehaviorSubject.create()
    private val resetData: BehaviorSubject<Unit> = BehaviorSubject.createDefault(Unit)

    override fun handle(): Observable<PartialState> {
        return mergeArray(

            setViewModelParams(),

            observeSearchDataChanges(),

            observeSearch(),

            observeResetData(),

            setSearchVisibility(),
        )
    }

    private fun observeResetData(): Observable<PartialState> {
        return intent<Intent.ResetData>()
            .map {
                resetData.onNext(Unit)
                PartialState.NoChange
            }
    }

    private fun observeSearchDataChanges(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { resetData }
            .switchMap { onSearchPublishSubject }
            .switchMap { wrap(getSearchMerchantData.get().execute(it).asObservable()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        PartialState.SetData(
                            customers = it.value.customers,
                            suppliers = it.value.suppliers,
                        )
                    }
                    is Result.Failure -> {
                        PartialState.HideLoading
                    }
                }
            }
    }

    private fun observeSearch(): Observable<PartialState> {
        return intent<Intent.SearchQuery>()
            .map {
                onSearchPublishSubject.onNext(it.searchQuery)
                PartialState.UpdateSearchQuery(it.searchQuery)
            }
    }

    private fun setViewModelParams(): Observable<PartialState> {
        return intent<Intent.Load>()
            .map {
                PartialState.NoChange
            }
    }

    private fun setSearchVisibility() = intent<Intent.ShowSearchInput>()
        .map {
            if (it.canShow) {
                emitViewEvent(ViewEvent.ShowKeyboard)
            }
            PartialState.SetSearchInput(it.canShow.not())
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        val tempState = when (partialState) {
            is PartialState.HideLoading -> currentState.copy(isLoading = false)
            is PartialState.UpdateSearchQuery -> currentState.copy(searchQuery = partialState.searchQuery)
            is PartialState.SetData -> currentState.copy(
                customers = partialState.customers, suppliers = partialState.suppliers, isLoading = false
            )
            is PartialState.NoChange -> currentState

            is PartialState.SetSearchInput -> currentState.copy(hideSearchInput = partialState.hide)
        }

        return tempState.copy(
            itemList = buildSearchItemList(tempState)
        )
    }

    private fun buildSearchItemList(state: State): List<SearchItem> {
        val list = mutableListOf<SearchItem>()
        addNoUserFoundItem(state, list)
        addCustomers(state, list)
        addSuppliers(state, list)

        return list
    }

    private fun addSuppliers(state: State, list: MutableList<SearchItem>) {
        if (state.suppliers.isEmpty()) return

        if (state.searchQuery.isNotEmpty()) {
            list.add(SearchItem.HeaderItem(R.string.suppliers))
        }

        list.addAll(state.suppliers.map { SearchItem.MerchantItem(it) })
    }

    private fun addCustomers(state: State, list: MutableList<SearchItem>) {
        if (state.customers.isEmpty()) return

        if (state.searchQuery.isNotEmpty()) {
            list.add(SearchItem.HeaderItem(R.string.customers))
        }

        list.addAll(state.customers.map { SearchItem.MerchantItem(it) })
    }

    private fun addNoUserFoundItem(state: State, list: MutableList<SearchItem>) {
        if (state.customers.isEmpty() && state.suppliers.isEmpty() &&
            state.isLoading.not() && state.searchQuery.isNotNullOrBlank()
        ) {
            list.add(SearchItem.NoUserFoundItem(state.searchQuery))
        }
    }
}
