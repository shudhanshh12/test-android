package `in`.okcredit.collection_ui.ui.home_menu

import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.collection.contract.CollectionSyncer.Source.HOME_PAYMENTS
import `in`.okcredit.collection_ui.ui.home_menu.HomePaymentsContainerContract.*
import `in`.okcredit.collection_ui.usecase.GetOnlinePaymentCount
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class HomePaymentsContainerViewModel @Inject constructor(
    initialState: State,
    private val getOnlinePaymentCount: Lazy<GetOnlinePaymentCount>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            observeForCollectionActivated(),
            syncMerchantQrAndPaymentOnLoad(),
        )
    }

    private fun syncMerchantQrAndPaymentOnLoad() = intent<Intent.RefreshMerchantPayments>()
        .switchMap { wrap { collectionSyncer.get().scheduleSyncOnlinePayments(HOME_PAYMENTS) } }
        .map { PartialState.NoChange }

    private fun observeForCollectionActivated() = intent<Intent.Load>().switchMap {
        wrap(getOnlinePaymentCount.get().execute())
    }.map {
        when (it) {
            is Result.Failure -> PartialState.SetLoading(false)
            is Result.Progress -> PartialState.SetLoading(true)
            is Result.Success -> {
                if (it.value > 0) {
                    emitViewEvent(ViewEvent.ShowTransactionHistory)
                    PartialState.EmptyCollections(false)
                } else {
                    PartialState.EmptyCollections(true)
                }
            }
        }
    }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            is PartialState.SetLoading -> currentState.copy(loading = partialState.loading)
            is PartialState.EmptyCollections -> currentState.copy(
                loading = false,
                showEmptyCollections = partialState.emptyCollections,
            )
        }
    }
}
