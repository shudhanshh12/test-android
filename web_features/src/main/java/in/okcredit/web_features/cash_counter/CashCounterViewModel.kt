package `in`.okcredit.web_features.cash_counter

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.auth.AuthService
import javax.inject.Inject

class CashCounterViewModel @Inject constructor(
    initialState: CashCounterContract.State = CashCounterContract.State(),
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val authService: AuthService
) : BaseViewModel<CashCounterContract.State, CashCounterContract.PartialState, CashCounterContract.ViewEvent>(
    initialState
) {

    override fun handle(): Observable<out UiState.Partial<CashCounterContract.State>> {
        return Observable.mergeArray(
            intent<CashCounterContract.Intent.Load>()
                .take(1)
                .switchMap { wrap(getActiveBusinessId.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> CashCounterContract.PartialState.NoChange
                        is Result.Success -> {
                            CashCounterContract.PartialState.SetMerchantIdAndAuthToken(
                                it.value,
                                authService.getAuthToken() ?: ""
                            )
                        }
                        is Result.Failure -> {
                            emitViewEvent(CashCounterContract.ViewEvent.ShowError)
                            CashCounterContract.PartialState.NoChange
                        }
                    }
                },
            intent<CashCounterContract.Intent.WebPageLoaded>()
                .map {
                    emitViewEvent(CashCounterContract.ViewEvent.WebPageLoaded)
                    CashCounterContract.PartialState.WebPageLoaded
                }
        )
    }

    override fun reduce(
        currentState: CashCounterContract.State,
        partialState: CashCounterContract.PartialState
    ): CashCounterContract.State {
        return when (partialState) {
            CashCounterContract.PartialState.NoChange -> currentState
            is CashCounterContract.PartialState.SetMerchantIdAndAuthToken -> currentState.copy(
                merchantId = partialState.merchantId,
                authToken = partialState.authToken
            )
            CashCounterContract.PartialState.WebPageLoaded -> currentState.copy(isLoading = false)
        }
    }
}
