package `in`.okcredit.collection_ui.ui.home

import `in`.okcredit.collection.contract.IsCollectionActivatedOrOnlinePaymentExist
import `in`.okcredit.collection_ui.ui.home.CollectionsHomeActivityContract.*
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class CollectionsHomeActivityViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam("referral_merchant_id") val referralMerchantId: String?,
    private val isCollectionActivatedOrOnlinePaymentExist: Lazy<IsCollectionActivatedOrOnlinePaymentExist>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            observeForCollectionActivated(),
        )
    }

    private fun observeForCollectionActivated() = intent<Intent.Load>().switchMap {
        wrap(isCollectionActivatedOrOnlinePaymentExist.get().execute().firstOrError())
    }.map {
        when (it) {
            is Result.Failure -> PartialState.SetLoading(false)
            is Result.Progress -> PartialState.SetLoading(true)
            is Result.Success -> {
                if (it.value) {
                    emitViewEvent(ViewEvent.QrScreen)
                } else {
                    emitViewEvent(ViewEvent.CollectionAdoption(referralMerchantId))
                }
                PartialState.SetLoading(false)
            }
        }
    }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            is PartialState.SetLoading -> currentState.copy(loading = partialState.loading)
        }
    }
}
